package snownee.snow.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.WallBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.Util;
import snownee.snow.ModUtil;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.WrappedSoundType;

@RenderLayer(Layer.CUTOUT)
public class SnowWallBlock extends WallBlock implements IWaterLoggableSnowVariant {
	public static final BooleanProperty DOWN = SixWayBlock.DOWN;

	public SnowWallBlock(Properties properties) {
		super(properties);
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
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return ModBlock.pickBlock(state, target, world, pos, player);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		String key = Util.getTextureItem(stack, "0");
		if (!key.isEmpty()) {
			tooltip.add(new TranslationTextComponent(key).mergeStyle(TextFormatting.GRAY));
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(UP, WALL_HEIGHT_EAST, WALL_HEIGHT_NORTH, WALL_HEIGHT_SOUTH, WALL_HEIGHT_WEST, BlockStateProperties.WATERLOGGED, DOWN);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.DOWN) {
			return stateIn.with(DOWN, CoreModule.BLOCK.isValidPosition(stateIn, worldIn, currentPos, true));
		}
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		World iblockreader = context.getWorld();
		BlockPos blockpos = context.getPos();
		BlockState stateIn = iblockreader.getBlockState(blockpos);
		return super.getStateForPlacement(context).with(DOWN, CoreModule.BLOCK.isValidPosition(stateIn, iblockreader, blockpos));
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		CoreModule.fillTextureItems(ItemTags.WALLS, this, items);
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return super.getShape(state.with(DOWN, Boolean.TRUE), worldIn, pos, context);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return super.getCollisionShape(state.with(DOWN, Boolean.TRUE), worldIn, pos, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		VoxelShape shape = super.getRenderShape(state, worldIn, pos);
		if (state.get(DOWN)) {
			shape = VoxelShapes.combine(shape, ModSnowBlock.SNOW_SHAPES_MAGIC[2], IBooleanFunction.OR);
		}
		return shape;
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
