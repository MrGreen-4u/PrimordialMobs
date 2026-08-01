package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.block.PMBlockRegistry;
import com.primordialmobs.server.item.PMItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PMCreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PrimordialMobs.NAMESPACE);

    public static final RegistryObject<CreativeModeTab> PRIMORDIAL_CAVES = DEF_REG.register("primordial_caves", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.alexscaves.primordial_caves"))
            .icon(() -> new ItemStack(PMItemRegistry.AMBER_CURIOSITY.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((enabledFeatures, output) -> {
                PMItemRegistry.getSpawnEggs().forEach((spawnEgg -> add(output, spawnEgg.get())));
                add(output, PMBlockRegistry.SUBTERRANODON_EGG.get());
                add(output, PMBlockRegistry.VALLUMRAPTOR_EGG.get());
                add(output, PMBlockRegistry.GROTTOCERATOPS_EGG.get());
                add(output, PMBlockRegistry.TREMORSAURUS_EGG.get());
                add(output, PMBlockRegistry.RELICHEIRUS_EGG.get());
                add(output, PMBlockRegistry.DINOSAUR_CHOP.get());
                add(output, PMBlockRegistry.COOKED_DINOSAUR_CHOP.get());
                add(output, PMItemRegistry.DINOSAUR_NUGGET.get());
                add(output, PMItemRegistry.PINE_NUTS.get());
                add(output, PMItemRegistry.TOUGH_HIDE.get());
                add(output, PMItemRegistry.HEAVY_BONE.get());
                add(output, PMItemRegistry.TRILOCARIS_TAIL.get());
                add(output, PMItemRegistry.COOKED_TRILOCARIS_TAIL.get());
                add(output, PMItemRegistry.TRILOCARIS_BUCKET.get());
                add(output, PMItemRegistry.SERENE_SALAD.get());
                add(output, PMItemRegistry.PRIMORDIAL_SOUP.get());
                add(output, PMItemRegistry.SEETHING_STEW.get());
                add(output, PMItemRegistry.PRIMITIVE_CLUB.get());
                add(output, PMItemRegistry.AMBER_CURIOSITY.get());
                add(output, PMItemRegistry.TECTONIC_SHARD.get());
                add(output, PMItemRegistry.PEWEN_SAP.get());
                add(output, PMBlockRegistry.AMBER.get());
                add(output, PMBlockRegistry.AMBER_MONOLITH.get());
                add(output, PMItemRegistry.DINOSAUR_POTTERY_SHERD.get());
                add(output, PMItemRegistry.FOOTPRINT_POTTERY_SHERD.get());
                add(output, PMItemRegistry.GUARDIAN_POTTERY_SHERD.get());
                add(output, PMItemRegistry.HERO_POTTERY_SHERD.get());
                add(output, PMBlockRegistry.TREE_STAR.get());
                add(output, PMBlockRegistry.FERN_THATCH.get());
                add(output, PMBlockRegistry.CURLY_FERN.get());
                add(output, PMBlockRegistry.FIDDLEHEAD.get());
                add(output, PMBlockRegistry.PEWEN_SAPLING.get());
                add(output, PMBlockRegistry.PEWEN_LOG.get());
                add(output, PMBlockRegistry.PEWEN_WOOD.get());
                add(output, PMBlockRegistry.STRIPPED_PEWEN_LOG.get());
                add(output, PMBlockRegistry.STRIPPED_PEWEN_WOOD.get());
                add(output, PMBlockRegistry.PEWEN_BRANCH.get());
                add(output, PMBlockRegistry.PEWEN_PINES.get());
            })
            .build());

    private static void add(CreativeModeTab.Output output, ItemLike item) {
        output.accept(item);
    }
}
