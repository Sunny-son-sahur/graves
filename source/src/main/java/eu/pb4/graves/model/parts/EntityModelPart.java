package eu.pb4.graves.model.parts;

import com.google.common.collect.Iterables;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import eu.pb4.graves.GravesMod;
import eu.pb4.graves.mixin.LivingEntityAccessor;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.EntityElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class EntityModelPart extends ModelPart<EntityElement<?>, EntityModelPart> {
    @SerializedName("entity_type")
    public EntityType<?> entityType;

    @SerializedName("entity_nbt")
    @Nullable
    public CompoundTag nbtCompound;

    @SerializedName("entity_pose")
    @Nullable
    public Pose entityPose;


    @Override
    public EntityElement<?> construct(ServerLevel world) {
        if (entityType == EntityTypes.PLAYER) {
            entityType = EntityTypes.MANNEQUIN;
        }

        var entity = entityType.create(world, EntitySpawnReason.COMMAND);

        if (nbtCompound != null) {
            entity.load(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), this.nbtCompound));
        }

        if (entityPose != null) {
            entity.setPose(this.entityPose);
        }
        var base = new EntityElement<>(entity, world);
        base.setOffset(this.position);
        return base;
    }

    @Override
    public ModelPartType type() {
        return ModelPartType.ENTITY;
    }
}
