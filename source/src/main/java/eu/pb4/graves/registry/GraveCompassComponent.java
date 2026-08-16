package eu.pb4.graves.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;

public record GraveCompassComponent(long graveId, boolean convertToVanilla)  {
    public static final DataComponentType<GraveCompassComponent> TYPE = DataComponentType.<GraveCompassComponent>builder()
            .persistent(RecordCodecBuilder.create(instance -> instance.group(
                    Codec.LONG.fieldOf("id").forGetter(GraveCompassComponent::graveId),
                    Codec.BOOL.fieldOf("vanilla").forGetter(GraveCompassComponent::convertToVanilla)
            ).apply(instance, GraveCompassComponent::new))).build();
}
