package snownee.snow.client.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface SRMModel {
	boolean isSnowy(BlockAndTintGetter level, BlockPos pos, BlockState state);
}
