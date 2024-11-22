package io.redspace.atlasapi;

import com.mojang.logging.LogUtils;
import io.redspace.atlasapi.internal.AtlasHandler;
import io.redspace.atlasapi.internal.DynamicModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import static io.redspace.atlasapi.api.ModelTypeRegistry.MODEL_TYPE_REGISTRY;

@Mod(AtlasApi.MODID)
public class AtlasApi {
    public static final String MODID = "atlas_api";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AtlasApi(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerModelLoader);
        modEventBus.addListener(this::registerClientListeners);
        modEventBus.addListener(this::registerRegistries);
        NeoForge.EVENT_BUS.addListener(this::onLogOut);
    }

    public void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        AtlasHandler.clear();
    }

    public void registerRegistries(NewRegistryEvent event) {
        event.register(MODEL_TYPE_REGISTRY);
    }

    public void registerClientListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new AtlasHandler());
    }

    public void registerModelLoader(ModelEvent.RegisterGeometryLoaders event) {
        event.register(id("dynamic_model"), DynamicModel.Loader.INSTANCE);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AtlasApi.MODID, path);
    }
}
