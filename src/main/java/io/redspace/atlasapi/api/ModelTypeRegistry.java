package io.redspace.atlasapi.api;

import io.redspace.atlasapi.AtlasApi;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModelTypeRegistry {
    public static final ResourceKey<Registry<ModelType>> MODEL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(AtlasApi.id("model_type"));
    public static final Registry<ModelType> MODEL_TYPE_REGISTRY = new RegistryBuilder<>(MODEL_TYPE_REGISTRY_KEY).defaultKey(AtlasApi.id("empty")).create();
}
