package io.redspace.atlasapi.internal;

import io.redspace.atlasapi.AtlasApi;
import io.redspace.atlasapi.api.AssetHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class DynamicAtlas extends TextureAtlas {
    public boolean hasBuilt = false;

    public void reset() {
        if (hasBuilt) {
            clearTextureData();
            hasBuilt = false;
        }
    }

    final AssetHandler handler;

    public DynamicAtlas(AssetHandler handler, TextureManager pTextureManager) {
        super(handler.getAtlasLocation());
        this.handler = handler;
        pTextureManager.register(this.location(), this);
    }

    public void buildCustomContents() {
        AtlasApi.LOGGER.info("Atlas {}: Building custom contents start", this.location());
        var loader = SpriteLoader.create(this);
        SpriteResourceLoader spriteresourceloader = SpriteResourceLoader.create(SpriteLoader.DEFAULT_METADATA_SECTIONS);
        var sources = new SpriteSourceList(handler.buildSpriteSources());
        var factories = sources.list(Minecraft.getInstance().getResourceManager());
        List<SpriteContents> contents = factories.stream().map(factory -> factory.apply(spriteresourceloader)).filter(Objects::nonNull).toList();
        var preparations = loader.stitch(contents, 0, Runnable::run);
        this.upload(preparations);
        AtlasApi.LOGGER.info("Atlas {}: Building custom contents finish ({} sprites)", this.location(), preparations.regions().size());
        hasBuilt = true;
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation pLocation) {
        if (!hasBuilt) {
            buildCustomContents();
            hasBuilt = true;
        }
        return super.getSprite(pLocation);
    }


}
