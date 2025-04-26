package io.redspace.atlasapi.internal;

import io.redspace.atlasapi.api.AssetHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.model.EmptyModel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A BakedModel that acts as a lazy-loaded holder for a given BakedModelSupplier
 * Supplier is accessed and cached on first itemstack-sensitive render
 * Passes all method calls through to the cached model
 */
public class PassthroughBakedModel implements BakedModel {

    /**
     * Proxy function for resolving via {@link net.minecraft.client.renderer.block.model.ItemOverrides#resolve(BakedModel, ItemStack, ClientLevel, LivingEntity, int)}
     */
    public interface BakedModelSupplier {
        /**
         * Packaged method parameters from {@link net.minecraft.client.renderer.block.model.ItemOverrides#resolve(BakedModel, ItemStack, ClientLevel, LivingEntity, int)}
         */
        record Context(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel,
                       @Nullable LivingEntity pEntity, int pSeed) {
        }

        BakedModel get(Context context);
    }

    boolean rasterized = false;
    BakedModel model;
    ItemOverrides overrides;
    Holder<AssetHandler> handler;

    private void resolveChild(ItemOverrides.BakedOverride override, BakedModelSupplier.Context ctx) {
        if (override.model != null) {
            override.model.getOverrides().resolve(ctx.pModel, ctx.pStack, ctx.pLevel, ctx.pEntity, ctx.pSeed);
        }
    }

    public PassthroughBakedModel(Holder<AssetHandler> handler, BakedModelSupplier modelSupplier, ModelBaker baker) {
        this.model = EmptyModel.BAKED;
        this.handler = handler;
        var blockmodel = (BlockModel) baker.getModel(ModelBakery.MISSING_MODEL_LOCATION);
        BiConsumer<BakedModel, BakedModelSupplier.Context> rasterizeCallback = (baked, ctx) -> {
            // set flag, cache model, and resolve children overrides (ie bow pulling modelstates)
            rasterized = true;
            this.model = baked;
            this.model.getOverrides().getOverrides().forEach(ovr -> resolveChild(ovr, ctx));
        };
        this.overrides = new ItemOverrides(baker, blockmodel,
                List.of()
                , baker.getModelTextureGetter()) {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
                var ctx = new BakedModelSupplier.Context(pModel, pStack, pLevel, pEntity, pSeed);
                // resolve supplier logic
                var model = modelSupplier.get(ctx);
                // iterate through the potential chain of model overrides (ie bow pulling modelstates)
                BakedModel lastModel = null;
                int itr = 8;
                do {
                    if (model == null || --itr == 0) {
                        return lastModel;
                    }
                    lastModel = model;
                    model = model.getOverrides().resolve(model, pStack, pLevel, pEntity, pSeed);
                } while (model != lastModel); // iterate while iterations have an effect
                rasterizeCallback.accept(model, ctx);
                return model;
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
        return model.getQuads(pState, pDirection, pRandom);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return model.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return model.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return model.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return model.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return model.getParticleIcon();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        // if our model is not yet rasterized, call for our customize override object, which will resolve the model
        return rasterized ? model.getOverrides() : overrides;
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return model.getTransforms();
    }

    @Override
    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
        // force textures to be sourced from the given AssetHandler
        return List.of(NeoForgeRenderTypes.getItemLayeredTranslucent(handler.value().getAtlasLocation()));
    }
}
