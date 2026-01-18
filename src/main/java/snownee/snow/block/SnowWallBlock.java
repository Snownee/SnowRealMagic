package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.Hooks;

public class SnowWallBlock extends WallBlock implements WaterLoggableSnowVariant, OptionalLayerSnowVariant {

	public SnowWallBlock(Properties properties) {
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
	public VoxelShape getShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE,
				blockState,
				it -> super.getShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		Hooks.randomTick(state, worldIn, pos, random);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OPTIONAL_LAYERS);
	}

	@Override
	protected BlockState updateShape(
			BlockState state,
			LevelReader level,
			ScheduledTickAccess ticks,
			BlockPos pos,
			Direction directionToNeighbour,
			BlockPos neighbourPos,
			BlockState neighbourState,
			RandomSource random) {
		state = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
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
