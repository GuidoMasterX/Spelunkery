package com.ordana.spelunkery.mixins;

import com.ordana.spelunkery.blocks.ChannelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(FlowingFluid.class)
abstract class FlowingFluidMixin {

    @Redirect(method = "getSpread",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V addChannelException(Map<K, V> instance, K direction, V fluidState, Level level, BlockPos pos, BlockState blockState) {

        var channelState = level.getBlockState(pos.below());
        if (channelState.getBlock() instanceof ChannelBlock && direction != Direction.DOWN && direction != Direction.UP) {
            if (channelState.getValue(ChannelBlock.PROPERTY_BY_DIRECTION.get(direction))) return null;
        }
        return instance.put(direction, fluidState);

    }
}

