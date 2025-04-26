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
import net.neoforged.neoforge.client.model.EmptyModel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class PassthroughBakedModel implements BakedModel {
    public interface BakedModelSupplier {
        BakedModel get(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed);
    }

    boolean rasterized = false;
    BakedModel model;
    ItemOverrides overrides;
    Holder<AssetHandler> handler;

    public PassthroughBakedModel(Holder<AssetHandler> handler, BakedModelSupplier modelSupplier, ModelBaker baker) {
        this.model = EmptyModel.BAKED;
        this.handler = handler;
        var blockmodel = (BlockModel) baker.getModel(ModelBakery.MISSING_MODEL_LOCATION);
        Consumer<BakedModel> rasterizeCallback = (baked) -> {
            this.model = baked;
            rasterized = true;
        };
        this.overrides = new ItemOverrides(baker, blockmodel,
                List.of()
                , baker.getModelTextureGetter()) {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
//                AtlasApi.LOGGER.debug("{} BRRRRT", pEntity == null ? -1 : pEntity.tickCount);
                var model = modelSupplier.get(pModel, pStack, pLevel, pEntity, pSeed);
                BakedModel lastModel = null;
                int itr = 8;
                do {
                    if (model == null || --itr == 0) {
                        return lastModel;
                    }
                    lastModel = model;
                    model = model.getOverrides().resolve(model, pStack, pLevel, pEntity, pSeed);
                } while (model != lastModel);
                rasterizeCallback.accept(model);
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
        return rasterized ? model.getOverrides() : overrides;
    }

    @Override
    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
        return List.of(RenderType.entityCutout(handler.value().getAtlasLocation()));
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return model.getTransforms();
    }
}
