package snownee.snow.compat.diagonalwalls;

import fuzs.diagonalblocks.api.v2.block.DiagonalWallBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.Hooks;
import snownee.snow.block.OptionalLayerSnowVariant;
import snownee.snow.block.ShapeCaches;
import snownee.snow.block.WaterLoggableSnowVariant;
import snownee.snow.compat.diagonalblocks.SnowStarCollisionBlock;

public class SnowDiagonalWallBlock extends DiagonalWallBlock implements SnowStarCollisionBlock, WaterLoggableSnowVariant, OptionalLayerSnowVariant {
	public SnowDiagonalWallBlock(Block block) {
		super(block);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.COLLIDER,
				blockState,
				it -> super.getCollisionShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter worldIn, BlockPos pos) {
		return ShapeCaches.get(ShapeCaches.VISUAL, blockState, it -> super.getOcclusionShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
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
			BlockState state,
			Direction direction,
			BlockState thatState,
			LevelAccessor level,
			BlockPos pos,
			BlockPos thatPos) {
		state = super.updateShape(state, direction, thatState, level, pos, thatPos);
		if (!Hooks.canSnowSurvive(level, pos)) {
			state = state.setValue(OPTIONAL_LAYERS, 0);
		}
		return state;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return Hooks.canBeReplaced(state, context);
	}
}
