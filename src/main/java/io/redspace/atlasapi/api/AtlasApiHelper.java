package io.redspace.atlasapi.api;

import io.redspace.atlasapi.internal.AtlasHandler;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class AtlasApiHelper {
    public static TextureAtlas getAtlas(AssetHandler assetHandler) {
        return AtlasHandler.getAtlas(assetHandler);
    }
}
