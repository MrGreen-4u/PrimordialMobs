package com.primordialmobs.server.config;

import com.primordialmobs.PrimordialMobs;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The mod's one config file ({@code config/primordialmobs-general.toml}).
 *
 * Every option is an add-on knob over content Alex's Caves owns; nothing here configures Alex's
 * Caves itself. Registered as COMMON so both the client (which needs {@code rename_mobs} for the
 * resource-pack overlay) and the server (which rolls variants and brushing loot) read it from the
 * same file.
 */
public class PMServerConfig {

    public static final String FILE_NAME = "primordialmobs-general.toml";

    public final ForgeConfigSpec.BooleanValue renameMobs;
    public final ForgeConfigSpec.BooleanValue alternativeTextures;
    public final ForgeConfigSpec.DoubleValue alternativeTextureChance;
    public final ForgeConfigSpec.DoubleValue brushingRelicChance;
    public final ForgeConfigSpec.DoubleValue brushingEggChance;

    public PMServerConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("renames");
        renameMobs = builder
                .comment("Whether the Primordial Caves creatures and everything named after them use this mod's",
                        "vanilla-style names (Grazer, Logger, Roarer, Drifter, Stealer, Rammer, Scorcher,",
                        "Roarerzilla) - including spawn eggs, egg blocks, advancements, sound subtitles and the",
                        "Alex's Caves guide book entries, in all 13 supported languages.",
                        "Applies on the next game start or resource reload (F3+T).")
                .translation("primordialmobs.config.rename_mobs")
                .define("rename_mobs", true);
        builder.pop();
        builder.push("alternative-textures");
        alternativeTextures = builder
                .comment("Whether newly arriving animals (hatched, bred, spawned or summoned) can roll this",
                        "mod's exclusive recolored look. Animals that already rolled one keep it.")
                .translation("primordialmobs.config.alternative_textures")
                .define("enabled", true);
        alternativeTextureChance = builder
                .comment("The chance (0.0-1.0) for a newly arriving animal to carry the recolored look.")
                .translation("primordialmobs.config.alternative_texture_chance")
                .defineInRange("chance", 0.15D, 0.0D, 1.0D);
        builder.pop();
        builder.push("brushing");
        brushingRelicChance = builder
                .comment("The chance (0.0-1.0) that brushing a vanilla suspicious sand/gravel block turns up a",
                        "Primordial Caves relic from Alex's Caves (Heavy Bone, Tectonic Shard, Amber Curiosity",
                        "or one of the four pottery sherds). A successful vanilla Sniffer Egg roll is never",
                        "replaced.")
                .translation("primordialmobs.config.brushing_relic_chance")
                .defineInRange("relic_chance", 0.08D, 0.0D, 1.0D);
        brushingEggChance = builder
                .comment("The chance (0.0-1.0) that brushing a vanilla suspicious sand/gravel block turns up one",
                        "of the five dinosaur egg blocks, from which the creature can later hatch.")
                .translation("primordialmobs.config.brushing_egg_chance")
                .defineInRange("egg_chance", 0.02D, 0.0D, 1.0D);
        builder.pop();
    }

    /**
     * {@code rename_mobs}, readable BEFORE Forge loads configs.
     *
     * The rename overlay pack is offered from a {@code RepositorySource}, and the first resource-pack
     * scan of a game launch runs during mod construction - earlier than any {@code ModConfigEvent}. On
     * that first scan the value is read straight from the TOML on disk (defaulting to true on a fresh
     * install, where the file does not exist yet); every later scan (F3+T, the pack screen, world
     * joins) sees the live Forge-loaded value.
     */
    public static boolean renamesEnabled() {
        if (PrimordialMobs.COMMON_CONFIG_SPEC.isLoaded()) {
            return PrimordialMobs.COMMON_CONFIG.renameMobs.get();
        }
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (Files.isRegularFile(path)) {
            try (com.electronwill.nightconfig.core.file.FileConfig config =
                         com.electronwill.nightconfig.core.file.FileConfig.of(path)) {
                config.load();
                Object value = config.get("renames.rename_mobs");
                if (value instanceof Boolean bool) {
                    return bool;
                }
            } catch (Exception e) {
                PrimordialMobs.LOGGER.warn("Could not pre-read {}; assuming renames are enabled", FILE_NAME, e);
            }
        }
        return true;
    }
}
