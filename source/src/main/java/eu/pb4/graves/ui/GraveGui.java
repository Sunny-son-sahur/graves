package eu.pb4.graves.ui;

import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.grave.Grave;
import eu.pb4.graves.other.*;
import eu.pb4.graves.registry.GraveCompassItem;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.gui.GuiLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;
import java.util.Set;

import static eu.pb4.graves.GravesMod.id;

public class GraveGui extends PagedGui {
    private final Grave grave;
    private final Container inventory;
    private boolean canTake;
    private final boolean canFetch;
    private final GuiLike previousUi;
    private final boolean canModify;
    private final boolean canTeleport;
    private final boolean hasAccess;
    private int ticker = 0;
    private int actionTimeRemoveProtect = -1;
    private int actionTimeFetch = -1;
    private int currentGraveSize;

    // Enchanted-table ("alt") font identifier — gives the galaxy-brain look
    private static final Identifier ALT_FONT = Identifier.of("minecraft", "alt");

    // Label rendered in enchanted-table font: "TP"
    private static final Component FREE_TP_LABEL = Component.literal("TP")
            .setStyle(Style.EMPTY.withFont(ALT_FONT).withBold(true).withColor(0xFF55FF));

    // Subtitle shown in lore
    private static final Component FREE_TP_LORE = Component.literal("Teleport to your grave instantly")
            .setStyle(Style.EMPTY.withItalic(true).withColor(0xAA00AA));

    public GraveGui(ServerPlayer player, Grave grave, boolean canModify, boolean canFetch) {
        super(player);
        this.grave = grave;
        this.canModify = canModify;
        this.canTeleport = ConfigManager.getConfig().teleportation.cost.type() != GenericCost.Type.CREATIVE || player.isCreative();
        this.hasAccess = grave.hasAccess(player);
        this.canTake = grave.canTakeFrom(player);
        this.canFetch = canFetch;
        this.setTitle(ConfigManager.getConfig().ui.graveTitle.with(grave.getPlaceholders(player.level().getServer())));
        this.inventory = this.grave.asInventory();
        this.currentGraveSize = this.inventory.getContainerSize();
        this.previousUi = SguiUtils.getCurrentGui(player);
        this.updateDisplay();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ContainerInput action) {
        return super.onAnyClick(index, type, action);
    }

    @Override
    public void onTick() {
        if (this.grave.isRemoved()) {
            if (this.previousUi != null) {
                this.previousUi.open();
            } else {
                this.close();
            }
        }

        this.ticker++;
        if (this.actionTimeRemoveProtect <= this.ticker) {
            this.actionTimeRemoveProtect = -1;
        }

        if (this.currentGraveSize != this.inventory.getContainerSize()) {
            this.currentGraveSize = this.inventory.getContainerSize();
            this.updateDisplay();
        } else if (this.ticker % 20 == 0) {
            if (this.canTake) {
                this.grave.tryBreak(this.player.level().getServer(), this.player);
            }
            this.updateDisplay();
        }
        super.onTick();
    }

    @Override
    public void onRemoved() {
        this.grave.updateDisplay();
        this.grave.updateSelf(this.player.level().getServer());
        super.onRemoved();
    }

