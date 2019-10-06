package snownee.snow.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.Util;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.state.SnowFenceBlockState;

public class SnowFenceBlock extends FenceBlock implements ISnowVariant
{

    public static final BooleanProperty DOWN = SixWayBlock.DOWN;
    public static final Material NO_MATCH = new Material.Builder(MaterialColor.WOOD).build();

    protected final StateContainer<Block, BlockState> ourStateContainer;

    public SnowFenceBlock(Properties properties)
    {
        super(properties);
        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        fillStateContainer(builder);
        ourStateContainer = builder.create(SnowFenceBlockState::new);
        setDefaultState(ourStateContainer.getBaseState().with(NORTH, false).with(EAST, false).with(SOUTH, false)
              .with(WEST, false).with(WATERLOGGED, false));
    }

    @Nonnull
    @Override
    public StateContainer<Block, BlockState> getStateContainer() {
        //Overwrite access to the state container with ours that has the override method
        return ourStateContainer;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new SnowTextureTile();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return ModBlock.pickBlock(state, target, world, pos, player);
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public Material getMaterial(BlockState state)
    {
        //Fallback to a custom material to ensure it does not match so that other fences only connect
        // when our custom blockstate says that we are solid
        return NO_MATCH;
    }

    public Material getMaterial(BlockState state, IBlockReader world, BlockPos pos)
    {
        return getRaw(state, world, pos).getMaterial();
    }

    /**
     * @param otherState  The neighboring block/fences state
     * @param isSolid     If the block we are attempting to attach to is solid on the side we are connecting to
     * @param dirToCheck  The direction the other block is from us
     * @param ourMaterial Our Material
     */
    public boolean canConnect(BlockState otherState, boolean isSolid, Direction dirToCheck, Material ourMaterial)
    {
        Block block = otherState.getBlock();
        boolean isMatchingFence = block.isIn(BlockTags.FENCES) && otherState.getMaterial() == ourMaterial;
        boolean isFenceGate = block instanceof FenceGateBlock && FenceGateBlock.isParallel(otherState, dirToCheck);
        return !cannotAttach(block) && isSolid || isMatchingFence || isFenceGate;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        World iblockreader = context.getWorld();
        BlockPos blockpos = context.getPos();
        BlockState stateIn = iblockreader.getBlockState(blockpos);
        return super.getStateForPlacement(context).with(WATERLOGGED, false).with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, iblockreader, blockpos));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if (facing == Direction.DOWN)
        {
            ModSnowTileBlock.updateSnowyDirt(worldIn, facingPos, facingState);
            return stateIn.with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, worldIn, currentPos, true));
        }
        if (facing.getAxis().getPlane() == Direction.Plane.HORIZONTAL)
        {
            boolean connected = this.canConnect(facingState, facingState.func_224755_d(worldIn, facingPos, facing.getOpposite()), facing.getOpposite(),
                  getMaterial(stateIn, worldIn, currentPos));
            return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), connected);
        }
        return stateIn;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        //Ensure that after we place the block already containing snow that it updates the block under it
        BlockPos down = pos.down();
        ModSnowTileBlock.updateSnowyDirt(world, down, world.getBlockState(down));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, WEST, SOUTH, DOWN, WATERLOGGED);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
    {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        String key = Util.getTextureItem(stack, "0");
        if (!key.isEmpty())
        {
            tooltip.add(new TranslationTextComponent(key).applyTextStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        MainModule.fillTextureItems(ItemTags.FENCES, this, items);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random)
    {
        if (!SnowCommonConfig.snowNeverMelt && worldIn.getLightFor(LightType.BLOCK, pos) > 11)
        {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        }
    }
}
