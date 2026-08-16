package eu.pb4.graves.compat;
/*
import de.rubixdev.inventorio.api.InventorioAPI;
import eu.pb4.graves.GravesApi;
import eu.pb4.graves.grave.GraveInventoryMask;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public record InventorioCompat() implements GraveInventoryMask {
    public static final GraveInventoryMask INSTANCE = new InventorioCompat();

    public static void register() {
        GravesApi.registerInventoryMask(Identifier.fromNamespaceAndPath("universal_graves", "inventorio"), INSTANCE);
    }

    @Override
    public void addToGrave(ServerPlayer player, ItemConsumer consumer) {
        var inv = InventorioAPI.getInventoryAddon(player);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (GravesApi.canAddItem(player, stack)) {
                consumer.addItem(inv.removeItemNoUpdate(i), i);
            }
        }
    }

    @Override
    public boolean moveToPlayerExactly(ServerPlayer player, ItemStack stack, int slot, Tag _unused) {
        var inventory = player.getInventory();

        if (inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack);
            return true;
        }

        return false;
    }

    @Override
    public boolean moveToPlayerClosest(ServerPlayer player, ItemStack stack, int intended, Tag _unused) {
        var inventory = InventorioAPI.getInventoryAddon(player);
        if (!stack.isEmpty()) {
            try {
                stack.setCount(inventory.addItem(stack).getCount());
                return true;
            } catch (Exception e) {
                // Silence!
            }
        }

        return false;
    }
}*/