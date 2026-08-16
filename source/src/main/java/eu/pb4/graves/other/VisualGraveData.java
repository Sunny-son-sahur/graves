package eu.pb4.graves.other;

import eu.pb4.graves.config.Config;
import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.grave.Grave;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public record VisualGraveData(ResolvableProfile profile, byte visualSkinModelLayers, HumanoidArm mainArm, Component deathCause, long creationTime, Location location, int minecraftDay) {
    public static final VisualGraveData DEFAULT = new VisualGraveData(Grave.DEFAULT_PROFILE_COMPONENT, (byte) 0xFF, HumanoidArm.RIGHT, Grave.DEFAULT_DEATH_CAUSE, 0, new Location(Identifier.parse("the_void"), BlockPos.ZERO), -1);

    public Map<String, Component> getPlaceholders(MinecraftServer server) {
        Config config = ConfigManager.getConfig();

        Map<String, Component> values = new HashMap<>();
        values.put("player", Component.literal(this.profile != null ? this.profile.name().orElse("<No player!>") : "<No player!>"));
        values.put("protection_time", Component.literal("" + (config.protection.protectionTime > -1 ? config.getFormattedTime(0) : config.texts.infinityText)));
        values.put("break_time", Component.literal("" + (config.protection.breakingTime > -1 ? config.getFormattedTime(0) : config.texts.infinityText)));
        values.put("xp", Component.literal("0"));
        values.put("item_count", Component.literal("0"));
        values.put("position", Component.literal("" + this.location.blockPos().toShortString()));
        values.put("world", GraveUtils.toWorldName(this.location.world()));
        values.put("death_cause", this.deathCause);
        values.put("minecraft_day", Component.literal("" + this.minecraftDay));
        values.put("creation_date", Component.literal(config.texts.fullDateFormat.format().format(new Date(this.creationTime * 1000))));
        values.put("since_creation", Component.literal(config.getFormattedTime(System.currentTimeMillis() / 1000 - this.creationTime)));
        values.put("id", Component.literal("<no id>"));
        return values;
    }

    public void writeData(ValueOutput view) {
        view.store("GameProfile", ResolvableProfile.CODEC, this.profile);
        view.store("DeathCause", ComponentSerialization.CODEC, this.deathCause);
        view.putLong("CreationTime", this.creationTime);
        view.putInt("MinecraftDay", this.minecraftDay);
        view.putByte("SkinModelParts", this.visualSkinModelLayers);
        view.putByte("MainArm", (byte) this.mainArm.ordinal());
        this.location.writeData(view);
    }


    public static VisualGraveData readData(ValueInput view) {
        return new VisualGraveData(
                LegacyNbtHelper.readProfileComponentOrLegacyGameProfile(view.childOrEmpty("GameProfile")).orElse(Grave.DEFAULT_PROFILE_COMPONENT),
                view.getByteOr("SkinModelParts", (byte) 0xFF),
                view.getByteOr("MainArm", (byte) 0) == HumanoidArm.LEFT.ordinal() ? HumanoidArm.LEFT : HumanoidArm.RIGHT,
                view.read("DeathCause", ComponentSerialization.CODEC).orElse(Component.empty()),
                view.getLongOr("CreationTime", 0),
                Location.readData(view),
                view.getIntOr("MinecraftDay", 0)
        );
    }
}
