package snownee.snow.compat.diagonalfences;

import fuzs.diagonalblocks.common.api.v2.block.DiagonalFenceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.Hooks;
import snownee.snow.block.OptionalLayerSnowVariant;
import snownee.snow.block.ShapeCaches;
import snownee.snow.block.WaterLoggableSnowVariant;
import snownee.snow.compat.diagonalblocks.SnowStarCollisionBlock;

public class SnowDiagonalFenceBlock extends DiagonalFenceBlock implements SnowStarCollisionBlock, WaterLoggableSnowVariant, OptionalLayerSnowVariant {
	public SnowDiagonalFenceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.COLLIDER,
				blockState,
				it -> super.getCollisionShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState) {
		return ShapeCaches.get(ShapeCaches.VISUAL, blockState, super::getOcclusionShape);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE,
				blockState,
				it -> super.getShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()),
				SnowStarCollisionBlock::mergeShape);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		Hooks.randomTick(state, worldIn, pos, random);
	}

	@Override
	public BlockState updateShape(
			BlockState blockState,
			LevelReader levelReader,
			ScheduledTickAccess scheduledTickAccess,
			BlockPos blockPos,
			Direction direction,
			BlockPos neighboringBlockPos,
			BlockState neighboringBlockState,
			RandomSource randomSource) {
		blockState = super.updateShape(
				blockState,
				levelReader,
				scheduledTickAccess,
				blockPos,
				direction,
				neighboringBlockPos,
				neighboringBlockState,
				randomSource);
		if (!Hooks.canSnowSurvive(levelReader, blockPos)) {
			blockState = blockState.setValue(OPTIONAL_LAYERS, 0);
		}
		return blockState;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return Hooks.canBeReplaced(state, context);
	}
}
