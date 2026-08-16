package eu.pb4.graves.registry;

import eu.pb4.graves.config.ConfigManager;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class GraveGameRules {
    public static final GameRule<Integer> PROTECTION_TIME =
            register(Identifier.parse("universal_graves:protection_time"), GameRuleBuilder.forInteger(-2).minValue(-2).category(GameRuleCategory.PLAYER));
    public static final GameRule<Integer> BREAKING_TIME = register(Identifier.parse("universal_graves:breaking_time"), GameRuleBuilder.forInteger(-2).minValue(-2).category(GameRuleCategory.PLAYER));


    private static <T> GameRule<T> register(Identifier identifier, GameRuleBuilder<T> t) {
        return Registry.register(BuiltInRegistries.GAME_RULE, identifier, t.build());
    }

    public static int getProtectionTime(MinecraftServer server) {
        var rule = server.getGameRules().get(PROTECTION_TIME);

        if (rule == -2) {
            return ConfigManager.getConfig().protection.protectionTime;
        } else {
            return rule;
        }
    }

    public static int getBreakingTime(MinecraftServer server) {
        var rule = server.getGameRules().get(BREAKING_TIME);

        if (rule == -2) {
            return ConfigManager.getConfig().protection.breakingTime;
        } else {
            return rule;
        }
    }

    public static void register() {

    }
}
