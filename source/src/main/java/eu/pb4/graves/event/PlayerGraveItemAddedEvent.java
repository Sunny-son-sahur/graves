package eu.pb4.graves.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * This is invoked before item is added to the grave
 */
public interface PlayerGraveItemAddedEvent {
    Event<PlayerGraveItemAddedEvent> EVENT = EventFactory.createArrayBacked(PlayerGraveItemAddedEvent.class,
                (listeners) -> (player, item) -> {
                    for (PlayerGraveItemAddedEvent listener : listeners) {
                        InteractionResult result = listener.canAddItem(player, item);

                        if (result != InteractionResult.PASS) {
                            return result;
                        }
                    }
                    return InteractionResult.PASS;
                });

    InteractionResult canAddItem(ServerPlayer player, ItemStack item);
}
