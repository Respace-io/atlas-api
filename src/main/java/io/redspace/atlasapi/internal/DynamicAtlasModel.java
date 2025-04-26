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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class DynamicAtlasModel implements IUnbakedGeometry<DynamicAtlasModel> {
    final Holder<AssetHandler> handler;

    public DynamicAtlasModel(Holder<AssetHandler> handler) {
        this.handler = handler;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new PassthroughBakedModel(handler, ctx -> {
            var _handler = handler.value();
            int id = _handler.modelId(ctx.pStack(), ctx.pLevel(),ctx.pEntity(), ctx.pSeed());
            return ClientManager.getModelOrCompute(handler.getKey().location(), id, (i) -> bake(_handler, _handler.makeBakedModelPreparations(ctx.pStack(), ctx.pLevel(),ctx.pEntity(), ctx.pSeed()), context, modelState, overrides));
        }, baker);
    }

    public static BakedModel bake(AssetHandler handler, BakingPreparations preparations, IGeometryBakingContext context, ModelState modelState, ItemOverrides overrides) {
        AtlasApi.LOGGER.debug("JewelryModel bake: {}", preparations);
        var atlas = ClientManager.getAtlas(handler);
        var layers = preparations.layers().stream().sorted(Comparator.comparingInt(ModelLayer::drawOrder)).toList();
        if (!layers.isEmpty()) {
            TextureAtlasSprite particle = atlas.getSprite(layers.getFirst().spriteLocation());
            CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
            Transformation rootTransform = context.getRootTransform();
            for (int i = 0; i < layers.size(); i++) {
                var layer = layers.get(i);

                TextureAtlasSprite sprite = atlas.getSprite(layer.spriteLocation());
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

    public static final class Loader implements IGeometryLoader<DynamicAtlasModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @Override
        public DynamicAtlasModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            try {
                String typestring = jsonObject.get("handler").getAsString();
                Holder<AssetHandler> type = AtlasApiRegistry.ASSET_HANDLER_REGISTRY.getHolderOrThrow(ResourceKey.create(AtlasApiRegistry.ASSET_HANDLER_REGISTRY_KEY, ResourceLocation.parse(typestring)));
                return new DynamicAtlasModel(type);
            } catch (Exception e) {
                throw new JsonParseException(e.getMessage());
            }
        }
    }
}
