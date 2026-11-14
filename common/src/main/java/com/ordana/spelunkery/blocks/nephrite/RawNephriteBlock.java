package com.ordana.spelunkery.blocks.nephrite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class RawNephriteBlock extends HugeMushroomBlock {
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION;


    public RawNephriteBlock(Properties properties) {
        super(properties);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        for (Direction dir : Direction.values()) {
            BlockPos relative = pos.relative(dir);
            this.updateShape(state, dir, level.getBlockState(relative), level, pos, relative);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite()) ? state.setValue(PROPERTY_BY_DIRECTION.get(direction), false) : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    static {
        PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    }
}
