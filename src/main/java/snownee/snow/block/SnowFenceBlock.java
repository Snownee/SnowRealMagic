package snownee.snow.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.WrappedSoundType;

public class SnowFenceBlock extends FenceBlock implements IWaterLoggableSnowVariant {

	public static final BooleanProperty DOWN = SixWayBlock.DOWN;

	public SnowFenceBlock(Properties properties) {
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		//        boolean isWooden = false;
		//        BlockState state = getDefaultState().with(WATERLOGGED, false)
		//        ItemStack stack = context.getItem();
		//        //Check the item to get the actual state we want to try to connect using.
		//        NBTHelper data = NBTHelper.of(stack);
		//        ResourceLocation rl = Util.RL(data.getString("BlockEntityTag.Items.0", ""));
		//        if (rl != null) {
		//            Item item = ForgeRegistries.ITEMS.getValue(rl);
		//            if (item instanceof BlockItem) {
		//                isWooden = ((BlockItem) item).getBlock().isIn(BlockTags.WOODEN_FENCES);
		//            }
		//        }
		//Now check the connections using the correct material we just retrieved
		World world = context.getWorld();
		BlockPos blockpos = context.getPos();
		BlockState stateIn = world.getBlockState(blockpos);
		return super.getStateForPlacement(context).with(DOWN, CoreModule.BLOCK.isValidPosition(stateIn, world, blockpos));
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		stateIn = super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		if (facing == Direction.DOWN) {
			return stateIn.with(DOWN, CoreModule.BLOCK.isValidPosition(stateIn, worldIn, currentPos, true));
		}
		return stateIn;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(DOWN);
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
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (SnowCommonConfig.retainOriginalBlocks) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		} else if (ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		VoxelShape shape = super.getRenderShape(state, worldIn, pos);
		if (state.get(DOWN)) {
			shape = VoxelShapes.combine(shape, ModSnowBlock.SNOW_SHAPES_MAGIC[2], IBooleanFunction.OR);
		}
		return shape;
	}

	@SuppressWarnings("deprecation")
	@Override
	public SoundType getSoundType(BlockState state) {
		return WrappedSoundType.get(super.getSoundType(state));
	}

	@Override
	public String getTranslationKey() {
		if (this == CoreModule.FENCE) {
			return super.getTranslationKey();
		} else {
			return CoreModule.FENCE.getTranslationKey();
		}
	}
}
