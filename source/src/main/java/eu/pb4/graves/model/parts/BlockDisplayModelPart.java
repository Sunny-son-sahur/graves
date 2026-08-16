package eu.pb4.graves.model.parts;

import com.google.gson.annotations.SerializedName;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDisplayModelPart extends DisplayModelPart<BlockDisplayElement, BlockDisplayModelPart> {
    @SerializedName("block_state")
    public BlockState blockState = Blocks.AIR.defaultBlockState();

    @Override
    protected BlockDisplayElement constructBase() {
        return new BlockDisplayElement(this.blockState);
    }

    @Override
    public ModelPartType type() {
        return ModelPartType.BLOCK;
    }
}
