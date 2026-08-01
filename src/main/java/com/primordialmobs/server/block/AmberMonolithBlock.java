package com.primordialmobs.server.block;

import com.primordialmobs.server.block.blockentity.PMBlockEntityRegistry;
import com.primordialmobs.server.block.blockentity.AmberMonolithBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class AmberMonolithBlock extends BaseEntityBlock {

    protected AmberMonolithBlock() {
        super(Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(3F, 12.0F).sound(PMSoundTypes.AMBER_MONOLITH).lightLevel(block -> 5).noOcclusion());
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, PMBlockEntityRegistry.AMBER_MONOLITH.get(), AmberMonolithBlockEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AmberMonolithBlockEntity(pos, state);
    }
}
