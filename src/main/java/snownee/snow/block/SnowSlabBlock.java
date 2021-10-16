package snownee.snow.block;

import java.util.Random;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import snownee.kiwi.block.ModBlock;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.WrappedSoundType;

public class SnowSlabBlock extends ModBlock implements WaterLoggableSnowVariant {
	protected static final VoxelShape BOTTOM_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	protected static final VoxelShape BOTTOM_RENDER_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

	public SnowSlabBlock(Properties builder) {
		super(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SnowTextureTile();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		TileEntity tile = worldIn.getTileEntity(pos);
		ItemStack stack = player.getHeldItem(handIn);
		if (hit.getFace() == Direction.UP && tile instanceof SnowTextureTile && ((SnowTextureTile) tile).getState().getBlock().asItem() == stack.getItem() && stack.getItem() instanceof BlockItem && stack.getItem().isIn(ItemTags.SLABS)) {
			Block block = ((BlockItem) stack.getItem()).getBlock();
			if (block instanceof SlabBlock) {
				BlockState state2 = block.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
				if (!worldIn.isRemote) {
					worldIn.setBlockState(pos, state2);
					if (!player.isCreative()) {
						stack.shrink(1);
					}
					if (player instanceof ServerPlayerEntity) {
						CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
					}
				}
				SoundType soundtype = state2.getSoundType(worldIn, pos, player);
				worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BOTTOM_SHAPE;
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return BOTTOM_RENDER_SHAPE;
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (SnowCommonConfig.retainOriginalBlocks) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		} else if (ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		return WrappedSoundType.get(getRaw(state, world, pos).getSoundType(world, pos, entity));
	}

	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
		return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
	}

}
