package io.redspace.atlasapi.api;

import io.redspace.atlasapi.internal.AtlasHandler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class AtlasApiHelper {
    public static TextureAtlas getAtlas(ResourceLocation resourceLocation) {
        return AtlasHandler.getAtlas(resourceLocation);
    }
}
