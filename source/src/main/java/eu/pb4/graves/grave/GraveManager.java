package eu.pb4.graves.grave;

import com.mojang.datafixers.DataFixer;
import eu.pb4.graves.GravesMod;
import eu.pb4.graves.other.Location;
import eu.pb4.graves.registry.GraveGameRules;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

import static eu.pb4.graves.GravesMod.id;

public final class GraveManager extends SavedData {
    public static GraveManager INSTANCE;
    private final HashMap<UUID, Set<Grave>> byUuid = new HashMap<>();
    private final HashMap<Location, Grave> byLocation = new HashMap<>();
    private final Long2ObjectMap<Grave> byId = new Long2ObjectOpenHashMap<>();
    private final HashSet<Grave> graves = new HashSet<>();
    private long ticker;
    private long currentGameTime = 0;
    private long currentGraveId = 0;
    private int protectionTime;
    private int breakingTime;


    public static SavedDataType<GraveManager> getType(MinecraftServer server) {
        return new SavedDataType<>(id("graves"), GraveManager::new,
                ExtraCodecs.converter(NbtOps.INSTANCE)
                        .xmap(
                                nbt -> fromNbt((CompoundTag) nbt, server.registryAccess(), server.getFixerUpper()),
                                manager -> manager.writeNbt(new CompoundTag(), server.registryAccess())),
                null);
    }

    public void add(Grave grave) {
        if (grave.getId() == -1) {
            grave.setId(this.requestId());
        }

        this.byUuid.computeIfAbsent(grave.getProfile().id(), (v) -> new HashSet<>()).add(grave);
        this.byLocation.put(grave.getLocation(), grave);
        this.byId.put(grave.getId(), grave);
        this.graves.add(grave);
        this.setDirty();
    }

    public void remove(Grave info) {
        if (this.graves.remove(info)) {
            var graveInfoList = this.byUuid.get(info.getProfile().id());
            this.byLocation.remove(info.getLocation());
            this.byId.remove(info.getId());
            if (graveInfoList != null) {
                graveInfoList.remove(info);
                if (graveInfoList.isEmpty()) {
                    this.byUuid.remove(info.getProfile().id());
                }
            }
            this.setDirty();
        }
    }

    public CompoundTag writeNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();

        for (Grave grave : new ArrayList<>(this.graves)) {
            if (!grave.shouldNaturallyBreak()) {
                list.add(grave.writeNbt(new CompoundTag(), lookup));
            }
        }
        nbt.put("Graves", list);
        nbt.putInt("Version", 3);
        nbt.putInt("GameVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        nbt.putLong("CurrentGameTime", this.currentGameTime);
        nbt.putLong("CurrentGrave", this.currentGraveId);

        return nbt;
    }

    public static GraveManager fromNbt(CompoundTag nbt, HolderLookup.Provider lookup, DataFixer dataFixer) {
        GraveManager manager = new GraveManager();
        GraveManager.INSTANCE = manager;
        int dataVersion = nbt.getIntOr("GameVersion", 3700);

        manager.currentGameTime = nbt.getLongOr("CurrentGameTime", 0);
        manager.currentGraveId = nbt.getLongOr("CurrentGrave", 0);

        for (var graveNbt : nbt.getListOrEmpty("Graves")) {
            Grave graveInfo = new Grave();
            graveInfo.readNbt((CompoundTag) graveNbt, lookup, dataFixer, dataVersion, SharedConstants.getCurrentVersion().dataVersion().version());
            manager.add(graveInfo);
        }
        return manager;
    }

    public Collection<Grave> getByUuid(UUID uuid) {
        var graveInfoList = this.byUuid.get(uuid);
        if (graveInfoList != null) {
            graveInfoList.removeIf(Grave::shouldNaturallyBreak);
            return graveInfoList;
        }

        return Collections.emptyList();
    }

    public Grave getId(long id) {
        return this.byId.get(id);
    }

    public Grave getByLocation(Location location) {
        return this.byLocation.get(location);
    }

    @ApiStatus.Internal
    public void moveToLocation(Grave grave, Location location) {
        this.byLocation.remove(grave.getLocation());
        this.byLocation.put(location, grave);
    }

    public Grave getByLocation(Identifier world, BlockPos pos) {
        return this.getByLocation(new Location(world, pos.immutable()));
    }

    public Grave getByLocation(Level world, BlockPos pos) {
        return this.getByLocation(new Location(world.dimension().identifier(), pos.immutable()));
    }

    public Collection<Grave> getAll() {
        return this.graves;
    }

    public void tick(MinecraftServer server) {
        this.ticker++;
        if (this.ticker >= 20) {
            this.ticker = 0;
            this.currentGameTime++;

            this.protectionTime = GraveGameRules.getProtectionTime(server);
            this.breakingTime = GraveGameRules.getBreakingTime(server);

            try {
                for (var grave : this.graves.toArray(new Grave[0])) {
                    grave.tick(server);
                }
            } catch (Exception e) {
                GravesMod.LOGGER.error("Failed to tick grave!", e);
            }
        }
    }

    public long getCurrentGameTime() {
        return this.currentGameTime;
    }

    public long requestId() {
        return this.currentGraveId++;
    }

    public int getProtectionTime() {
        return this.protectionTime;
    }

    public boolean isProtectionEnabled() {
        return this.protectionTime != 0;
    }

    public int getBreakingTime() {
        return this.breakingTime;
    }

    public Collection<Grave> getByPlayer(ServerPlayer player) {
        return this.getByUuid(player.getUUID());
    }

    public void setServer(MinecraftServer server) {
        this.protectionTime = GraveGameRules.getProtectionTime(server);
        this.breakingTime = GraveGameRules.getBreakingTime(server);
    }
}
