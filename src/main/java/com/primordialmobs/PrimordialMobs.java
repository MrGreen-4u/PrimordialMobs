package com.primordialmobs;

import com.mojang.logging.LogUtils;
import com.primordialmobs.compat.CompatEvents;
import com.primordialmobs.server.config.PMServerConfig;
import com.primordialmobs.server.entity.util.SnifferTaming;
import com.primordialmobs.server.entity.util.TrilocarisSpawns;
import com.primordialmobs.server.event.SnifferEvents;
import com.primordialmobs.server.misc.PMBiomeModifierRegistry;
import com.primordialmobs.server.misc.PMLootTableRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

/**
 * Primordial Mobs — a small add-on over Alex's Caves' Primordial Caves.
 *
 * Alex's Caves is a hard dependency and owns every creature, block, item and asset. This mod only
 * layers its own touches on top: vanilla-style names, the recoloured-variant lottery, taming and
 * riding for the Grazer and the Logger (with their sitting poses), the Sniffer rework, brushing
 * finds in vanilla archaeology, and Trilocaris/Sniffer natural spawns. Nothing of Alex's Caves is
 * duplicated — everything is resolved against its registries by id, or grafted on with small
 * mixins and event handlers.
 */
@Mod(PrimordialMobs.MODID)
public class PrimordialMobs {
    /** The mod id (mods.toml, config file). Distinct from {@link #NAMESPACE}. */
    public static final String MODID = "primordialmobs";
    /**
     * Alex's Caves' registry/asset namespace. Every id this mod resolves (items, entities, biomes,
     * textures) and every asset path it adds to (variant textures, the rename overlay) lives there,
     * because the content it decorates is Alex's Caves' own.
     */
    public static final String NAMESPACE = "alexscaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final PMServerConfig COMMON_CONFIG;
    public static final ForgeConfigSpec COMMON_CONFIG_SPEC;

    static {
        final Pair<PMServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(PMServerConfig::new);
        COMMON_CONFIG = pair.getLeft();
        COMMON_CONFIG_SPEC = pair.getRight();
    }

    @SuppressWarnings("removal")
    public PrimordialMobs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC, PMServerConfig.FILE_NAME);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // The Sniffer may spawn in the Primordial Caves; the Trilocaris placement REPLACES Alex's
        // Caves' own so the Lush Caves water-over-clay rule applies (its rule has no clay check).
        modEventBus.addListener(SnifferTaming::registerSpawnPlacements);
        modEventBus.addListener(TrilocarisSpawns::registerCompatPlacement);
        // Serializers for the brushing loot modifier and the two spawn biome modifiers.
        PMLootTableRegistry.GLOBAL_LOOT_MODIFIER_DEF_REG.register(modEventBus);
        PMBiomeModifierRegistry.DEF_REG.register(modEventBus);
        // The Sniffer rework, and the overlay over Alex's Caves' dinosaurs (variants, taming, goals).
        MinecraftForge.EVENT_BUS.register(new SnifferEvents());
        MinecraftForge.EVENT_BUS.register(new CompatEvents());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.primordialmobs.client.PMClientSetup.init(modEventBus));
    }
}
