package io.redspace.atlasapi.api;

import io.redspace.atlasapi.api.data.BakingPreparations;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Entrypoint for users of the AtlasAPI.
 * This class must be implemented and registered (via deferred register of registry {@link AssetHandlerRegistry#ASSET_HANDLER_REGISTRY})
 * Your singleton of this class is used to keep track of your dynamic atlas, provide instructions for generating said atlas, and finally provide instructions for assembling baked models using that atlas.
 */
public abstract class AssetHandler {
    /**
     * @return List of {@link SpriteSource} to be stitched into an atlas. This is called on the first query of an atlas during gameplay, and cannot be changed until world relog or texture reload.
     */
    @NotNull
    public abstract List<SpriteSource> buildSpriteSources();

    /**
     * Called whenever an item which uses the atlas_api:dynamic_model geometry loader is attempting to render itself.
     *
     * @param itemStack    Item stack which is asking for a model
     * @param clientLevel
     * @param livingEntity
     * @param seed
     * @return {@link BakingPreparations} to be used to bake a unique model
     */
    @NotNull
    public abstract BakingPreparations makeBakedModelPreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed);

    /**
     * @return The location of the dynamic atlas associated with this registered AtlasHandler
     */
    public final ResourceLocation getAtlasLocation() {
        return AssetHandlerRegistry.ASSET_HANDLER_REGISTRY.getKey(this);
    }

    /**
     * @return The sprite at a given resource location of this handler's associated atlas
     */
    public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return AtlasApiHelper.getAtlas(this.getAtlasLocation()).getSprite(resourceLocation);
    }

    /**
     * @return A unique id for this set of context when baking a model.
     * Two ids should be equal if and only if their model is identical.
     * If your model is based off an item component, the best id is the component's hash code.
     */
    public abstract int modelId(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed);
}
