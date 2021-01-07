package snownee.snow.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.Util;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;

@RenderLayer(Layer.CUTOUT)
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
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return ModBlock.pickBlock(state, target, world, pos, player);
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
        return super.getStateForPlacement(context).with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, world, blockpos));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN) {
            return stateIn.with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, worldIn, currentPos, true));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, DOWN, WATERLOGGED);
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
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (material == Material.WOOD) {
            MainModule.fillTextureItems(ItemTags.WOODEN_FENCES, this, items);
        } else {
            MainModule.fillTextureItems(ItemTags.FENCES, this, items, item -> !item.isIn(ItemTags.WOODEN_FENCES));
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (SnowCommonConfig.retainOriginalBlocks) {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        } else if (BlockUtil.shouldMelt(worldIn, pos)) {
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
}
