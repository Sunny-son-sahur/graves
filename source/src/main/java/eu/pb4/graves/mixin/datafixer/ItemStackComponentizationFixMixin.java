package eu.pb4.graves.mixin.datafixer;

import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixCustomStacks(ItemStackComponentizationFix.ItemStackData data, Dynamic dynamic, CallbackInfo ci) {
        if (data.is("universal_graves:icon")) {
            data.moveTagToComponent("Texture", "universal_graves:texture", dynamic.createString(""));
        } else if (data.is("universal_graves:grave_compass")) {
            data.setComponent("universal_graves:compass", dynamic.emptyMap()
                    .set("id", data.removeTag("GraveId").result().orElse(dynamic.createLong(-1)))
                    .set("vanilla", data.removeTag("ConvertToVanillaGraveId").result().orElse(dynamic.createBoolean(false)))
            );
        }
    }
}
