package eu.pb4.graves.other;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OutputSlot extends Slot {
    private final boolean canTake;

    public OutputSlot(Container inventory, int index, int x, int y, boolean canTake) {
        super(inventory, index, x, y);
        this.canTake = canTake;
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return this.canTake;
    }

    @Override
    public ItemStack remove(int amount) {
        return this.canTake ? super.remove(amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeTake(int min, int max, Player player) {
        return this.canTake ? super.safeTake(min, max, player) : ItemStack.EMPTY;
    }

    @Override
    public boolean allowModification(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
