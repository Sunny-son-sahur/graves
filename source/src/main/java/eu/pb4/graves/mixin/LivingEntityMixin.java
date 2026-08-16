package eu.pb4.graves.mixin;

import eu.pb4.graves.GravesMod;
import eu.pb4.graves.other.GraveUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropEquipment(Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.BEFORE))
    private void replaceWithGrave(ServerLevel world, DamageSource damageSource, CallbackInfo ci) {
        if (((Object) this) instanceof ServerPlayer player) {
            try {
                GraveUtils.createGrave(player, world, damageSource);
            } catch (Throwable e) {
                player.sendSystemMessage(Component.literal("Failed to create a grave due to an exception! See logs for more information and report it!").withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal(e.toString()).withStyle(ChatFormatting.RED));
                GravesMod.LOGGER.error("Failed to create a grave due to an exception!", e);
            }
        }
    }
}
