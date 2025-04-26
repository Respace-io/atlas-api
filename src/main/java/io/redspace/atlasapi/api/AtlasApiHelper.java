package io.redspace.atlasapi.api;

import io.redspace.atlasapi.internal.ClientManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class AtlasApiHelper {
    public static class Constants {
        public static final String ATLAS_API_MODID = "atlas_api";
        public static final ResourceLocation DYNAMIC_MODEL_LOADER = ResourceLocation.fromNamespaceAndPath(ATLAS_API_MODID, "dynamic_model");
        public static final ResourceLocation SIMPLE_MODEL_LOADER = ResourceLocation.fromNamespaceAndPath(ATLAS_API_MODID, "simple_model");
    }

    public static TextureAtlas getAtlas(AssetHandler assetHandler) {
        return ClientManager.getAtlas(assetHandler);
    }
}
