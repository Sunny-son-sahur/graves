package eu.pb4.graves.compat;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.graves.GravesApi;
import eu.pb4.graves.grave.GraveInventoryMask;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record TrinketsUpdatedCompat() implements GraveInventoryMask {
    private static final String INVENTORY_TAG = "InvId";

    public static void register() {
        GravesApi.registerInventoryMask(Identifier.fromNamespaceAndPath("universal_graves", "trinkets_updated"), new TrinketsUpdatedCompat());
    }

    @Override
    public void addToGrave(ServerPlayer player, ItemConsumer consumer) {
        TrinketsApi.getAttachment(player).forEach((ref, stack) -> {
            if (stack.isEmpty() || !GravesApi.canAddItem(player, stack)) {
                return;
            }

            var dropRule = TrinketsApi.getDropRule(stack, ref, player, false);

            if (dropRule == TrinketDropRule.DROP) {
                var nbt = new CompoundTag();
                nbt.putString(INVENTORY_TAG, ref.inventory().slotType().getId());

                consumer.addItem(stack.copy(), ref.index(), nbt);
                ref.set(ItemStack.EMPTY);
            }
        });
    }

    @Override
    public boolean moveToPlayerExactly(ServerPlayer player, ItemStack stack, int slot, Tag extraData) {
        var inventoryId = ((CompoundTag) extraData).getStringOr(INVENTORY_TAG, "");

        var access = TrinketsApi.getAttachment(player).getSlotAccess(inventoryId, slot);

        if (access != null) {
            if (access.get().isEmpty()) {
                access.set(stack.copyAndClear());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean moveToPlayerClosest(ServerPlayer player, ItemStack stack, int slot, Tag data) {
        var inventoryId = ((CompoundTag) data).getStringOr(INVENTORY_TAG, "");

        var inventory = TrinketsApi.getAttachment(player).getInventory(inventoryId);

        if (inventory != null) {
            int size = inventory.getContainerSize();

            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i).isEmpty()) {
                    inventory.setItem(i, stack.copyAndClear());
                    return true;
                }
            }
        }
        return false;
    }
}