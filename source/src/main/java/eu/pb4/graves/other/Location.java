package eu.pb4.graves.other;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record Location(Identifier world, BlockPos blockPos) {
    public int x() { return this.blockPos.getX(); }

    public int y() {
        return this.blockPos.getY();
    }

    public int z() {
        return this.blockPos.getZ();
    }

    public void writeData(ValueOutput view) {
        view.putIntArray("Position", new int[]{this.x(), this.y(), this.z()});
        view.putString("World", this.world().toString());
    }

    public void writeData(CompoundTag view) {
        view.putIntArray("Position", new int[]{this.x(), this.y(), this.z()});
        view.putString("World", this.world().toString());
    }

    public static Location readData(ValueInput view) {
        int[] pos = view.getIntArray("Position").orElse(new int[0]);
        return new Location(Identifier.tryParse(view.getStringOr("World", "")), new BlockPos(pos[0], pos[1], pos[2]));
    }

    public static Location readData(CompoundTag view) {
        int[] pos = view.getIntArray("Position").orElse(new int[0]);
        return new Location(Identifier.tryParse(view.getStringOr("World", "")), new BlockPos(pos[0], pos[1], pos[2]));
    }

    public static Location fromEntity(ServerPlayer player) {
        return new Location(player.level().dimension().identifier(), player.blockPosition());
    }

    public GlobalPos asGlobalPos() {
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION, this.world), this.blockPos);
    }

    public Location withPos(BlockPos pos) {
        return new Location(world, pos);
    }
}
