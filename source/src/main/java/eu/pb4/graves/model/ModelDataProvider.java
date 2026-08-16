package eu.pb4.graves.model;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public interface ModelDataProvider {
    String getGraveModelId();
    boolean isGraveProtected();
    boolean isGraveBroken();
    boolean isGravePlayerMade();
    boolean isGravePaymentRequired();
    Component getGravePlaceholder(String id);
    ResolvableProfile getGraveGameProfile();
    ItemStack getGraveSlotItem(int i);
    ItemStack getGraveTaggedItem(Identifier identifier);
    HumanoidArm getGraveMainArm();
    byte getGraveSkinModelLayers();
}
