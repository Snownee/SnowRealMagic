package snownee.snow;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.lighting.LightEngine;
import snownee.snow.block.SnowTextureTile;
import snownee.snow.block.SnowVariant;

public final class Hook {
	private Hook() {
	}

	public static boolean canSurvive(BlockState blockState, IWorldReader viewableWorld, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.up();
		BlockState blockState2 = viewableWorld.getBlockState(blockPos2);
		if (blockState2.getBlock().isIn(CoreModule.BOTTOM_SNOW)) {
			if (blockState2.getBlock() == Blocks.SNOW) {
				return SnowCommonConfig.sustainGrassIfLayerMoreThanOne || blockState2.get(SnowBlock.LAYERS) == 1;
			}
			return true;
		} else {
			int i = LightEngine.func_215613_a(viewableWorld, blockState, blockPos, blockState2, blockPos2, Direction.UP, blockState2.getOpacity(viewableWorld, blockPos2));
			return i < viewableWorld.getMaxLightLevel();
		}
	}

	public static boolean shouldRenderFaceSnow(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
		if (!state.isSolid()) {
			return false;
		}
		pos = pos.offset(direction);
		state = level.getBlockState(pos);
		if (!state.hasTileEntity() || !(state.getBlock() instanceof SnowVariant)) {
			return false;
		}
		TileEntity blockEntity = level.getTileEntity(pos);
		if (blockEntity instanceof SnowTextureTile) {
			return !((SnowTextureTile) blockEntity).getState().isSolid();
		}
		return false;
	}

}
