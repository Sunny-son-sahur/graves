package eu.pb4.graves.registry;

import eu.pb4.graves.GravesMod;
import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.model.ModelDataProvider;
import eu.pb4.graves.other.VisualGraveData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


public abstract class AbstractGraveBlockEntity extends BlockEntity implements ModelDataProvider {
    protected static final Component EMPTY_TEXT = Component.empty();

    private String model = ConfigManager.getConfig().model.defaultModelId;

    public AbstractGraveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putString("GraveModel", this.model);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.model = view.getStringOr("GraveModel", "default");
        this.onModelChanged(this.model);

    }

    @Override
    public String getGraveModelId() {
        return this.model;
    }

    public void setModelId(String model) {
        this.model = model;
        this.onModelChanged(model);
        this.setChanged();
    }



    public abstract void onModelChanged(String model);

    public abstract VisualGraveData getClientData();

    public abstract void updateModel();
}
