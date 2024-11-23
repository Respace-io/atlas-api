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
import java.util.Objects;

/**
 * Entrypoint for users of the AtlasAPI, used to keep track of your dynamic atlas, provide instructions for generating said atlas, and provide instructions for assembling baked models using that atlas.
 * <p>
 * <p>
 * This class must be implemented and registered (via deferred register of registry {@link AtlasApiRegistry#ASSET_HANDLER_REGISTRY}).
 * Each registered AssetsHandler automatically has a Dynamic Atlas prepared for it.
 * <p>
 * <p>
 * In order for an item to use these sprites in game, the item's model definition must use the <code>atlas_api:dynamic_model</code> geometry loader, and provide the resourcelocation to your registered handler.
 * For example:
 * {@code
 * {
 * "parent": "minecraft:item/generated",
 * "loader": "atlas_api:dynamic_model",
 * "handler": "examplemod:my_handler"
 * }
 * }
 * This then uses your {@code makeBakedModelPreparations} to prepare and bake the models for that item.
 */
public abstract class AssetHandler {
    /**
     * @return List of {@link SpriteSource} to be stitched into an atlas. This is called on the first query of an atlas during gameplay, and cannot be changed until world relog or texture reload.
     * The resource locations you provide here will be the resource locations you must use to access the sprites later
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
     * @return {@link BakingPreparations} to be used to bake a new model
     */
    @NotNull
    public abstract BakingPreparations makeBakedModelPreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed);

    /**
     * @return The location of the dynamic atlas associated with this registered AtlasHandler
     */
    public final ResourceLocation getAtlasLocation() {
        return Objects.requireNonNull(AtlasApiRegistry.ASSET_HANDLER_REGISTRY.getKey(this)).withPrefix("atlas/");
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
