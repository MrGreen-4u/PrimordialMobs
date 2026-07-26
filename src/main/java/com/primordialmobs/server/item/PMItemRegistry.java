package com.primordialmobs.server.item;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.block.PMBlockRegistry;
import com.primordialmobs.server.entity.PMEntityRegistry;
import net.minecraft.ChatFormatting;
import com.primordialmobs.server.item.dispenser.FluidContainerDispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class PMItemRegistry {
    private static List<RegistryObject<Item>> spawnEggs = new ArrayList<>();
    public static final Rarity RARITY_DEMONIC = Rarity.create("alexscaves:demonic", ChatFormatting.DARK_RED);
    public static final DeferredRegister<Item> DEF_REG = DeferredRegister.create(ForgeRegistries.ITEMS, PrimordialMobs.NAMESPACE);

    public static final RegistryObject<Item> PINE_NUTS = DEF_REG.register("pine_nuts", () -> new ItemNameBlockItem(PMBlockRegistry.PEWEN_SAPLING.get(), new Item.Properties().food(PMFoods.PINE_NUTS)));
    public static final RegistryObject<Item> TOUGH_HIDE = DEF_REG.register("tough_hide", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_BONE = DEF_REG.register("heavy_bone", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> TRILOCARIS_TAIL = DEF_REG.register("trilocaris_tail", () -> new Item(new Item.Properties().food(PMFoods.TRILOCARIS_TAIL)));
    public static final RegistryObject<Item> COOKED_TRILOCARIS_TAIL = DEF_REG.register("cooked_trilocaris_tail", () -> new Item(new Item.Properties().food(PMFoods.TRILOCARIS_TAIL_COOKED)));
    public static final RegistryObject<Item> TRILOCARIS_BUCKET = DEF_REG.register("trilocaris_bucket", () -> new ModFishBucketItem(PMEntityRegistry.TRILOCARIS, () -> Fluids.WATER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> AMBER_CURIOSITY = DEF_REG.register("amber_curiosity", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DINOSAUR_NUGGET = DEF_REG.register("dinosaur_nugget", () -> new Item(new Item.Properties().food(PMFoods.DINOSAUR_NUGGETS)));
    public static final RegistryObject<Item> SERENE_SALAD = DEF_REG.register("serene_salad", () -> new PrehistoricMixtureItem(new Item.Properties().stacksTo(1).food(PMFoods.SERENE_SALAD)));
    public static final RegistryObject<Item> PRIMORDIAL_SOUP = DEF_REG.register("primordial_soup", () -> new PrehistoricMixtureItem(new Item.Properties().stacksTo(1).food(PMFoods.PRIMORDIAL_SOUP)));
    public static final RegistryObject<Item> SEETHING_STEW = DEF_REG.register("seething_stew", () -> new PrehistoricMixtureItem(new Item.Properties().stacksTo(1).food(PMFoods.SEETHING_STEW)));
    public static final RegistryObject<Item> PRIMITIVE_CLUB = DEF_REG.register("primitive_club", () -> new PrimitiveClubItem(new Item.Properties().defaultDurability(120)));
    public static final RegistryObject<Item> PRIMITIVE_CLUB_SPRITE = DEF_REG.register("primitive_club_inventory", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TECTONIC_SHARD = DEF_REG.register("tectonic_shard", () -> new Item(new Item.Properties().rarity(RARITY_DEMONIC).fireResistant()));

    static {
        spawnEgg("trilocaris", PMEntityRegistry.TRILOCARIS, 0X713E0D, 0X8B2010);
        spawnEgg("subterranodon", PMEntityRegistry.SUBTERRANODON, 0X00B1B2, 0XFFF11C);
        spawnEgg("vallumraptor", PMEntityRegistry.VALLUMRAPTOR, 0X22389A, 0XEEE5AB);
        spawnEgg("grottoceratops", PMEntityRegistry.GROTTOCERATOPS, 0XAC3B03, 0XD39B4E);
        spawnEgg("tremorsaurus", PMEntityRegistry.TREMORSAURUS, 0X53780E, 0XDFA211);
        spawnEgg("relicheirus", PMEntityRegistry.RELICHEIRUS, 0X6AE4F9, 0X5B2152);
    }

    private static void spawnEgg(String entityName, RegistryObject type, int color1, int color2) {
        RegistryObject<Item> item = DEF_REG.register("spawn_egg_" + entityName, () -> new ForgeSpawnEggItem(type, color1, color2, new Item.Properties()));
        spawnEggs.add(item);
    }

    public static void setup() {
        DispenserBlock.registerBehavior(TRILOCARIS_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        ComposterBlock.COMPOSTABLES.put(PINE_NUTS.get(), 0.5F);
        ComposterBlock.COMPOSTABLES.put(PMBlockRegistry.TREE_STAR.get().asItem(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(PMBlockRegistry.FERN_THATCH.get().asItem(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(PMBlockRegistry.CURLY_FERN.get().asItem(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(PMBlockRegistry.FIDDLEHEAD.get().asItem(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(PMBlockRegistry.PEWEN_SAPLING.get().asItem(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(PMBlockRegistry.PEWEN_PINES.get().asItem(), 0.3F);
    }

    public static List<RegistryObject<Item>> getSpawnEggs() {
        return spawnEggs;
    }
}
