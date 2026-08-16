package eu.pb4.graves.registry;

import com.mojang.authlib.GameProfile;
import eu.pb4.graves.model.GraveModelHandler;
import eu.pb4.graves.other.VisualGraveData;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.*;

import static eu.pb4.graves.registry.AbstractGraveBlock.IS_LOCKED;

public class VisualGraveBlockEntity extends AbstractGraveBlockEntity {
    public static BlockEntityType<VisualGraveBlockEntity> BLOCK_ENTITY_TYPE;
    public BlockState replacedBlockState = Blocks.AIR.defaultBlockState();
    private VisualGraveData visualData = VisualGraveData.DEFAULT;
    protected boolean isPlayerMade = false;
    protected Component[] textOverrides = null;
    private GraveModelHandler model;
    private Map<String, Component> cachedPlaceholders;

    public VisualGraveBlockEntity(BlockPos pos, BlockState state) {
        super(BLOCK_ENTITY_TYPE, pos, state);
    }

    public VisualGraveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public void setVisualData(VisualGraveData data, BlockState oldBlockState) {
        this.replacedBlockState = oldBlockState;
        this.visualData = data;
        this.cachedPlaceholders = null;
        if (this.model != null) {
            this.model.setGrave(this);
        }

        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("BlockState", CompoundTag.CODEC, NbtUtils.writeBlockState(this.replacedBlockState));
        this.visualData.writeData(view.child("VisualData"));
        view.putBoolean("AllowModification", this.isPlayerMade);

        if (this.textOverrides != null) {
            var list = view.list("TextOverride", ComponentSerialization.CODEC);
            for (var text : this.textOverrides) {
                list.add(text);
            }
        }
    }


    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        try {
            this.visualData = VisualGraveData.readData(view.childOrEmpty("VisualData"));
            this.replacedBlockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, (CompoundTag) Objects.requireNonNull(view.read("BlockState", CompoundTag.CODEC).orElse(new CompoundTag())));


            var texts = view.listOrEmpty("TextOverride", ComponentSerialization.CODEC);

            if (!texts.isEmpty()) {
                var textOverrides = new ArrayList<>();
                for (var text : texts) {
                    if (text.getSiblings().isEmpty() && text.getContents() instanceof PlainTextContents.LiteralContents literal
                            && literal.text().length() >= 2 && literal.text().charAt(0) == '"' && literal.text().charAt(literal.text().length() - 1) == '"') {
                        text = Component.literal(literal.text().substring(1, literal.text().length() - 1));
                    }

                    textOverrides.add(text);
                }
                this.textOverrides = textOverrides.toArray(new Component[0]);
            }
        } catch (Exception e) {
            if (this.visualData == null) {
                this.visualData = VisualGraveData.DEFAULT;
            }
        }
        this.cachedPlaceholders = null;
    }

    public static <T extends BlockEntity> void tick(Level world, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof VisualGraveBlockEntity self) || world.isClientSide()) {
            return;
        }

        if (self.model == null) {
            self.model = (GraveModelHandler) BlockBoundAttachment.get(world, pos).holder();
            self.model.setGrave(self);
        }

        self.model.maybeTick(world.getGameTime());
    }

    protected Map<String, Component> createPlaceholders() {
        var placeholder = this.getGrave().getPlaceholders(this.level.getServer());

        if (this.textOverrides != null) {
            placeholder.put("text_1", this.textOverrides[0]);
            placeholder.put("text_2", this.textOverrides[1]);
            placeholder.put("text_3", this.textOverrides[2]);
            placeholder.put("text_4", this.textOverrides[3]);
        } else {
            placeholder.put("text_1", Component.empty());
            placeholder.put("text_2", Component.empty());
            placeholder.put("text_3", Component.empty());
            placeholder.put("text_4", Component.empty());
        }
        return placeholder;
    }

    public VisualGraveData getGrave() {
        return this.visualData;
    }

    @Override
    public VisualGraveData getClientData() {
        return this.visualData;
    }

    @Override
    public void onModelChanged(String model) {
        if (this.model != null) {
            this.model.updateModel();
        }
    }

    public void openEditScreen(ServerPlayer player) {
        var sign = new SignGui(player) {
            @Override
            public void onRemoved() {
                VisualGraveBlockEntity.this.textOverrides = new Component[]{
                        this.getLine(0),
                        this.getLine(1),
                        this.getLine(2),
                        this.getLine(3)
                };
                VisualGraveBlockEntity.this.cachedPlaceholders = null;
                VisualGraveBlockEntity.this.setChanged();
            }
        };
        sign.setSignType(Blocks.BIRCH_SIGN);

        if (this.textOverrides != null) {
            int i = 0;

            for (var text : this.textOverrides) {
                sign.setLine(i, text.copy());
                i++;

                if (i == 4) {
                    break;
                }
            }
        }

        sign.open();
    }

    @Override
    public boolean isGraveProtected() {
        return this.getBlockState().getValue(IS_LOCKED);
    }

    @Override
    public boolean isGraveBroken() {
        return true;
    }

    @Override
    public boolean isGravePlayerMade() {
        return this.isPlayerMade;
    }

    @Override
    public boolean isGravePaymentRequired() {
        return false;
    }

    @Override
    public Component getGravePlaceholder(String id) {
        var x = this.cachedPlaceholders;
        if (x == null) {
            x = this.createPlaceholders();
            this.cachedPlaceholders = x;
        }

        return x.getOrDefault(id, EMPTY_TEXT);
    }

    @Override
    public ResolvableProfile getGraveGameProfile() {
        return this.getGrave().profile();
    }

    @Override
    public ItemStack getGraveSlotItem(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getGraveTaggedItem(Identifier identifier) {
        return ItemStack.EMPTY;
    }

    @Override
    public HumanoidArm getGraveMainArm() {
        return this.getGrave().mainArm();
    }

    @Override
    public byte getGraveSkinModelLayers() {
        return this.getGrave().visualSkinModelLayers();
    }

    @Override
    public void updateModel() {
        if (this.model != null) {
            this.model.updateModel();
        }
    }
}
