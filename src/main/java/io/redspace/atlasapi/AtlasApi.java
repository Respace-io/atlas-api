package io.redspace.atlasapi;

import com.mojang.logging.LogUtils;
import io.redspace.atlasapi.internal.ClientManager;
import io.redspace.atlasapi.internal.DynamicAtlasModel;
import io.redspace.atlasapi.internal.SimpleAtlasModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import static io.redspace.atlasapi.api.AtlasApiRegistry.ASSET_HANDLER_REGISTRY;

@Mod(AtlasApi.MODID)
public class AtlasApi {
    public static final String MODID = "atlas_api";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AtlasApi(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerModelLoader);
        modEventBus.addListener(this::registerClientListeners);
        modEventBus.addListener(this::registerRegistries);
        NeoForge.EVENT_BUS.addListener(this::onLogOut);
        NeoForge.EVENT_BUS.addListener(this::onLogIn);
    }

    public void onLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientManager.clear();
    }

    public void onLogIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ASSET_HANDLER_REGISTRY.stream().forEach(handler -> ClientManager.getAtlas(handler).buildCustomContents());
    }

    public void registerRegistries(NewRegistryEvent event) {
        event.register(ASSET_HANDLER_REGISTRY);
    }

    public void registerClientListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ClientManager());
    }

    public void registerModelLoader(ModelEvent.RegisterGeometryLoaders event) {
        event.register(id("dynamic_model"), DynamicAtlasModel.Loader.INSTANCE);
        event.register(id("simple_model"), SimpleAtlasModel.Loader.INSTANCE);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AtlasApi.MODID, path);
    }
}
