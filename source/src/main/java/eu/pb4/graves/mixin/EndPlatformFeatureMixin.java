package eu.pb4.graves.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.graves.registry.GraveBlock;
import eu.pb4.graves.registry.GravesRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndPlatformFeature.class)
public class EndPlatformFeatureMixin {
    @WrapOperation(method = "createEndPlatform(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private static boolean grave_dontBreak(BlockState instance, Object o, Operation<Boolean> original) {
        return original.call(instance, o) || instance.is(GravesRegistry.GRAVE_BLOCK);
    }
}
