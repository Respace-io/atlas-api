package io.redspace.atlasapi.internal;

import io.redspace.atlasapi.api.AssetHandler;
import io.redspace.atlasapi.api.AtlasApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class ClientManager implements PreparableReloadListener {
    private static final Map<ResourceLocation, DynamicAtlas> ATLASES = new HashMap<>();
    private static final HashMap<ResourceLocation, HashMap<Integer, BakedModel>> MODEL_CACHE = new HashMap<>();

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        return pPreparationBarrier.wait(null)
                .thenRun(() -> {
                    for (DynamicAtlas atlas : ATLASES.values()) {
                        // If we have already built, rebuild. If not, then the game is still loading and we do nothing
                        if (atlas.hasBuilt) {
                            atlas.buildCustomContents();
                        }
                    }
                    MODEL_CACHE.clear();
                });
    }

    public static BakedModel getModelOrCompute(ResourceLocation handlerId, int modelId, Function<Integer, BakedModel> bakery) {
        //validate registry existance
        if (!AtlasApiRegistry.ASSET_HANDLER_REGISTRY.containsKey(handlerId)) {
            throw new IllegalStateException("Invalid Asset Handler key: " + handlerId);
        } else {
            return MODEL_CACHE.computeIfAbsent(handlerId, rc -> new HashMap<>()).computeIfAbsent(modelId, bakery);
        }
    }

    public static DynamicAtlas getAtlas(AssetHandler assetHandler) {
        return ATLASES.computeIfAbsent(assetHandler.getAtlasLocation(), r -> new DynamicAtlas(assetHandler, Minecraft.getInstance().getTextureManager()));
    }

    public static void clear() {
        ATLASES.values().forEach(DynamicAtlas::reset);
        MODEL_CACHE.clear();
    }
}
