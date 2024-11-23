package io.redspace.atlasapi.api.data;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * @param sprite         Sprite for this layer
 * @param drawOrder      Order to draw. Lower values are drawn first, and thus appear at the bottom
 * @param transformation If specified, a transformation to be applied to this layer when baked. For example, if using 32x32 textures, you can scale by x2 to preverse vanilla pixel density
 */
public record ModelLayer(ResourceLocation sprite, int drawOrder, Optional<Transformation> transformation) {
}