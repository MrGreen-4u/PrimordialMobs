package com.primordialmobs.client.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The mod's client config ({@code primordialmobs-client.toml}). Only the options this mod actually reads
 * are declared: everything else inherited from upstream Alex's Caves configured cave biomes, nuclear
 * explosions, cave maps and other content that does not exist here.
 *
 * <p>The indicator options keep the {@code subterranodon} spelling because that is still the Drifter's
 * registry id ({@code alexscaves:subterranodon}), which this mod deliberately never renamed.
 */
public class PMClientConfig {

    public final ForgeConfigSpec.BooleanValue screenShaking;
    public final ForgeConfigSpec.IntValue subterranodonIndicatorX;
    public final ForgeConfigSpec.IntValue subterranodonIndicatorY;

    public PMClientConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("visuals");
        screenShaking = builder.comment("whether to shake the screen from Roarer stomping and roaring.").translation("screen_shaking").define("screen_shaking", true);
        subterranodonIndicatorX = builder.comment("determines how far to the left the Drifter flight indicator renders on the screen when mounted. Negative numbers will render it on the right.").translation("subterranodon_indicator_x").defineInRange("subterranodon_indicator_x", 22, -12000, 12000);
        subterranodonIndicatorY = builder.comment("determines how far from bottom the Drifter flight indicator renders on the screen when mounted.").translation("subterranodon_indicator_y").defineInRange("subterranodon_indicator_y", 6, -12000, 12000);
        builder.pop();
    }
}
