package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.entity.SnowCoveredBlockEntity;

public class SnowFenceGateBlock extends FenceGateBlock implements EntityBlock, WatcherSnowVariant {

	public SnowFenceGateBlock(Properties properties) {
		super(properties);
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
	public VoxelShape getVisualShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return state.getCollisionShape(worldIn, pos, context);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(ShapeCaches.OUTLINE, state, worldIn, pos, () -> {
			VoxelShape shape = super.getShape(state, worldIn, pos, context);
			return Shapes.or(shape, getSnowState(state, worldIn, pos).getShape(worldIn, pos, context));
		});
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SnowCoveredBlockEntity(pos, state);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean p_60514_) {
		SnowCoveredBlockEntity.updateOptions(level, pos);
	}

}
