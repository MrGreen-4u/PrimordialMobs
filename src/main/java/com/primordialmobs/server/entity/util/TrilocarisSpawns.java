package com.primordialmobs.server.entity.util;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.util.RandomSource;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Where the Trilocaris comes from naturally.
 *
 * In the Lush Caves it spawns EXACTLY like an axolotl: a full water block sitting on clay. Vanilla's rule is
 * literally {@code level.getBlockState(pos.below()).is(BlockTags.AXOLOTLS_SPAWNABLE_ON)} (that tag is just
 * {@code minecraft:clay}), and the placement is IN_WATER / MOTION_BLOCKING_NO_LEAVES — the same pair used
 * here.
 *
 * Everywhere else the original "any deep cave water" rule is kept, because with the full Alex's Caves
 * installed the Trilocaris is also a water_ambient spawn of its own Primordial Caves biome; requiring clay
 * globally would silently delete those spawns.
 */
public final class TrilocarisSpawns {

    public static final ResourceLocation LUSH_CAVES = new ResourceLocation("minecraft", "lush_caves");
    public static final ResourceLocation TRILOCARIS = new ResourceLocation(PrimordialMobs.NAMESPACE, "trilocaris");

    private TrilocarisSpawns() {
    }

    public static boolean checkSpawnRules(EntityType<? extends LivingEntity> type, ServerLevelAccessor level,
                                          MobSpawnType spawnType, BlockPos pos, RandomSource randomSource) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.is(FluidTags.WATER) || fluidState.getAmount() < 8) {
            return false;
        }
        if (level.getBiome(pos).is(LUSH_CAVES)) {
            return level.getBlockState(pos.below()).is(BlockTags.AXOLOTLS_SPAWNABLE_ON);
        }
        return isInCave(level, pos);
    }

    private static boolean isInCave(ServerLevelAccessor level, BlockPos pos) {
        BlockPos above = pos;
        while (level.getFluidState(above).is(FluidTags.WATER)) {
            above = above.above();
        }
        return !level.canSeeSky(above) && above.getY() < level.getSeaLevel();
    }

    /**
     * Compat mode only: Alex's Caves registers its own Trilocaris placement, whose rule has no clay
     * requirement, so it is REPLACEd with ours. Standalone the equivalent registration lives in
     * PMEntityRegistry. Resolving the type by id keeps this class free of any Alex's Caves reference.
     */
    public static void registerCompatPlacement(SpawnPlacementRegisterEvent event) {
        EntityType<?> trilocaris = ForgeRegistries.ENTITY_TYPES.getValue(TRILOCARIS);
        if (trilocaris == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        EntityType<LivingEntity> typed = (EntityType<LivingEntity>) trilocaris;
        event.register(typed, SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TrilocarisSpawns::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
