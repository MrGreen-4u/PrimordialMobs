package com.primordialmobs;

import com.primordialmobs.client.ClientProxy;
import com.primordialmobs.client.config.PMClientConfig;
import com.primordialmobs.client.particle.PMParticleRegistry;
import com.primordialmobs.server.CommonProxy;
import com.primordialmobs.server.block.PMBlockRegistry;
import com.primordialmobs.server.config.PMServerConfig;
import com.primordialmobs.server.entity.PMEntityRegistry;
import com.primordialmobs.server.event.CommonEvents;
import com.primordialmobs.server.event.SnifferEvents;
import com.primordialmobs.server.entity.util.SnifferTaming;
import com.primordialmobs.server.misc.PMBiomeModifierRegistry;
import com.primordialmobs.server.item.PMItemRegistry;
import com.primordialmobs.server.level.feature.PMFeatureRegistry;
import com.primordialmobs.server.message.MountedEntityKeyMessage;
import com.primordialmobs.server.message.UpdateEffectVisualityEntityMessage;
import com.primordialmobs.server.misc.PMAdvancementTriggerRegistry;
import com.primordialmobs.server.misc.PMCreativeTabRegistry;
import com.primordialmobs.server.misc.PMLootTableRegistry;
import com.primordialmobs.server.misc.PMSoundRegistry;
import com.primordialmobs.server.potion.PMEffectRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Mod(PrimordialMobs.MODID)
public class PrimordialMobs {
    /**
     * The mod id (mods.toml, config files, network channel). Distinct from {@link #NAMESPACE} so this
     * mod can coexist with the full Alex's Caves mod, whose mod id is "alexscaves".
     */
    public static final String MODID = "primordialmobs";
    /**
     * Registry/asset namespace. Kept as "alexscaves" so existing worlds keep loading and, when the full
     * Alex's Caves mod is installed, our data (loot modifier entries, lang keys, biome checks) resolves
     * against its identically-named content instead of ours.
     */
    public static final String NAMESPACE = "alexscaves";
    /**
     * True when the full Alex's Caves mod is present. In that case we register NO content of our own
     * (Alex's Caves provides every "alexscaves:" object) and act as a compatibility overlay instead:
     * renamed lang entries, replacement dino renderers with the recolored-variant textures, the brushing
     * loot modifier, the Sniffer changes and the natural Sniffer spawns in the Primordial Caves.
     */
    public static final boolean ALEXS_CAVES_INSTALLED = net.minecraftforge.fml.loading.FMLLoader.getLoadingModList().getModFileById("alexscaves") != null;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static CommonProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final ResourceLocation PACKET_NETWORK_NAME = new ResourceLocation(PrimordialMobs.MODID, "main_channel");
    public static final SimpleChannel NETWORK_WRAPPER = NetworkRegistry.ChannelBuilder
            .named(PACKET_NETWORK_NAME)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    public static final PMServerConfig COMMON_CONFIG;
    private static final ForgeConfigSpec COMMON_CONFIG_SPEC;
    public static final PMClientConfig CLIENT_CONFIG;
    private static final ForgeConfigSpec CLIENT_CONFIG_SPEC;

    static {
        final Pair<PMServerConfig, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(PMServerConfig::new);
        COMMON_CONFIG = serverPair.getLeft();
        COMMON_CONFIG_SPEC = serverPair.getRight();
        final Pair<PMClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(PMClientConfig::new);
        CLIENT_CONFIG = clientPair.getLeft();
        CLIENT_CONFIG_SPEC = clientPair.getRight();
    }

    @SuppressWarnings("removal")
    public PrimordialMobs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC, "primordialmobs-general.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG_SPEC, "primordialmobs-client.toml");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(SnifferTaming::registerSpawnPlacements);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new SnifferEvents());
        // The brushing loot modifier and the Primordial Caves sniffer-spawn modifier work in both modes:
        // their item/entity references are resolved by id, so with Alex's Caves installed they hit its content.
        PMLootTableRegistry.GLOBAL_LOOT_MODIFIER_DEF_REG.register(modEventBus);
        PMBiomeModifierRegistry.DEF_REG.register(modEventBus);
        if (ALEXS_CAVES_INSTALLED) {
            // Alex's Caves registers every "alexscaves:" object itself; registering ours too would crash
            // with duplicate ids (and its cave_creature MobCategory must only be created once).
            MinecraftForge.EVENT_BUS.register(new com.primordialmobs.compat.CompatEvents());
            // Alex's Caves registers its own Trilocaris spawn placement (no clay requirement), so ours has
            // to REPLACE it for the Lush Caves rule to apply. Standalone this lives in PMEntityRegistry.
            modEventBus.addListener(com.primordialmobs.server.entity.util.TrilocarisSpawns::registerCompatPlacement);
        } else {
            MinecraftForge.EVENT_BUS.register(new CommonEvents());
            PMBlockRegistry.DEF_REG.register(modEventBus);
            PMItemRegistry.DEF_REG.register(modEventBus);
            PMParticleRegistry.DEF_REG.register(modEventBus);
            PMEntityRegistry.DEF_REG.register(modEventBus);
            modEventBus.addListener(PMEntityRegistry::initializeAttributes);
            modEventBus.addListener(PMEntityRegistry::spawnPlacements);
            PMFeatureRegistry.DEF_REG.register(modEventBus);
            PMSoundRegistry.DEF_REG.register(modEventBus);
            PMEffectRegistry.DEF_REG.register(modEventBus);
            PMCreativeTabRegistry.DEF_REG.register(modEventBus);
        }
        PROXY.commonInit();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PROXY.initPathfinding();
        int packetsRegistered = 0;
        NETWORK_WRAPPER.registerMessage(packetsRegistered++, MountedEntityKeyMessage.class, MountedEntityKeyMessage::write, MountedEntityKeyMessage::read, MountedEntityKeyMessage::handle);
        NETWORK_WRAPPER.registerMessage(packetsRegistered++, UpdateEffectVisualityEntityMessage.class, UpdateEffectVisualityEntityMessage::write, UpdateEffectVisualityEntityMessage::read, UpdateEffectVisualityEntityMessage::handle);
        if (!ALEXS_CAVES_INSTALLED) {
            event.enqueueWork(() -> {
                PMItemRegistry.setup();
                PMAdvancementTriggerRegistry.setup();
            });
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.clientInit());
    }

    public static <MSG> void sendMSGToServer(MSG message) {
        NETWORK_WRAPPER.sendToServer(message);
    }

    public static <MSG> void sendMSGToAll(MSG message) {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }

    public static <MSG> void sendNonLocal(MSG msg, ServerPlayer player) {
        NETWORK_WRAPPER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
