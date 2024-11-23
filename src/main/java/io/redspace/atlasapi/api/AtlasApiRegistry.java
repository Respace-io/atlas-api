package io.redspace.atlasapi.api;

import io.redspace.atlasapi.AtlasApi;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class AtlasApiRegistry {
    public static final ResourceKey<Registry<AssetHandler>> ASSET_HANDLER_REGISTRY_KEY = ResourceKey.createRegistryKey(AtlasApi.id("asset_handler"));
    public static final Registry<AssetHandler> ASSET_HANDLER_REGISTRY = new RegistryBuilder<>(ASSET_HANDLER_REGISTRY_KEY).defaultKey(AtlasApi.id("empty")).create();
}
