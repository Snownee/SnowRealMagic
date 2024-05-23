package snownee.snow.block;

import org.jetbrains.annotations.NotNull;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.util.CommonProxy;

public class SnowSlabBlock extends Block implements WaterLoggableSnowVariant {
	protected static final VoxelShape BOTTOM_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	protected static final VoxelShape BOTTOM_RENDER_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

	public SnowSlabBlock(Properties builder) {
		super(builder);
	}

	@Override
	protected @NotNull ItemInteractionResult useItemOn(
			ItemStack itemStack,
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			InteractionHand interactionHand,
			BlockHitResult blockHitResult) {
		if (!(level.getBlockEntity(blockPos) instanceof SnowCoveredBlockEntity blockEntity)) {
			return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		}

		if (itemStack.isEmpty() && player.getOffhandItem().isEmpty()) {
			blockEntity.options.renderOverlay = !blockEntity.options.renderOverlay;
			blockEntity.refresh();
			return ItemInteractionResult.SUCCESS;
		}

		if (blockHitResult.getDirection() == Direction.UP &&
				blockEntity.getContainedState().getBlock().asItem() == itemStack.getItem() &&
				itemStack.getItem() instanceof BlockItem blockItem &&
				itemStack.is(ItemTags.SLABS)) {
			if (blockState.hasProperty(SlabBlock.TYPE)) {
				blockState.trySetValue(SlabBlock.TYPE, SlabType.DOUBLE);
				if (!level.isClientSide) {
					level.setBlockAndUpdate(blockPos, blockState);
					if (!player.isCreative()) {
						itemStack.shrink(1);
					}
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack);
				}

				SoundType soundtype = blockState.getSoundType();
				level.playSound(
						player,
						blockPos,
						soundtype.getPlaceSound(),
						SoundSource.BLOCKS,
						(soundtype.getVolume() + 1.0F) / 2.0F,
						soundtype.getPitch() * 0.8F);
				return ItemInteractionResult.SUCCESS;
			}
		}

		return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return BOTTOM_RENDER_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(
			BlockState blockState,
			BlockGetter blockGetter,
			BlockPos blockPos,
			CollisionContext collisionContext) {
		return BOTTOM_SHAPE;
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		if (pathComputationType == PathComputationType.WATER) {
			return blockState.getFluidState().is(FluidTags.WATER);
		}
		return false;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (SnowCommonConfig.retainOriginalBlocks || CommonProxy.shouldMelt(worldIn, pos)) {
			worldIn.setBlockAndUpdate(pos, srm$getRaw(state, worldIn, pos));
		}
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState srm$getSnowState(BlockState state, BlockGetter level, BlockPos pos) {
		return Blocks.SNOW.defaultBlockState();
	}
}
