package eu.pb4.graves;

import com.google.common.base.Suppliers;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;


public final class GraveTextures {
    private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("universal_graves", "has_rp");
    private static final Supplier<Component> DEV_TEXTURE = () -> Component.literal("-1.").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("universal_graves", "gui"))));
    private static final Supplier<Component> TEXTURE = GravesMod.DEV ? DEV_TEXTURE : Suppliers.memoize(DEV_TEXTURE::get);

    public static Component get(ServerPlayer player, Component text) {
        return hasGuiTexture(player) ? Component.empty().append(TEXTURE.get()).append(text) : text;
    }

    public static boolean hasGuiTexture(@Nullable ServerPlayer player) {
        var mata = player != null ? PolymerServerNetworking.getMetadata(player.connection, IDENTIFIER, IntTag.TYPE) : null;
        return PolymerResourcePackUtils.hasMainPack(player)
                || (player != null && player.connection != null && mata != null && mata.intValue() == 1);
    }

    public static void initialize() {
        PolymerServerNetworking.setServerMetadata(IDENTIFIER, IntTag.valueOf(1));
    }
}
