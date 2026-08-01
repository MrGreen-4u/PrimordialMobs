package com.primordialmobs.compat.client;

import com.primordialmobs.PrimordialMobs;
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
 * When the full Alex's Caves mod is installed, BOTH mods ship {@code assets/alexscaves/lang/*.json}. Forge
 * orders competing mod resource packs by mod-file discovery order (NOT by mods.toml {@code ordering=AFTER}),
 * so our renamed names (Roarer/Grazer/Logger/Drifter/Stealer and their items) are not guaranteed to win.
 *
 * To make the rename deterministic, we register a dedicated resource pack (bundled at
 * {@code resourcepacks/primordial_compat/}) via {@link AddPackFindersEvent}. A pack added this way carries a
 * name that is absent from Forge's mod-pack sort table, which places it above every mod resource pack, and
 * {@code required=true} + {@link Pack.Position#TOP} keeps it always-enabled at the top of the stack.
 *
 * Its content is exactly the text that names the five mobs and everything derived from them: the lang files
 * (including the Roarerzilla rename and Alex's Caves' sound subtitles, cave paintings, advancements and
 * Extinction Spear enchantments) and the guide-book pages under {@code assets/alexscaves/books/}. Nothing
 * else is overridden. Regenerate both with {@code tools/localize_names.py} then {@code tools/derived_names.py}.
 *
 * Client-only, compat-mode only.
 */
public class CompatLangPack {

    public void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !PrimordialMobs.ALEXS_CAVES_INSTALLED) {
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
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }
}
