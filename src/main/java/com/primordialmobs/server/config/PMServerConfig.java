package com.primordialmobs.server.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The mod's general config ({@code primordialmobs-general.toml}). Only the options this mod actually
 * reads are declared: everything else inherited from upstream Alex's Caves configured content that does
 * not exist here (nukes, cave maps, cave tablets, the Watcher, the Atlatitan and friends) and only made
 * the file confusing.
 */
public class PMServerConfig {

    public final ForgeConfigSpec.IntValue pathfindingThreads;
    public final ForgeConfigSpec.IntValue amberMonolithMeanTime;
    public final ForgeConfigSpec.BooleanValue enchantmentsInLoot;

    public PMServerConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("mob-behavior");
        pathfindingThreads = builder.comment("How many cpu cores the big dinosaurs (Grazer, Roarer, Logger etc) should utilize when pathing. Bigger number = less impact on TPS").translation("pathfinding_threads").defineInRange("pathfinding_threads", 5, 1, 100);
        builder.pop();
        builder.push("block-behavior");
        amberMonolithMeanTime = builder.comment("How long (in game ticks) it usually takes for an amber monolith to spawn an animal.").translation("amber_monolith_mean_time").defineInRange("amber_monolith_mean_time", 32000, 1000, Integer.MAX_VALUE);
        builder.pop();
        builder.push("vanilla-changes");
        enchantmentsInLoot = builder.comment("Whether this mod's weapon enchantments can be traded by villagers and appear as enchanted books in vanilla loot tables.").translation("enchantments_in_loot").define("enchantments_in_loot", false);
        builder.pop();
    }
}
