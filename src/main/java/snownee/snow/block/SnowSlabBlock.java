package snownee.snow.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.util.CommonProxy;

public class SnowSlabBlock extends Block implements WaterLoggableSnowVariant {
	protected static final VoxelShape BOTTOM_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	protected static final VoxelShape BOTTOM_RENDER_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

	public SnowSlabBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(
			ItemStack itemStack,
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hitResult) {
		if (!(level.getBlockEntity(pos) instanceof SnowCoveredBlockEntity blockEntity)) {
			return InteractionResult.PASS;
		}

		if (hitResult.getDirection() == Direction.UP &&
				blockEntity.getContainedState().getBlock().asItem() == itemStack.getItem() &&
				itemStack.is(ItemTags.SLABS) &&
				itemStack.getItem() instanceof BlockItem blockItem) {
			state = blockItem.getBlock().defaultBlockState().trySetValue(SlabBlock.TYPE, SlabType.DOUBLE);
			if (!level.isClientSide()) {
				level.setBlockAndUpdate(pos, state);
				if (!player.isCreative()) {
					itemStack.shrink(1);
				}
				CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, itemStack);
			}

			SoundType soundtype = state.getSoundType();
			level.playSound(
					player,
					pos,
					soundtype.getPlaceSound(),
					SoundSource.BLOCKS,
					(soundtype.getVolume() + 1.0F) / 2.0F,
					soundtype.getPitch() * 0.8F);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return BOTTOM_RENDER_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			CollisionContext context) {
		return BOTTOM_SHAPE;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (CommonProxy.shouldMeltInGeneral(level, pos)) {
			level.setBlockAndUpdate(pos, srm$getRaw(state, level, pos));
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

	@Override
	public double srm$renderLayerOffset(BlockState blockState) {
		return 0.5;
	}
}
