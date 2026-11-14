package com.ordana.spelunkery.mixins;

import com.ordana.spelunkery.blocks.ChannelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
abstract class FlowingFluidMixin {

    @Inject(method = "canPassThroughWall", at = @At("HEAD"), cancellable = true)
    private void spelunkery$blockClosedChannel(Direction direction, BlockGetter level, BlockPos fromPos,
                                               BlockState fromState, BlockPos toPos, BlockState toState,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!direction.getAxis().isHorizontal()) return;

        BlockState below = level.getBlockState(fromPos.below());
        if (below.getBlock() instanceof ChannelBlock
                && below.getValue(ChannelBlock.PROPERTY_BY_DIRECTION.get(direction))) {
            cir.setReturnValue(false);
        }
    }
}

