package eu.pb4.graves;

import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.graves.compat.GomlCompat;
import eu.pb4.graves.compat.TrinketsUpdatedCompat;
import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.grave.GraveManager;
import eu.pb4.graves.other.Commands;
import eu.pb4.graves.other.GraveProtectionProvider;
import eu.pb4.graves.other.VanillaInventoryMask;
import eu.pb4.graves.registry.*;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.ArrayList;
import java.util.List;

public class GravesMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Universal Graves");
    public static final boolean DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean IS_CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    public static ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("universal-graves").get();

    public static final List<Runnable> DO_ON_NEXT_TICK = new ArrayList<>();

    @Override
    public void onInitialize() {
        CardboardWarning.checkAndAnnounce();
        FabricLoader loader = FabricLoader.getInstance();
        GenericModInfo.build(CONTAINER);

        GravesRegistry.register();
        Commands.register();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((e) -> {
            e.accept(GravesRegistry.CONTAINER_GRAVE_ITEM);
        });

        GraveTextures.initialize();
        GraveGameRules.register();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            PolymerResourcePackUtils.addModAssets("universal-graves");
        }

        CommonProtection.register(Identifier.fromNamespaceAndPath("universal_graves", "graves"), GraveProtectionProvider.INSTANCE);

        GravesApi.registerInventoryMask(Identifier.parse("vanilla"), VanillaInventoryMask.INSTANCE);

        if (loader.isModLoaded("goml")) {
            GomlCompat.register();
        }
        if (loader.isModLoaded("inventorio")) {
            //InventorioCompat.register();
        }

        if (loader.isModLoaded("accessories")) {
            //AccessoriesCompat.register();
        }

        if (loader.isModLoaded("trinkets_updated")) {
            TrinketsUpdatedCompat.register();
        }
        if (loader.isModLoaded("sgod")) {
            //SaveGearOnDeathCompat.register();
        }

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> ConfigManager.loadConfig(server.registryAccess()));
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            GraveManager.INSTANCE = null;
            ConfigManager.clearConfig();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CardboardWarning.checkAndAnnounce();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(((server) -> {
            GraveManager.INSTANCE = server.getDataStorage().computeIfAbsent(GraveManager.getType(server));
            GraveManager.INSTANCE.setServer(server);
        }));


        ServerTickEvents.END_SERVER_TICK.register(s -> {
            GraveManager.INSTANCE.tick(s);

            var copied = new ArrayList<>(DO_ON_NEXT_TICK);
            DO_ON_NEXT_TICK.clear();
            for (var c : copied) {
                try {
                    c.run();
                } catch (Throwable e) {
                    GravesMod.LOGGER.error("Error occurred while executing delayed task!", e);
                }
            }
        });
    }


    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("universal_graves", path);
    }

}
