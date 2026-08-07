package com.primordialmobs.client;

import com.primordialmobs.client.render.entity.PrimordialSnifferRenderer;
import com.primordialmobs.compat.client.CompatLangPack;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * All of the mod's client-side wiring. Kept in one class (and only ever touched through
 * {@code DistExecutor}) so no client class is loaded on a dedicated server.
 */
public final class PMClientSetup {

    private PMClientSetup() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(PMClientSetup::clientSetup);
        // The rename overlay pack (names, subtitles, advancements, guide book in 13 languages).
        modEventBus.addListener(new CompatLangPack()::addPackFinders);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        // The retextured Sniffer renderer (retro/tectonic hides and the recoloured variants). The
        // dinosaurs' variant textures need no renderer of ours: mixins into Alex's Caves' own
        // renderers hand back the variant texture, which sidesteps the parallel-dispatch race that
        // renderer re-registration between two mods would be.
        event.enqueueWork(() -> EntityRenderers.register(EntityType.SNIFFER, PrimordialSnifferRenderer::new));
    }
}
