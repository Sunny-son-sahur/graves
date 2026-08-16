package eu.pb4.graves.other;

import eu.pb4.graves.GravesApi;
import eu.pb4.graves.grave.GraveInventoryMask;
import eu.pb4.graves.mixin.PlayerInventoryAccessor;
import eu.pb4.graves.model.ModelTags;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class VanillaInventoryMask implements GraveInventoryMask {
    public static final VanillaInventoryMask INSTANCE = new VanillaInventoryMask();

    @Override
    public void addToGrave(ServerPlayer player, ItemConsumer consumer) {
        var inventory = player.getInventory();
        var size = inventory.getContainerSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (GravesApi.canAddItem(player, itemStack)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                consumer.addItem(itemStack, slot, createTags(player, inventory, slot, itemStack));
            }
        }

        ItemStack itemStack = player.inventoryMenu.getCarried();
        if (GravesApi.canAddItem(player, itemStack)) {
            consumer.addItem(itemStack.copy(), -1);
            player.inventoryMenu.setCarried(ItemStack.EMPTY);
        }
    }

    private Identifier[] createTags(ServerPlayer player, Inventory inventory, int slot, ItemStack stack) {
        if (slot == inventory.getSelectedSlot()) {
            return new Identifier[]{ModelTags.EQUIPMENT_MAIN_HAND};
        }

        return switch (slot) {
            case Inventory.SLOT_OFFHAND -> new Identifier[]{ModelTags.EQUIPMENT_OFFHAND_HAND};
            case Inventory.INVENTORY_SIZE -> new Identifier[]{ModelTags.EQUIPMENT_BOOTS};
            case Inventory.INVENTORY_SIZE + 1 -> new Identifier[]{ModelTags.EQUIPMENT_LEGGINGS};
            case Inventory.INVENTORY_SIZE + 2 -> new Identifier[]{ModelTags.EQUIPMENT_CHESTPLATE};
            case Inventory.INVENTORY_SIZE + 3 -> new Identifier[]{ModelTags.EQUIPMENT_HELMET};
            default -> new Identifier[0];
        };
    }

    @Override
    public boolean moveToPlayerExactly(ServerPlayer player, ItemStack stack, int slot, Tag extraData) {
        var inventory = player.getInventory();
        if (slot > -1 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack.copy());
            stack.setCount(0);
            return true;
        }

        return false;
    }

    @Override
    public boolean moveToPlayerClosest(ServerPlayer player, ItemStack stack, int intended, Tag extraData) {
        var inventory = player.getInventory();
        if (!stack.isEmpty()) {
            int slot;
            try {
                if (stack.isDamaged()) {
                    slot = inventory.getFreeSlot();

                    if (slot >= 0) {
                        inventory.getNonEquipmentItems().set(slot, stack.copy());
                        stack.setCount(0);
                        return true;
                    }
                } else {
                    int i;
                    do {
                        i = stack.getCount();
                        stack.setCount(((PlayerInventoryAccessor) inventory).callAddResource(stack));
                    } while (!stack.isEmpty() && stack.getCount() < i);
                }
            } catch (Exception e) {
                // Silence!
            }
        }

        return false;
    }
}
