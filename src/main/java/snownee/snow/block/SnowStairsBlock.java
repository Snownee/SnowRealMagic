package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.util.CommonProxy;

public class SnowStairsBlock extends StairBlock implements WaterLoggableSnowVariant {

	public SnowStairsBlock(Properties properties) {
		super(Blocks.STONE.defaultBlockState(), properties);
	}

	@Override
	public VoxelShape getCollisionShape(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			CollisionContext context) {
		// to make Entity#getOnPos work properly
		return super.getShape(state, level, pos, context);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return ShapeCaches.get(
				ShapeCaches.VISUAL, state, it -> {
					VoxelShape shape = getShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
					return Shapes.join(shape, Shapes.block(), BooleanOp.AND);
				});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE, state, it -> {
					VoxelShape shape = super.getShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()).move(
							0,
							0.125,
							0);
					return Shapes.or(
							shape,
							Blocks.OAK_SLAB.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
				});
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (CommonProxy.shouldMeltInGeneral(level, pos)) {
			level.setBlockAndUpdate(pos, srm$getRaw(state, level, pos));
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(HALF, Half.BOTTOM);
	}

	//	@Override
	//	public float getPlayerRelativeBlockHardness(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
	//		return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
	//	}

	@Override
	public boolean srm$canRenderDecoration(BlockState blockState) {
		return true;
	}

	@Override
	public boolean srm$canRenderOverlay(BlockState blockState) {
		return false;
	}
}
