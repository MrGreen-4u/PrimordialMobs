package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PrimordialBrushingLootModifier implements IGlobalLootModifier {
    public static final Supplier<Codec<PrimordialBrushingLootModifier>> CODEC = () ->
            RecordCodecBuilder.create(inst ->
                    inst.group(
                                    LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
                                    Codec.FLOAT.fieldOf("egg_chance").forGetter(lm -> lm.eggChance),
                                    Codec.FLOAT.fieldOf("relic_chance").forGetter(lm -> lm.relicChance)
                            )
                            .apply(inst, PrimordialBrushingLootModifier::new));

    private static final String[] DINOSAURS = {"tremorsaurus", "relicheirus", "grottoceratops", "subterranodon", "vallumraptor"};

    /**
     * Relic table: only non-craftable, non-plant, non-food curiosities — things that make sense as a
     * fossilised find. Deliberately ABSENT:
     * - the mod's plants (tree star, curly fern, fiddlehead, pewen sapling, pine nuts): since the Sniffer
     *   rework they can only be dug out of the ground by a Sniffer
     *   (see data/minecraft/loot_tables/gameplay/sniffer_digging.json), never brushed out;
     * - fresh meat (raw trilocaris tail, dinosaur chop): edible parts of living animals have no business
     *   surviving buried in sand, so they only come from the animals themselves.
     * Everything is referenced by id so that, when the full Alex's Caves mod is installed (which registers
     * these same "alexscaves:" ids itself), the modifier hands out its items.
     */
    private static final Object[][] RELICS = {
            {"heavy_bone", 10, 1, 2},
            {"tectonic_shard", 8, 1, 1},
            {"amber_curiosity", 8, 1, 1},
            // The four pottery sherds (from the original mod): brushed out of suspicious sand AND
            // suspicious gravel, since the conditions below cover all six vanilla archaeology tables.
            {"dinosaur_pottery_sherd", 5, 1, 1},
            {"footprint_pottery_sherd", 5, 1, 1},
            {"guardian_pottery_sherd", 5, 1, 1},
            {"hero_pottery_sherd", 5, 1, 1}
    };

    private final LootItemCondition[] conditions;

    private final Predicate<LootContext> orConditions;

    private final float eggChance;

    private final float relicChance;

    protected PrimordialBrushingLootModifier(LootItemCondition[] conditionsIn, float eggChance, float relicChance) {
        this.conditions = conditionsIn;
        this.orConditions = LootItemConditions.orConditions(conditionsIn);
        this.eggChance = eggChance;
        this.relicChance = relicChance;
    }

    @NotNull
    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        return this.orConditions.test(context) ? this.doApply(generatedLoot, context) : generatedLoot;
    }

    @Nonnull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Warm-ocean archaeology owns the vanilla Sniffer Egg roll. Never replace a successful roll with
        // a primordial egg or relic, so this modifier does not alter the vanilla egg's probability.
        if (generatedLoot.stream().anyMatch(stack -> stack.is(Items.SNIFFER_EGG))) {
            return generatedLoot;
        }
        RandomSource random = context.getRandom();
        ItemStack discovery = ItemStack.EMPTY;
        if (random.nextFloat() < eggChance) {
            discovery = rollEgg(random);
        } else if (random.nextFloat() < relicChance) {
            discovery = rollRelic(random);
        }
        if (!discovery.isEmpty()) {
            generatedLoot.clear();
            generatedLoot.add(discovery);
        }
        return generatedLoot;
    }

    /**
     * The rare find is the dinosaur's <em>egg block</em> ({@code alexscaves:<dinosaur>_egg}), not its spawn
     * egg: you carry the egg home, place it on solid ground and wait for it to hatch, and the baby that
     * comes out imprints on you. A buried, fossilised egg is the thing archaeology should turn up — a
     * spawn egg is a creative-mode item and handed the finished adult over for free.
     */
    private static ItemStack rollEgg(RandomSource random) {
        String dinosaur = DINOSAURS[random.nextInt(DINOSAURS.length)];
        Item egg = ForgeRegistries.ITEMS.getValue(new ResourceLocation(PrimordialMobs.NAMESPACE, dinosaur + "_egg"));
        return egg == null ? ItemStack.EMPTY : new ItemStack(egg);
    }

    private static ItemStack rollRelic(RandomSource random) {
        int totalWeight = 0;
        for (Object[] relic : RELICS) {
            totalWeight += (int) relic[1];
        }
        int roll = random.nextInt(totalWeight);
        for (Object[] relic : RELICS) {
            roll -= (int) relic[1];
            if (roll < 0) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(PrimordialMobs.NAMESPACE, (String) relic[0]));
                if (item == null) {
                    return ItemStack.EMPTY;
                }
                int min = (int) relic[2];
                int max = (int) relic[3];
                return new ItemStack(item, min + random.nextInt(max - min + 1));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
