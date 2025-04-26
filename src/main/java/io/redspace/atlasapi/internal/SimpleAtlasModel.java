package io.redspace.atlasapi.internal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.redspace.atlasapi.api.AssetHandler;
import io.redspace.atlasapi.api.AtlasApiRegistry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.HashMap;
import java.util.function.Function;

public class SimpleAtlasModel implements IUnbakedGeometry<SimpleAtlasModel> {
    private final BlockModel unbakedGeometry;
    private final Holder<AssetHandler> handler;

    public SimpleAtlasModel(BlockModel unbakedGeometry, Holder<AssetHandler> handler) {
        this.unbakedGeometry = unbakedGeometry;
        this.handler = handler;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new PassthroughBakedModel(handler,
                ctx -> {
                    var map = new HashMap<>(unbakedGeometry.textureMap); // swap target atlas to the handler's atlas
                    map.forEach((string, mat) -> unbakedGeometry.textureMap.put(string, mat.mapBoth(material -> new Material(handler.value().getAtlasLocation(), material.texture()), Function.identity())));
                    return unbakedGeometry.bake(baker, m -> handler.value().getSprite(m.texture()), modelState);
                }, baker
        );
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        unbakedGeometry.resolveParents(modelGetter);
    }

    public static final class Loader implements IGeometryLoader<SimpleAtlasModel> {
        public static final SimpleAtlasModel.Loader INSTANCE = new SimpleAtlasModel.Loader();

        private Loader() {
        }

        @Override
        public SimpleAtlasModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            try {
                BlockModel baseModel = new BlockModel.Deserializer().deserialize(jsonObject, BlockModel.class, deserializationContext);
                String typestring = jsonObject.get("handler").getAsString();
                Holder<AssetHandler> type = AtlasApiRegistry.ASSET_HANDLER_REGISTRY.getHolderOrThrow(ResourceKey.create(AtlasApiRegistry.ASSET_HANDLER_REGISTRY_KEY, ResourceLocation.parse(typestring)));
                return new SimpleAtlasModel(baseModel, type);
            } catch (Exception e) {
                throw new JsonParseException(e.getMessage());
            }
        }
    }
}