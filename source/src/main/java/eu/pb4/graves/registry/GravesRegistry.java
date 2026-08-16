package eu.pb4.graves.registry;

import eu.pb4.graves.GravesMod;
import eu.pb4.graves.other.GraveUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;

public interface GravesRegistry {
    GraveBlock GRAVE_BLOCK = register("grave", GraveBlock::new);
    VisualGraveBlock VISUAL_GRAVE_BLOCK = register("visual_grave", BlockBehaviour.Properties.ofFullCopy(GRAVE_BLOCK).destroyTime(4)
            .noLootTable(), VisualGraveBlock::new);
    ContainerGraveBlock CONTAINER_GRAVE_BLOCK = register("container_grave", BlockBehaviour.Properties.of().noOcclusion().dynamicShape().destroyTime(4),
            ContainerGraveBlock::new);
    TempBlock TEMP_BLOCK = register("temp_block", BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noCollision(), TempBlock::new);

    GraveCompassItem GRAVE_COMPASS_ITEM = registerItem("grave_compass", GraveCompassItem::new);
    CointainerGraveBlockItem CONTAINER_GRAVE_ITEM = registerItem("visual_grave", (s) -> new CointainerGraveBlockItem(CONTAINER_GRAVE_BLOCK, s.useBlockDescriptionPrefix()));
    IconItem ICON_ITEM = registerItem("icon", IconItem::new);

    static <T extends Item> T registerItem(String path, Function<Item.Properties, T> function) {
        var id = Identifier.fromNamespaceAndPath("universal_graves", path);
        var key = ResourceKey.create(Registries.ITEM, id);
        var settings = new Item.Properties();
        settings.setId(key);
        var value = function.apply(settings);
        Registry.register(BuiltInRegistries.ITEM, id, value);
        return value;
    }

    static <T extends Block> T register(String path, Function<BlockBehaviour.Properties, T> function) {
        var id = Identifier.fromNamespaceAndPath("universal_graves", path);
        var key = ResourceKey.create(Registries.BLOCK, id);
        var settings = BlockBehaviour.Properties.of();
        settings.setId(key);
        var value = function.apply(settings);
        Registry.register(BuiltInRegistries.BLOCK, id, value);
        return value;
    }

    static <T extends Block> T register(String path, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> function) {
        var id = GravesMod.id(path);
        var key = ResourceKey.create(Registries.BLOCK, id);
        settings.setId(key);
        var value = function.apply(settings);
        Registry.register(BuiltInRegistries.BLOCK, id, value);
        return value;
    }

    static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("universal_graves", "xp"), SafeXPEntity.TYPE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("universal_graves", "compass"), GraveCompassComponent.TYPE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("universal_graves", "texture"), IconItem.TEXTURE);
        PolymerComponent.registerDataComponent(GraveCompassComponent.TYPE, IconItem.TEXTURE);
        PolymerEntityUtils.registerType(SafeXPEntity.TYPE);
        GraveBlockEntity.BLOCK_ENTITY_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "universal_graves:grave", FabricBlockEntityTypeBuilder.create(GraveBlockEntity::new, GRAVE_BLOCK).build());
        VisualGraveBlockEntity.BLOCK_ENTITY_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "universal_graves:visual_grave", FabricBlockEntityTypeBuilder.create(VisualGraveBlockEntity::new,VISUAL_GRAVE_BLOCK).build());
        ContainerGraveBlockEntity.BLOCK_ENTITY_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "universal_graves:container_grave", FabricBlockEntityTypeBuilder.create(ContainerGraveBlockEntity::new, CONTAINER_GRAVE_BLOCK).build());
        PolymerBlockUtils.registerBlockEntity(GraveBlockEntity.BLOCK_ENTITY_TYPE, VisualGraveBlockEntity.BLOCK_ENTITY_TYPE, ContainerGraveBlockEntity.BLOCK_ENTITY_TYPE);
        Registry.register(BuiltInRegistries.TICKET_TYPE, "universal_graves:grave", GraveUtils.GRAVE_TICKED);
    }
}
