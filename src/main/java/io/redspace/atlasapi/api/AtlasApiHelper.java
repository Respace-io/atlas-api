package io.redspace.atlasapi.api;

import io.redspace.atlasapi.internal.AtlasHandler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class AtlasApiHelper {
    public static TextureAtlas getAtlas(AssetHandler assetHandler){
        return AtlasHandler.getAtlas(assetHandler);
    }
}
