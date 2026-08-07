package com.primordialmobs.compat.client;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.config.PMServerConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Path;

/**
 * The rename overlay: a dedicated resource pack (bundled at {@code resourcepacks/primordial_compat/})
 * registered via {@link AddPackFindersEvent}. A pack added this way carries a name that is absent from
 * Forge's mod-pack sort table, which places it above every mod resource pack — deterministically above
 * Alex's Caves' own lang — and {@code required=true} + {@link Pack.Position#TOP} keeps it at the top of
 * the stack whenever it is offered at all (the {@code rename_mobs} config option decides that).
 *
 * Its content is exactly the text that names the renamed mobs (Grazer, Logger, Roarer, Drifter, Stealer,
 * Rammer, Scorcher, Roarerzilla) and everything derived from them: the lang files (egg blocks, spawn
 * eggs, sound subtitles, cave paintings, advancements, the Extinction Spear enchantments) and the
 * guide-book pages under {@code assets/alexscaves/books/}. Nothing else is overridden. Regenerate with
 * {@code tools/localize_names.py} then {@code tools/derived_names.py}.
 *
 * Client-only.
 */
public class CompatLangPack {

    public void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        IModFile modFile = ModList.get().getModFileById(PrimordialMobs.MODID).getFile();
        Path root = modFile.findResource("resourcepacks/primordial_compat");
        Pack pack = Pack.readMetaAndCreate(
                "primordialmobs:compat_names",
                Component.literal("Primordial Mobs Names"),
                true,
                id -> new PathPackResources(id, root, false),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN
        );
        if (pack != null) {
            // The RepositorySource runs on EVERY pack scan, so the config gate is live: the first
            // scan of a launch (which happens before Forge loads configs) falls back to reading the
            // TOML directly, and any later scan (F3+T, the pack screen, world join) uses the loaded
            // value. Disabling rename_mobs simply stops offering the pack.
            event.addRepositorySource(consumer -> {
                if (PMServerConfig.renamesEnabled()) {
                    consumer.accept(pack);
                }
            });
        }
    }
}
