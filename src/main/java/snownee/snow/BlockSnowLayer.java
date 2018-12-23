package snownee.snow;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSnowLayer extends BlockSnow
{
    protected static final AxisAlignedBB[] SNOW_AABB_MAGIC = new AxisAlignedBB[] {
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.4375D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D) };

    public static final PropertyBool TILE = PropertyBool.create("tile");

    public BlockSnowLayer()
    {
        super();
        if (ModConfig.placeSnowInBlock)
        {
            this.setDefaultState(this.blockState.getBaseState().withProperty(LAYERS, 1).withProperty(TILE, false));
        }
        setRegistryName("minecraft", "snow_layer");
        setHardness(0.1F);
        setSoundType(SoundType.SNOW);
        setTranslationKey("snow");
        setLightOpacity(0);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        if (ModConfig.placeSnowInBlock)
        {
            return new BlockStateContainer(this, LAYERS, TILE);
        }
        return super.createBlockState();
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        if (ModConfig.placeSnowInBlock)
        {
            return state.getValue(TILE);
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        if (ModConfig.placeSnowInBlock)
        {
            return new TileSnowLayer();
        }
        return null;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if (ModConfig.placeSnowInBlock)
        {
            IBlockState state = super.getStateFromMeta(meta);
            if (meta >= 8)
            {
                state = state.withProperty(TILE, true);
            }
            return state;
        }
        return super.getStateFromMeta(meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int meta = super.getMetaFromState(state);
        if (ModConfig.placeSnowInBlock && state.getValue(TILE))
        {
            meta += 8;
        }
        return meta;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        if (!ModConfig.thinnerBoundingBox)
        {
            return super.getCollisionBoundingBox(blockState, worldIn, pos);
        }
        int layers = blockState.getValue(LAYERS);
        if (layers == 8 && !worldIn.isAirBlock(pos.up()))
        {
            return FULL_BLOCK_AABB;
        }
        return SNOW_AABB_MAGIC[layers - 1];
    }

    @Override
    public int tickRate(World worldIn)
    {
        return 2;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (ModConfig.snowGravity)
        {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (ModConfig.snowGravity)
        {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.isRemote)
            return;
        this.checkFallable(worldIn, pos, state);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11)
        {
            worldIn.setBlockToAir(pos);
            return;
        }
        if (!ModConfig.snowAccumulationDuringSnowfall && !ModConfig.snowAccumulationDuringSnowstorm)
        {
            return;
        }
        if (random.nextInt(8) > 0 || !worldIn.canSeeSky(pos.up()))
        {
            return;
        }
        int layers = state.getValue(LAYERS);

        boolean flag = false;
        if (worldIn.isRaining())
        {
            if (ModConfig.snowAccumulationDuringSnowfall)
            {
                flag = true;
            }
            else if (ModConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering())
            {
                flag = true;
            }
        }

        if (worldIn.canSnowAt(pos, false))
        {
            if (flag && layers < 8)
            {
                // check light
                if (pos.getY() >= 0 && pos.getY() < 256 && worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
                {
                    worldIn.setBlockState(pos, state.withProperty(LAYERS, layers + 1));
                }
            }
            else if (layers > 1 && !worldIn.isRaining() && worldIn.getBlockState(pos.up()).getBlock() != this)
            {
                worldIn.setBlockState(pos, state.withProperty(LAYERS, layers - 1));
            }
        }
    }

    private boolean checkFallable(World worldIn, BlockPos pos, IBlockState state)
    {
        if ((worldIn.isAirBlock(pos.down()) || BlockFalling.canFallThrough(worldIn.getBlockState(pos.down())))
                && pos.getY() >= 0)
        {
            int i = 32;

            if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)))
            {
                if (!worldIn.isRemote)
                {
                    worldIn.setBlockToAir(pos);
                    EntityFallingSnow entityfallingblock = new EntityFallingSnow(worldIn, pos.getX() + 0.5D, pos.getY(),
                            pos.getZ() + 0.5D, state.getValue(LAYERS));
                    worldIn.spawnEntity(entityfallingblock);
                }
            }
            else
            {
                worldIn.setBlockToAir(pos);
                BlockPos blockpos;

                for (blockpos = pos.down(); (worldIn.isAirBlock(blockpos)
                        || BlockFalling.canFallThrough(worldIn.getBlockState(blockpos)))
                        && blockpos.getY() > 0; blockpos = blockpos.down())
                {
                }

                if (blockpos.getY() > 0)
                {
                    worldIn.setBlockState(blockpos.up(), state); //Forge: Fix loss of state information during world gen.
                }
            }
            return true;
        }
        return false;
    }

    public static void placeLayersOn(World world, BlockPos pos, int layers, boolean falling)
    {
        layers = MathHelper.clamp(layers, 1, 8);
        IBlockState state = world.getBlockState(pos);
        int originLayers = 0;
        IBlockState base;
        boolean flag = false;
        if (state.getBlock() == Blocks.SNOW_LAYER)
        {
            originLayers = state.getValue(LAYERS);
            base = state;
        }
        else
        {
            base = Blocks.SNOW_LAYER.getDefaultState();
            if (canContainState(state))
            {
                flag = true;
                base = base.withProperty(BlockSnowLayer.TILE, true);
            }
        }
        if (flag || state.getBlock() == Blocks.SNOW_LAYER || state.getBlock().isReplaceable(world, pos))
        {
            world.setBlockState(pos, base.withProperty(LAYERS, MathHelper.clamp(originLayers + layers, 1, 8)));
            if (flag)
            {
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileSnowLayer)
                {
                    ((TileSnowLayer) tile).setState(state);
                }
            }
            if (falling)
            {
                world.addBlockEvent(pos, Blocks.SNOW_LAYER, originLayers, layers);
            }
            else
            {
                SoundType soundtype = Blocks.SNOW_LAYER.getSoundType(state, world, pos, null);
                world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                        (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
            }
            if (originLayers + layers > 8)
            {
                pos = pos.up();
                if (world.getBlockState(pos).getBlock().isReplaceable(world, pos))
                {
                    world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(LAYERS,
                            MathHelper.clamp(originLayers + layers - 8, 1, 8)));
                }
            }
        }
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int originLayers, int layers)
    {
        double offsetY = originLayers / 8D;
        layers *= 10;
        for (int i = 0; i < layers; ++i)
        {
            double d0 = RANDOM.nextGaussian() * 0.2D;
            double d1 = RANDOM.nextGaussian() * 0.02D;
            double d2 = RANDOM.nextGaussian() * 0.2D;
            worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, (double) ((float) pos.getX() + RANDOM.nextFloat()),
                    (double) pos.getY() + offsetY, (double) ((float) pos.getZ() + RANDOM.nextFloat()), d0, d1, d2);
        }
        SoundType soundtype = getSoundType(state, worldIn, pos, null);
        worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F,
                soundtype.getPitch() * 0.8F);
        return true;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() == this && state.getValue(TILE))
        {
            return getContainedState(worldIn, pos).getMaterial().isReplaceable();
        }
        return (ModConfig.snowAlwaysReplaceable && state.getValue(LAYERS) < 8) || super.isReplaceable(worldIn, pos);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (!ModConfig.particleThroughLeaves || rand.nextInt(31) != 1)
        {
            return;
        }
        IBlockState stateDown = worldIn.getBlockState(pos.down());
        if (stateDown.getBlock().isLeaves(stateDown, worldIn, pos.down()))
        {
            double d0 = pos.getX() + rand.nextDouble();
            double d1 = pos.getY() - 0.05D;
            double d2 = pos.getZ() + rand.nextDouble();
            worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    public static boolean canContainState(IBlockState state)
    {
        if (!ModConfig.placeSnowInBlock || state.getBlock().hasTileEntity(state))
        {
            return false;
        }
        Block block = state.getBlock();
        if (block instanceof BlockTallGrass || block instanceof BlockFlower || block instanceof BlockSapling
                || block instanceof BlockMushroom)
        {
            return true;
        }
        if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockPane)
        {
            return true;
        }
        return false;
    }

    public IBlockState getContainedState(IBlockAccess world, BlockPos pos)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileSnowLayer)
        {
            return ((TileSnowLayer) tile).getState();
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        if (state.getValue(TILE))
        {
            IBlockState stateIn = getContainedState(worldIn, pos);
            Block block = stateIn.getBlock();
            if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockPane)
            {
                return BlockFaceShape.SOLID;
            }
            return stateIn.getBlockFaceShape(worldIn, pos, face);
        }
        return super.getBlockFaceShape(worldIn, state, pos, face);
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        return getContainedState(world, pos).getBlock().canBeConnectedTo(world, pos, facing);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState)
    {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
        if (ModConfig.placeSnowInBlock && state.getValue(TILE))
        {
            getContainedState(worldIn, pos).addCollisionBoxToList(worldIn, pos, entityBox, collidingBoxes, entityIn,
                    isActualState);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if (worldIn.getBlockState(pos).getMaterial() == Material.AIR && hasTileEntity(state))
        {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileSnowLayer)
            {
                IBlockState newState = ((TileSnowLayer) tile).getState();
                worldIn.removeTileEntity(pos);
                if (worldIn.setBlockState(pos, newState))
                    newState.getBlock().neighborChanged(newState, worldIn, pos, newState.getBlock(), pos.down());
            }
            else
            {
                worldIn.removeTileEntity(pos);
            }
        }
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        if (ModConfig.placeSnowInBlock)
        {
            return getContainedState(worldIn, pos).getBlock().isPassable(worldIn, pos);
        }
        return super.isPassable(worldIn, pos);
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state)
    {
        return hasTileEntity(state) ? EnumPushReaction.BLOCK : EnumPushReaction.DESTROY;
    }
}
