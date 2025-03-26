package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.util.CommonProxy;

@NotNullByDefault
public class SnowStairsBlock extends StairBlock implements WaterLoggableSnowVariant {

	public SnowStairsBlock(Properties properties) {
		super(Blocks.STONE.defaultBlockState(), properties);
	}

	@Override
	public VoxelShape getCollisionShape(
			BlockState blockState,
			BlockGetter blockGetter,
			BlockPos blockPos,
			CollisionContext collisionContext) {
		return super.getShape(blockState, blockGetter, blockPos, collisionContext);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE, blockState, it -> {
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
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeBlockEntity(pos);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (CommonProxy.shouldMelt(worldIn, pos)) {
			worldIn.setBlockAndUpdate(pos, srm$getRaw(state, worldIn, pos));
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

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}
}
