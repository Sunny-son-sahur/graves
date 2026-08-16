package eu.pb4.graves.grave;

import eu.pb4.graves.GravesApi;
import eu.pb4.graves.model.ModelTags;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface GraveInventoryMask {
    void addToGrave(ServerPlayer player, ItemConsumer itemStackConsumer);

    boolean moveToPlayerExactly(ServerPlayer player, ItemStack stack, int slot, @Nullable Tag optionalData);
    boolean moveToPlayerClosest(ServerPlayer player, ItemStack stack, int slot, @Nullable Tag optionalData);

    default Identifier getId() {
        return GravesApi.getInventoryMaskId(this);
    }


    interface ItemConsumer {
        default void addItem(ItemStack stack, int slot) {
            this.addItem(stack, slot, null, new Identifier[0]);
        }
        default void addItem(ItemStack stack, int slot, Identifier... tags) {
            this.addItem(stack, slot, null, tags);
        }

        default void addItem(ItemStack stack, int slot, @Nullable Tag nbtElement) {
            this.addItem(stack, slot, nbtElement, new Identifier[0]);
        };

        void addItem(ItemStack stack, int slot, @Nullable Tag nbtElement, Identifier... tags);
    }
}
