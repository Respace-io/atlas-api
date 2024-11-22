package io.redspace.atlasapi.api;

import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class ModelType {

    /**
     * @param sprite         Sprite for this layer
     * @param drawOrder      Order to draw. Lower values are drawn first, and thus appear at the bottom
     * @param transformation If specified, a transformation to be applied to this layer when baked. For example, if using 32x32 textures, you can scale by x2 to preverse vanilla pixel density
     */
    public record Layer(ResourceLocation sprite, int drawOrder, Optional<Transformation> transformation) {
        @Override
        public int hashCode() {
            return transformation.map(Transformation::hashCode).orElse(0) * 31 + sprite.hashCode();
        }
    }

    /**
     * @param layers List of things to be drawn and baked into the final model
     */
    public record BakingPreparations(List<Layer> layers) {
        @Override
        public int hashCode() {
            return layers.hashCode();
        }
    }

    public abstract List<SpriteSource> buildSpriteSources();

    @NotNull
    public abstract BakingPreparations makePreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed);

    public final ResourceLocation getAtlasLocation() {
        return ModelTypeRegistry.MODEL_TYPE_REGISTRY.getKey(this);
    }

    /**
     * @return A unique id for this set of contextual parameters. For example, if the model is based on an item component, the id should be the component's hash code.
     */
    public abstract int modelId(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed);
}