    @Override
    protected int getPageAmount() {
        return this.grave.getItems().size() / PAGE_SIZE + 1;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if (id < this.inventory.getContainerSize()) {
            return GuiSlot.of(new OutputSlot(inventory, id, 0, 0, this.canModify && this.canTake));
        }
        return GuiSlot.empty();
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        var config = ConfigManager.getConfig();

        return switch (id) {
            case 0 -> {
                var placeholders = grave.getPlaceholders(this.player.level().getServer());

                yield GuiSlot.of(ConfigManager.getConfig().ui.graveInfoIcon.get(this.grave.isProtected())
                        .builder(placeholders)
                        .setCallback(() -> {
                            var cursor = this.player.containerMenu.getCarried();
                            if (!cursor.isEmpty() && cursor.is(Items.COMPASS)) {
                                cursor.shrink(1);
                                player.getInventory().placeItemBackInInventory(GraveCompassItem.create(this.grave.getId(), true));
                            }
                        })
                );
            }
            case 1 -> getUnlockGrave();
            case 2 -> getRemoveProtection();

            // ── Slot 3: Free instant TP — no cost, no timer, no permission check ─
            case 3 -> getFreeInstantTeleport();

            case 4 -> this.canFetch ?
                    GuiSlot.of(this.actionTimeFetch != -1 ? ConfigManager.getConfig().ui.fetchButton.get(false).builder()
                            .setCallback(() -> {
                                playClickSound(player);
                                this.actionTimeFetch = -1;
                                if (!this.grave.moveTo(player.level().getServer(), Location.fromEntity(player))) {
                                    return;
                                }
                                this.close();
                            }) : ConfigManager.getConfig().ui.fetchButton.get(true).builder()
                            .setCallback(() -> {
                                playClickSound(player);
                                this.actionTimeFetch = this.ticker + 20 * 5;
                                this.updateDisplay();
                            })
                    ) : GuiSlot.lowerBar(player);
            case 5 -> GuiSlot.previousPage(this);
            case 6 -> this.previousUi != null ? GuiSlot.nextPage(this) : GuiSlot.lowerBar(player);
            case 7 -> this.previousUi == null ? GuiSlot.nextPage(this) : GuiSlot.lowerBar(player);
            case 8 -> this.previousUi != null ? GuiSlot.back(this.previousUi::open) : GuiSlot.lowerBar(player);
            default -> GuiSlot.lowerBar(player);
        };
    }

