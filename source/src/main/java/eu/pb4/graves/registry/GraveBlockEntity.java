package eu.pb4.graves.registry;

import com.mojang.authlib.GameProfile;
import eu.pb4.graves.GravesMod;
import eu.pb4.graves.config.Config;
import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.grave.Grave;
import eu.pb4.graves.grave.GraveHolder;
import eu.pb4.graves.grave.GraveManager;
import eu.pb4.graves.model.GraveModelHandler;
import eu.pb4.graves.other.VisualGraveData;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static eu.pb4.graves.registry.AbstractGraveBlock.IS_LOCKED;

public class GraveBlockEntity extends AbstractGraveBlockEntity implements GraveHolder {
    public static BlockEntityType<GraveBlockEntity> BLOCK_ENTITY_TYPE;
    public BlockState replacedBlockState = Blocks.AIR.defaultBlockState();
    private Grave data = null;
    private VisualGraveData visualData = VisualGraveData.DEFAULT;
    private long graveId = -1;
    private GraveModelHandler model;
    private int selfDestructTimer = 0;
    private Map<String, Component> cachedPlaceholders;

    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(BLOCK_ENTITY_TYPE, pos, state);
    }

    public void setGrave(Grave grave, BlockState oldBlockState) {
        this.replacedBlockState = oldBlockState;
        this.setGrave(grave);
    }

    public void setGrave(Grave grave) {
        this.data = grave;

        if (grave != null) {
            this.visualData = grave.toVisualGraveData();
            this.graveId = grave.getId();
        } else {
            this.graveId = -1;
        }
        this.selfDestructTimer = 0;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("BlockState", CompoundTag.CODEC, NbtUtils.writeBlockState(this.replacedBlockState));
        this.getClientData().writeData(view.child("VisualData"));
        view.putLong("GraveId", this.graveId);
    }


    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        try {
            this.graveId = view.getLongOr("GraveId", -1);

            this.fetchGraveData();

            if (this.visualData == null) {
                this.visualData = VisualGraveData.readData(view.childOrEmpty("VisualData"));
            }
            this.selfDestructTimer = 0;
            this.replacedBlockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, (CompoundTag) Objects.requireNonNull(view.read(  "BlockState", CompoundTag.CODEC).orElse(new CompoundTag())));
        } catch (Exception e) {
            this.visualData = VisualGraveData.DEFAULT;
        }
    }

    protected void fetchGraveData() {
        this.data = GraveManager.INSTANCE.getId(this.graveId);

        if (this.data != null) {
            this.visualData = this.data.toVisualGraveData();
            this.updateForAllPlayers();
            this.setChanged();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (this.getGrave() != null && this.level != null) {
            this.getGrave().destroyGrave(level.getServer(), null);
        }
    }

    protected void updateForAllPlayers() {
        assert this.level != null;
    }


    public static <T extends BlockEntity> void tick(Level world, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof GraveBlockEntity self) || world.isClientSide()) {
            return;
        }
        self.cachedPlaceholders = null;
        if (self.data == null) {
            if (world.getGameTime() % 10 == 0) {
                self.fetchGraveData();
            } else if (self.selfDestructTimer++ > 10 * 20) {
                GravesMod.LOGGER.warn("Failed to load grave at {}! Removing it!", pos.toShortString());
                world.setBlockAndUpdate(pos, self.replacedBlockState);
            }

            return;
        }

        if (self.data.isRemoved()) {
            self.breakBlock();
            return;
        }

        if (self.model == null) {
            self.model = (GraveModelHandler) BlockBoundAttachment.get(world, pos).holder();
            self.model.setGrave(self);
        }

        if (world.getGameTime() % 5 != 0) {
            self.model.maybeTick(world.getGameTime());
            return;
        }

        Config config = ConfigManager.getConfig();


        if (config.protection.breakingTime > -1 && self.data.shouldNaturallyBreak()) {
            world.setBlock(pos, self.replacedBlockState, Block.UPDATE_ALL);
            return;
        }

        if (state.getValue(IS_LOCKED) && !self.data.isProtected()) {
            world.setBlockAndUpdate(pos, state.setValue(IS_LOCKED, false));
            if (self.model != null) {
                self.model.updateModel();
            }
        }

        self.model.maybeTick(world.getGameTime());
    }

    @Override
    public void onModelChanged(String model) {
        if (this.model != null) {
            this.model.updateModel();
        }
    }

    public void breakBlock() {
        breakBlock(true);
    }

    public void breakBlock(boolean canCreateVisual) {
        assert level != null;
        if (canCreateVisual && ConfigManager.getConfig().placement.createVisualGrave) {
            level.setBlockAndUpdate(worldPosition,  GravesRegistry.VISUAL_GRAVE_BLOCK.withPropertiesOf(this.getBlockState()));

            if (level.getBlockEntity(worldPosition) instanceof VisualGraveBlockEntity blockEntity) {
                blockEntity.setVisualData(this.getClientData(), this.replacedBlockState);
                blockEntity.setModelId(this.getGraveModelId());
            }
        } else {
            level.setBlockAndUpdate(worldPosition, this.replacedBlockState);
        }
    }


    @Nullable
    public Grave getGrave() {
        if (this.data == null) {
            this.fetchGraveData();
        }
        return this.data;
    }

    public VisualGraveData getClientData() {
        return this.data != null ? this.data.toVisualGraveData() : this.visualData;
    }

    @Override
    public void updateModel() {
        if (this.model != null) {
            this.model.updateModel();
        }
    }

    @Override
    public boolean isGraveProtected() {
        return this.getBlockState().getValue(IS_LOCKED);
    }

    @Override
    public boolean isGraveBroken() {
        return false;
    }

    @Override
    public boolean isGravePlayerMade() {
        return false;
    }

    @Override
    public boolean isGravePaymentRequired() {
        return this.getGrave() != null && this.data.isPaymentRequired();
    }

    @Override
    public Component getGravePlaceholder(String id) {
        var x = this.cachedPlaceholders;
        if (x == null) {
            assert this.level != null;
            var server = this.level.getServer();
            x = this.getGrave() != null && server != null ? this.data.getPlaceholders(server) : Map.of();
            this.cachedPlaceholders = x;
        }

        return x.getOrDefault(id, EMPTY_TEXT);
    }

    @Override
    public ResolvableProfile getGraveGameProfile() {
        return this.getGrave() != null ? this.data.getProfileComponent() : Grave.DEFAULT_PROFILE_COMPONENT;
    }

    @Override
    public ItemStack getGraveSlotItem(int i) {
        if (this.getGrave() != null) {
            var items = this.data.getItems();
            if (i < items.size()) {
                return items.get(i).stack();
            }

        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getGraveTaggedItem(Identifier identifier) {
        if (this.getGrave() != null) {
            return this.data.getTaggedItem(identifier);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public HumanoidArm getGraveMainArm() {
        return this.getGrave() != null ? this.data.mainArm() : HumanoidArm.RIGHT;
    }

    @Override
    public byte getGraveSkinModelLayers() {
        return this.getGrave() != null ? this.data.visibleSkinModelParts() : (byte) 0xFF;
    }
}
