package snownee.snow.compat.farmersdelight;

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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.Hooks;
import snownee.snow.block.OptionalLayerSnowVariant;
import snownee.snow.block.ShapeCaches;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WaterLoggableSnowVariant;
import vectorwing.farmersdelight.common.block.RopeFenceBlock;

@NotNullByDefault
public class SnowRopeFenceBlock extends RopeFenceBlock implements WaterLoggableSnowVariant, OptionalLayerSnowVariant {

	public SnowRopeFenceBlock(Properties properties) {
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
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter worldIn, BlockPos pos) {
		return ShapeCaches.get(ShapeCaches.VISUAL, blockState, it -> super.getOcclusionShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
	}

	@Override
	public VoxelShape getVisualShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE,
				blockState,
				it -> super.getVisualShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
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
	protected boolean isSameFence(BlockState state) {
		return super.isSameFence(state) || state.getBlock() instanceof SnowRopeFenceBlock || state.getBlock() instanceof SnowFenceGateBlock;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SnowVariant.OPTIONAL_LAYERS);
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
			state = state.setValue(SnowVariant.OPTIONAL_LAYERS, 0);
		}
		return state;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return Hooks.canBeReplaced(state, context);
	}

}