package com.primordialmobs.server.block;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.block.grower.PewenGrower;
import com.primordialmobs.server.entity.PMEntityRegistry;
import com.primordialmobs.server.item.PMItemRegistry;
import com.github.alexthe666.citadel.item.BlockItemWithSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import com.primordialmobs.server.block.grower.AncientTreeGrower;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class PMBlockRegistry {

    public static final BlockBehaviour.Properties PEWEN_LOG_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.CHERRY_WOOD).instrument(NoteBlockInstrument.BASS);
    public static final WoodType PEWEN_WOOD_TYPE = WoodType.register(new WoodType("alexscaves:pewen", BlockSetType.OAK));

    public static final DeferredRegister<Block> DEF_REG = DeferredRegister.create(ForgeRegistries.BLOCKS, PrimordialMobs.NAMESPACE);
    public static final RegistryObject<Block> SUBTERRANODON_EGG = registerBlockAndItem("subterranodon_egg", () -> new MultipleDinosaurEggsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F).sound(SoundType.METAL).randomTicks(), PMEntityRegistry.SUBTERRANODON, 4));
    public static final RegistryObject<Block> VALLUMRAPTOR_EGG = registerBlockAndItem("vallumraptor_egg", () -> new MultipleDinosaurEggsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F).sound(SoundType.METAL).randomTicks(), PMEntityRegistry.VALLUMRAPTOR, 4));
    public static final RegistryObject<Block> GROTTOCERATOPS_EGG = registerBlockAndItem("grottoceratops_egg", () -> new DinosaurEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F).sound(SoundType.METAL).randomTicks(), PMEntityRegistry.GROTTOCERATOPS, 8, 10));
    public static final RegistryObject<Block> TREMORSAURUS_EGG = registerBlockAndItem("tremorsaurus_egg", () -> new DinosaurEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F).sound(SoundType.METAL).randomTicks(), PMEntityRegistry.TREMORSAURUS, 10, 16));
    public static final RegistryObject<Block> RELICHEIRUS_EGG = registerBlockAndItem("relicheirus_egg", () -> new DinosaurEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F).sound(SoundType.METAL).randomTicks(), PMEntityRegistry.RELICHEIRUS, 14, 16));
    public static final RegistryObject<Block> DINOSAUR_CHOP = registerBlockAndItem("dinosaur_chop", () -> new DinosaurChopBlock(3, 0.2F));
    public static final RegistryObject<Block> COOKED_DINOSAUR_CHOP = registerBlockAndItem("cooked_dinosaur_chop", () -> new DinosaurChopBlock(7, 0.35F));
    public static final RegistryObject<Block> PEWEN_LOG = registerBlockAndItem("pewen_log", () -> new StrippableLogBlock(PEWEN_LOG_PROPERTIES));
    public static final RegistryObject<Block> PEWEN_WOOD = registerBlockAndItem("pewen_wood", () -> new StrippableLogBlock(PEWEN_LOG_PROPERTIES));
    public static final RegistryObject<Block> STRIPPED_PEWEN_LOG = registerBlockAndItem("stripped_pewen_log", () -> new RotatedPillarBlock(PEWEN_LOG_PROPERTIES));
    public static final RegistryObject<Block> STRIPPED_PEWEN_WOOD = registerBlockAndItem("stripped_pewen_wood", () -> new RotatedPillarBlock(PEWEN_LOG_PROPERTIES));
    public static final RegistryObject<Block> PEWEN_BRANCH = registerBlockAndItem("pewen_branch", () -> new PewenBranchBlock());
    public static final RegistryObject<Block> PEWEN_PINES = registerBlockAndItem("pewen_pines", () -> new PewenPinesBlock());
    public static final RegistryObject<Block> PEWEN_SAPLING = registerBlockAndItem("pewen_sapling", () -> new SaplingBlock(new PewenGrower(), BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> FIDDLEHEAD = registerBlockAndItem("fiddlehead", () -> new FiddleheadBlock());
    public static final RegistryObject<Block> CURLY_FERN = registerBlockAndItem("curly_fern", () -> new DoublePlantWithRotationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)));
    public static final RegistryObject<Block> TREE_STAR = registerBlockAndItem("tree_star", () -> new TreeStarBlock());
    public static final RegistryObject<Block> FERN_THATCH = registerBlockAndItem("fern_thatch", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.5F).sound(SoundType.GRASS).noOcclusion()));
    public static final RegistryObject<Block> AMBER = registerBlockAndItem("amber", () -> new GlassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noOcclusion().requiresCorrectToolForDrops().strength(0.3F, 2.0F).sound(PMSoundTypes.AMBER)));
    public static final RegistryObject<Block> AMBER_MONOLITH = registerBlockAndItem("amber_monolith", () -> new AmberMonolithBlock());
    public static final RegistryObject<Block> FLYTRAP = registerBlockAndItem("flytrap", () -> new FlytrapBlock());
    public static final RegistryObject<Block> POTTED_FLYTRAP = DEF_REG.register("potted_flytrap", () -> new PottedFlytrapBlock());
    public static final RegistryObject<Block> CYCAD = registerBlockAndItem("cycad", () -> new CycadBlock());
    public static final RegistryObject<Block> POTTED_CYCAD = DEF_REG.register("potted_cycad", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, CYCAD, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> ARCHAIC_VINE = registerBlockAndItem("archaic_vine", () -> new ArchaicVineBlock());
    public static final RegistryObject<Block> ARCHAIC_VINE_PLANT = DEF_REG.register("archaic_vine_plant", () -> new ArchaicVinePlantBlock());
    public static final RegistryObject<Block> ANCIENT_LEAVES = registerBlockAndItem("ancient_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().isSuffocating((blockState, getter, pos) -> false)));
    public static final RegistryObject<Block> ANCIENT_SAPLING = registerBlockAndItem("ancient_sapling", () -> new SaplingBlock(new AncientTreeGrower(), BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> POTTED_ANCIENT_SAPLING = DEF_REG.register("potted_ancient_sapling", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, ANCIENT_SAPLING, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

    /** Flower-pot wiring for the absorbed plants (called from PMItemRegistry.setup, standalone only). */
    public static void setupFlowerPots() {
        FlowerPotBlock flowerPotBlock = (FlowerPotBlock) Blocks.FLOWER_POT;
        flowerPotBlock.addPlant(FLYTRAP.getId(), POTTED_FLYTRAP);
        flowerPotBlock.addPlant(CYCAD.getId(), POTTED_CYCAD);
        flowerPotBlock.addPlant(ANCIENT_SAPLING.getId(), POTTED_ANCIENT_SAPLING);
    }

    private static RegistryObject<Block> registerBlockAndItem(String name, Supplier<Block> block) {
        RegistryObject<Block> blockObj = DEF_REG.register(name, block);
        PMItemRegistry.DEF_REG.register(name, () -> new BlockItemWithSupplier(blockObj, new Item.Properties()));
        return blockObj;
    }
}
