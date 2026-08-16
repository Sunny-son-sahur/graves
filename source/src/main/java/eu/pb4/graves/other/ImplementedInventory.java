package eu.pb4.graves.other;

import java.util.Iterator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A simple {@code Inventory} implementation with only default methods + an item list getter.
 *
 * Originally by Juuz
 */
public interface ImplementedInventory extends Container {

    NonNullList<ItemStack> getItems();


    static ImplementedInventory of(NonNullList<ItemStack> items, Runnable markDirty) {
        return new ImplementedInventory() {
            @Override
            public NonNullList<ItemStack> getItems() {
                return items;
            }

            @Override
            public void setChanged() {
                markDirty.run();
            }
        };
    }


    static ImplementedInventory ofSize(int size) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY), () -> {});
    }


    @Override
    default int getContainerSize() {
        return getItems().size();
    }


    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }


    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }


    @Override
    default ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(this.getItems(), slot, count);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }


    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.getItems(), slot);
    }


    @Override
    default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
    }

    @Override
    default void clearContent() {
        getItems().clear();
    }


    void setChanged();


    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    default boolean canInsert(ItemStack stack) {
        boolean bl = false;
        var var3 = this.getItems().iterator();

        while(var3.hasNext()) {
            ItemStack itemStack = (ItemStack)var3.next();
            if (itemStack.isEmpty() || this.canCombine(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                bl = true;
                break;
            }
        }

        return bl;
    }

    default ItemStack addStack(ItemStack stack) {
        ItemStack itemStack = stack.copy();
        this.addToExistingSlot(itemStack);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.addToNewSlot(itemStack);
            return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
        }
    }

    default void addToExistingSlot(ItemStack stack) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (this.canCombine(itemStack, stack)) {
                this.transfer(stack, itemStack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    default void addToNewSlot(ItemStack stack) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) {
                this.setItem(i, stack.copy());
                stack.setCount(0);
                return;
            }
        }
    }

    default void transfer(ItemStack source, ItemStack target) {
        int i = Math.min(this.getMaxStackSize(), target.getMaxStackSize());
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.grow(j);
            source.shrink(j);
            this.setChanged();
        }

    }

   default boolean canCombine(ItemStack one, ItemStack two) {
        return one.isEmpty() == two.isEmpty() && ItemStack.isSameItemSameComponents(one, two);
    }
}