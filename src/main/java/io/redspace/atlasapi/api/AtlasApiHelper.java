package io.redspace.atlasapi.api;

import io.redspace.atlasapi.internal.ClientManager;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class AtlasApiHelper {
    public static TextureAtlas getAtlas(AssetHandler assetHandler) {
        return ClientManager.getAtlas(assetHandler);
    }
}
