package eu.pb4.graves.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Inventory.class)
public interface PlayerInventoryAccessor {
    @Invoker
    int callAddResource(ItemStack stack);
}