    /**
     * Free Instant Teleport button.
     *
     * Rules:
     *  - Always visible to the grave owner (hasAccess).
     *  - No cost, no permission node, no OP required.
     *  - Teleports instantly (0 tick delay) regardless of creative/survival.
     *  - Works cross-dimension.
     *  - Uses enchanted-table ("alt") font for the label.
     */
    private GuiSlot getFreeInstantTeleport() {
        // Only show to players who own / have access to this grave
        if (!this.hasAccess) {
            return GuiSlot.lowerBar(player);
        }

        // Build the button stack — Ender Pearl with enchanted-table "TP" name
        var tpStack = new ItemStack(Items.ENDER_PEARL);
        tpStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, FREE_TP_LABEL);
        tpStack.set(net.minecraft.core.component.DataComponents.LORE,
                new net.minecraft.world.item.component.ItemLore(java.util.List.of(FREE_TP_LORE)));
        // Give it enchant-glint so it looks special
        tpStack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return GuiSlot.of(tpStack, () -> {
            playClickSound(this.player);
            this.close();
            instantTeleportToGrave(this.player, this.grave);
        });
    }

    /**
     * Instant teleport — no delay, no cost, no permission check, cross-dimension.
     * Bypasses the config teleportation.cost and teleportation.teleportTime entirely.
     */
    private static void instantTeleportToGrave(ServerPlayer player, Grave grave) {
        var pos = grave.getLocation();
        MinecraftServer server = Objects.requireNonNull(player.level().getServer(), "server is null");
        ServerLevel world = server.getLevel(ResourceKey.create(Registries.DIMENSION, pos.world()));

        if (world == null) {
            // Dimension doesn't exist / isn't loaded — send a chat message and bail
            player.sendSystemMessage(Component.literal("§cCouldn't find that dimension!")
                    .setStyle(Style.EMPTY.withItalic(false)));
            return;
        }

        double x = pos.x() + 0.5;
        double y = pos.y() + 1.0; // stand on top of the grave block
        double z = pos.z() + 0.5;

        // teleportTo handles cross-dimension relocation natively
        player.teleportTo(world, x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);

        // Brief invulnerability so they don't take fall damage on arrival
        ((PlayerAdditions) player).graves$setInvulnerable(true);

        // Schedule invuln removal after 40 ticks (2 seconds)
        eu.pb4.graves.GravesMod.DO_ON_NEXT_TICK.add(new Runnable() {
            int ticks = 40;
            @Override
            public void run() {
                if (--ticks > 0) {
                    eu.pb4.graves.GravesMod.DO_ON_NEXT_TICK.add(this);
                } else {
                    ((PlayerAdditions) player).graves$setInvulnerable(false);
                }
            }
        });

        // Play the enderman teleport sound on arrival
        player.connection.send(new ClientboundSoundEntityPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENDERMAN_TELEPORT),
                SoundSource.MASTER, player, 1f, 1f, player.getRandom().nextLong()
        ));

        // Notify the player
        player.sendSystemMessage(
                Component.literal("§5Teleported to your grave at §d" + pos.x() + ", " + pos.y() + ", " + pos.z())
                        .setStyle(Style.EMPTY.withItalic(false))
        );
    }

    private GuiSlot getUnlockGrave() {
        var config = ConfigManager.getConfig();
        if (this.grave.isPaymentRequired() && (config.interactions.allowRemoteGraveUnlocking || FabricPermissionBridge.checkPermission(player, id("can_unlock_remotely"), PermissionLevel.ADMINS))) {
            return GuiSlot.of(ConfigManager.getConfig().ui.unlockButton.get(config.interactions.cost.checkCost(player))
                    .builder(ConfigManager.getConfig().interactions.cost.getPlaceholders())
                    .setCallback(() -> {
                        if (this.grave.payForUnlock(player)) {
                            this.canTake = this.grave.canTakeFrom(player);
                            playClickSound(this.player);
                            this.updateDisplay();
                        } else {
                            playClickSound(this.player, SoundEvents.VILLAGER_NO);
                        }
                    }));
        }
        return GuiSlot.lowerBar(player);
    }

    private GuiSlot getRemoveProtection() {
        var config = ConfigManager.getConfig();
        if (this.grave.isProtected() && (this.hasAccess && (config.interactions.allowRemoteProtectionRemoval || FabricPermissionBridge.checkPermission(player, id("can_remove_protection_remotely"), PermissionLevel.ADMINS)))) {
            if (this.actionTimeRemoveProtect != -1) {
                return GuiSlot.of(config.ui.removeProtectionButton.get(false).builder()
                        .setCallback(() -> {
                            playClickSound(player);
                            this.grave.disableProtection();
                            this.actionTimeRemoveProtect = -1;
                            this.updateDisplay();
                        })
                );
            } else {
                return GuiSlot.of(config.ui.removeProtectionButton.get(true).builder()
                        .setCallback(() -> {
                            playClickSound(player);
                            this.actionTimeRemoveProtect = this.ticker + 20 * 5;
                            this.updateDisplay();
                        })
                );
            }
        }

        if (this.canModify || (this.canTake && (config.interactions.allowRemoteGraveBreaking || FabricPermissionBridge.checkPermission(player, id("can_break_remotely"), PermissionLevel.ADMINS)))) {
            if (this.actionTimeRemoveProtect != -1) {
                return GuiSlot.of(config.ui.breakGraveButton.get(false).builder()
                        .setCallback(() -> {
                            playClickSound(player);
                            this.grave.destroyGrave(this.player.level().getServer(), this.player);
                            this.actionTimeRemoveProtect = -1;
                            this.close();
                        })
                );
            } else {
                return GuiSlot.of(config.ui.breakGraveButton.get(true).builder()
                        .setCallback(() -> {
                            playClickSound(player);
                            this.actionTimeRemoveProtect = this.ticker + 20 * 5;
                            this.updateDisplay();
                        })
                );
            }
        }
        return GuiSlot.lowerBar(player);
    }
}
