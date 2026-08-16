package eu.pb4.graves.model.parts;

import com.google.gson.annotations.SerializedName;
import eu.pb4.graves.config.BaseGson;
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public abstract class ModelPart<T extends AbstractElement, G extends ModelPart<T, G>> {
    @SerializedName("id")
    public @Nullable Identifier id;
    @SerializedName("tags")
    public Set<Identifier> tags = new HashSet<>();
    @SerializedName("rotate_position")
    public boolean rotatePos = true;
    @SerializedName("rotate_yaw")
    public boolean rotateYaw = true;
    @SerializedName("position")
    public Vec3 position = Vec3.ZERO;

    public abstract T construct(ServerLevel world);

    public abstract ModelPartType type();

    public G copy() {
        var gson = BaseGson.getGson(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        // Ugly but quick
        //noinspection unchecked
        return (G) gson.fromJson(gson.toJsonTree(this), this.getClass());
    }
}
