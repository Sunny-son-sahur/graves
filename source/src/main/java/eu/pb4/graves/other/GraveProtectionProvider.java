package eu.pb4.graves.other;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.ProtectionProvider;
import eu.pb4.graves.grave.GraveHolder;
import eu.pb4.graves.registry.GraveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public final class GraveProtectionProvider implements ProtectionProvider {
    public static final ProtectionProvider INSTANCE = new GraveProtectionProvider();

    private GraveProtectionProvider() {}

    @Override
    public boolean isProtected(Level world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof GraveBlockEntity;
    }

    @Override
    public boolean isAreaProtected(Level world, AABB area) {
        return false;
    }

    @Override
    public boolean canBreakBlock(Level world, BlockPos pos, NameAndId profile, @Nullable Player player) {
        var be = world.getBlockEntity(pos, GraveBlockEntity.BLOCK_ENTITY_TYPE);

        return be.isEmpty() || (be.get().getGrave() != null && be.get().getGrave().canTakeFrom(profile));
    }

    @Override
    public boolean canDamageEntity(Level world, Entity entity, NameAndId profile, @Nullable Player player) {
        return !(entity instanceof GraveHolder graveHolder && graveHolder.getGrave() != null && !graveHolder.getGrave().canTakeFrom(profile));
    }
}
