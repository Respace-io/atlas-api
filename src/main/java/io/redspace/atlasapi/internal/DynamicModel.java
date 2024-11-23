package io.redspace.atlasapi.internal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import io.redspace.atlasapi.AtlasApi;
import io.redspace.atlasapi.api.AssetHandler;
import io.redspace.atlasapi.api.AtlasApiRegistry;
import io.redspace.atlasapi.api.data.BakingPreparations;
import io.redspace.atlasapi.api.data.ModelLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DynamicModel implements IUnbakedGeometry<DynamicModel> {
    final AssetHandler handler;

    public DynamicModel(AssetHandler handler) {
        this.handler = handler;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new BakedHolder(handler, context, baker, modelState);
    }

    public static class BakedHolder implements BakedModel {
        BakedModel model;
        ItemOverrides overrides;

        public BakedHolder(AssetHandler handler, IGeometryBakingContext context, ModelBaker baker, ModelState modelState) {
            this.model = Minecraft.getInstance().getModelManager().getMissingModel();
            var blockmodel = (BlockModel) baker.getModel(ModelBakery.MISSING_MODEL_LOCATION);
            this.overrides = new ItemOverrides(baker, blockmodel,
                    List.of()
                    , baker.getModelTextureGetter()) {
                @Nullable
                @Override
                public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
                    int id = handler.modelId(pStack, pLevel, pEntity, pSeed);
                    return AtlasHandler.MODEL_CACHE.computeIfAbsent(id, (i) -> bake(handler, handler.makeBakedModelPreparations(pStack, pLevel, pEntity, pSeed), context, modelState, new ItemOverrides(baker, blockmodel, List.of(), baker.getModelTextureGetter())));
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
        public TextureAtlasSprite getParticleIcon() {
            return model.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }

    public static BakedModel bake(AssetHandler handler, BakingPreparations preparations, IGeometryBakingContext context, ModelState modelState, ItemOverrides overrides) {
        AtlasApi.LOGGER.debug("JewelryModel bake: {}", preparations);
        var atlas = AtlasHandler.getAtlas(handler.getAtlasLocation());
        var layers = preparations.layers().stream().sorted(Comparator.comparingInt(ModelLayer::drawOrder)).toList();
        if (!layers.isEmpty()) {
            TextureAtlasSprite particle = atlas.getSprite(layers.getFirst().sprite());
            CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
            Transformation rootTransform = context.getRootTransform();
            for (int i = 0; i < layers.size(); i++) {
                var layer = layers.get(i);

                TextureAtlasSprite sprite = atlas.getSprite(layer.sprite());
                Transformation transformation = layer.transformation().orElse(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(), new Vector3f(1, 1, 1),
                        new Quaternionf())
                );

                ModelState subState = new SimpleModelState(modelState.getRotation().compose(rootTransform.compose(transformation)), modelState.isUvLocked());

                List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite);
                List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(unbaked, (material2) -> sprite, subState);
                RenderTypeGroup renderTypes = new RenderTypeGroup(RenderType.solid(), NeoForgeRenderTypes.getUnsortedTranslucent(handler.getAtlasLocation()));

                builder.addQuads(renderTypes, quads);
            }
            return builder.build();
        }
        return EmptyModel.BAKED;

    }

    public static final class Loader implements IGeometryLoader<DynamicModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @Override
        public DynamicModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            try {
                String typestring = jsonObject.get("handler").getAsString();
                AssetHandler type = Objects.requireNonNull(AtlasApiRegistry.ASSET_HANDLER_REGISTRY.get(ResourceLocation.parse(typestring)));
                return new DynamicModel(type);
            } catch (Exception e) {
                throw new JsonParseException(e.getMessage());
            }
        }
    }
}
