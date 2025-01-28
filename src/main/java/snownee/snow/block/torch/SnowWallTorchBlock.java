package snownee.snow.block.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.Hooks;
import snownee.snow.block.ShapeCaches;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.block.WaterLoggableSnowVariant;
import snownee.snow.block.entity.SnowCoveredBlockEntity;

public class SnowWallTorchBlock extends WallTorchBlock implements WaterLoggableSnowVariant, WatcherSnowVariant {

	public SnowWallTorchBlock(Properties properties, ParticleOptions particleOptions) {
		super(properties, particleOptions);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(ShapeCaches.COLLIDER, state, worldIn, pos, () -> {
			VoxelShape shape = super.getCollisionShape(state, worldIn, pos, context);
			return Shapes.or(shape, getSnowState(state, worldIn, pos).getCollisionShape(worldIn, pos, context));
		});
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return ShapeCaches.get(ShapeCaches.VISUAL, state, worldIn, pos, () -> {
			VoxelShape shape = super.getOcclusionShape(state, worldIn, pos);
			return Shapes.or(shape, getSnowState(state, worldIn, pos).getOcclusionShape(worldIn, pos));
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(ShapeCaches.OUTLINE, state, worldIn, pos, () -> {
			VoxelShape shape = super.getShape(state, worldIn, pos, context);
			return Shapes.or(shape, getSnowState(state, worldIn, pos).getShape(worldIn, pos, context));
		});
	}


	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean p_60514_) {
		SnowCoveredBlockEntity.updateOptions(level, pos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OPTIONAL_LAYERS);
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
		if (!Hooks.canSnowSurvive(state, level, pos)) {
			state = state.setValue(OPTIONAL_LAYERS, 0);
		}
		return state;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return Hooks.canBeReplaced(state, context);
	}
}
