package eu.pb4.graves.model.parts;

import com.google.common.collect.Iterables;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import eu.pb4.graves.mixin.LivingEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement;
import eu.pb4.polymer.virtualentity.api.elements.EntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

public class ParticleModelPart extends ModelPart<ParticleModelPart.ParticleElement, ParticleModelPart> {
    @SerializedName("particle")
    public ParticleOptions particleEffect;

    @SerializedName("wait_duration")
    public int waitDuration = 5;

    @SerializedName("delta")
    public Vector3f delta = new Vector3f();

    @SerializedName("speed")
    public float speed = 0;

    @SerializedName("count")
    public int count = 0;

    @Override
    public ParticleElement construct(ServerLevel world) {
        return new ParticleElement(this.particleEffect, this.delta, this.speed, this.count, this.waitDuration);
    }

    @Override
    public ModelPartType type() {
        return ModelPartType.PARTICLE;
    }

    public static class ParticleElement extends AbstractElement {
        private final ParticleOptions particleEffect;
        private final Vector3f delta;
        private final float speed;
        private final int count;
        private final int waitDuration;
        private int tick = 0;
        private Packet<ClientGamePacketListener> packet;

        public ParticleElement(ParticleOptions particleEffect, Vector3f delta, float speed, int count, int waitDuration) {
            this.particleEffect = particleEffect;
            this.delta = delta;
            this.speed = speed;
            this.count = count;
            this.waitDuration = Math.max(waitDuration, 1);
        }

        @Override
        public void setOffset(Vec3 offset) {
            super.setOffset(offset);
            this.packet = null;
        }

        @Override
        public IntList getEntityIds() {
            return IntList.of();
        }

        @Override
        public void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {}

        @Override
        public void stopWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {}

        @Override
        public void notifyMove(Vec3 oldPos, Vec3 currentPos, Vec3 delta) {}

        @Override
        public void tick() {
            if (this.tick++ % this.waitDuration == 0) {
                if (this.packet == null) {
                    var pos = Objects.requireNonNull(this.getHolder()).getPos().add(this.getOffset());
                    this.packet = new ClientboundLevelParticlesPacket(this.particleEffect, false, false, pos.x, pos.y, pos.z, this.delta.x, this.delta.y, this.delta.z, this.speed, this.count);
                }

                Objects.requireNonNull(this.getHolder()).sendPacket(this.packet);
            }

        }
    }
}
