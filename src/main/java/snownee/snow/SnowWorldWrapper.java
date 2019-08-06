//package snownee.snow;
//
//import java.util.List;
//import java.util.Random;
//import java.util.function.Predicate;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.SnowBlock;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.fluid.Fluid;
//import net.minecraft.fluid.IFluidState;
//import net.minecraft.particles.IParticleData;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.SoundCategory;
//import net.minecraft.util.SoundEvent;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.shapes.VoxelShape;
//import net.minecraft.world.DifficultyInstance;
//import net.minecraft.world.ITickList;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.LightType;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.border.WorldBorder;
//import net.minecraft.world.chunk.AbstractChunkProvider;
//import net.minecraft.world.chunk.ChunkStatus;
//import net.minecraft.world.chunk.IChunk;
//import net.minecraft.world.dimension.Dimension;
//import net.minecraft.world.gen.Heightmap.Type;
//import net.minecraft.world.storage.WorldInfo;
//import snownee.snow.block.ModSnowBlock;
//import snownee.snow.block.SnowTile;
//
//public class SnowWorldWrapper implements IWorld
//{
//    private final IWorld world;
//
//    public SnowWorldWrapper(IWorld world)
//    {
//        this.world = world;
//    }
//
//    @Override
//    public Biome getBiome(BlockPos pos)
//    {
//        return world.getBiome(pos);
//    }
//
//    @Override
//    public int getLightFor(LightType type, BlockPos pos)
//    {
//        return world.getLightFor(type, pos);
//    }
//
//    @Override
//    public TileEntity getTileEntity(BlockPos pos)
//    {
//        return null;
//    }
//
//    @Override
//    public BlockState getBlockState(BlockPos pos)
//    {
//        BlockState state = world.getBlockState(pos);
//        if (state.getBlock() == MainModule.TILE_BLOCK)
//        {
//            return MainModule.TILE_BLOCK.getContainedState(world, pos);
//        }
//        return state;
//    }
//
//    @Override
//    public boolean hasBlockState(BlockPos pos, Predicate<BlockState> state)
//    {
//        return world.hasBlockState(pos, state);
//    }
//
//    @Override
//    public boolean setBlockState(BlockPos pos, BlockState newState, int flags)
//    {
//        BlockState state = world.getBlockState(pos);
//        if (state.getBlock() == MainModule.TILE_BLOCK)
//        {
//            if (ModSnowBlock.canContainState(newState))
//            {
//                TileEntity tile = world.getTileEntity(pos);
//                if (tile instanceof SnowTile)
//                {
//                    ((SnowTile) tile).setState(newState);
//                }
//                return true;
//            }
//            return false;
//        }
//        return world.setBlockState(pos, newState, flags);
//    }
//
//    @Override
//    public boolean removeBlock(BlockPos pos, boolean isMoving)
//    {
//        BlockState state = world.getBlockState(pos);
//        if (state.getBlock() == MainModule.TILE_BLOCK)
//        {
//            int i = state.get(SnowBlock.LAYERS);
//            BlockState newState = MainModule.BLOCK.getDefaultState().with(SnowBlock.LAYERS, i);
//            return world.setBlockState(pos, newState, 3 | (isMoving ? 64 : 0));
//        }
//        return world.removeBlock(pos, isMoving);
//    }
//
//    @Override
//    public IFluidState getFluidState(BlockPos pos)
//    {
//        return world.getFluidState(pos);
//    }
//
//    @Override
//    public int getLightSubtracted(BlockPos pos, int amount)
//    {
//        return world.getLightSubtracted(pos, amount);
//    }
//
//    @Override
//    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull)
//    {
//        return world.getChunk(x, z, requiredStatus, nonnull);
//    }
//
//    @Override
//    public boolean chunkExists(int chunkX, int chunkZ)
//    {
//        return world.chunkExists(chunkX, chunkZ);
//    }
//
//    @Override
//    public BlockPos getHeight(Type heightmapType, BlockPos pos)
//    {
//        return world.getHeight(heightmapType, pos);
//    }
//
//    @Override
//    public int getHeight(Type heightmapType, int x, int z)
//    {
//        return world.getHeight(heightmapType, x, z);
//    }
//
//    @Override
//    public int getSkylightSubtracted()
//    {
//        return world.getSkylightSubtracted();
//    }
//
//    @Override
//    public WorldBorder getWorldBorder()
//    {
//        return world.getWorldBorder();
//    }
//
//    @Override
//    public boolean checkNoEntityCollision(Entity entityIn, VoxelShape shape)
//    {
//        return world.checkNoEntityCollision(entityIn, shape);
//    }
//
//    @Override
//    public boolean isRemote()
//    {
//        return world.isRemote();
//    }
//
//    @Override
//    public int getSeaLevel()
//    {
//        return world.getSeaLevel();
//    }
//
//    @Override
//    public Dimension getDimension()
//    {
//        return world.getDimension();
//    }
//
//    @Override
//    public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate)
//    {
//        return world.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
//    }
//
//    @Override
//    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, Predicate<? super T> filter)
//    {
//        return world.getEntitiesWithinAABB(clazz, aabb, filter);
//    }
//
//    @Override
//    public List<? extends PlayerEntity> getPlayers()
//    {
//        return world.getPlayers();
//    }
//
//    @Override
//    public boolean destroyBlock(BlockPos pos, boolean dropBlock)
//    {
//        return world.destroyBlock(pos, dropBlock);
//    }
//
//    @Override
//    public long getSeed()
//    {
//        return world.getSeed();
//    }
//
//    @Override
//    public ITickList<Block> getPendingBlockTicks()
//    {
//        return world.getPendingBlockTicks();
//    }
//
//    @Override
//    public ITickList<Fluid> getPendingFluidTicks()
//    {
//        return world.getPendingFluidTicks();
//    }
//
//    @Override
//    public World getWorld()
//    {
//        return world.getWorld();
//    }
//
//    @Override
//    public WorldInfo getWorldInfo()
//    {
//        return world.getWorldInfo();
//    }
//
//    @Override
//    public DifficultyInstance getDifficultyForLocation(BlockPos pos)
//    {
//        return world.getDifficultyForLocation(pos);
//    }
//
//    @Override
//    public AbstractChunkProvider getChunkProvider()
//    {
//        return world.getChunkProvider();
//    }
//
//    @Override
//    public Random getRandom()
//    {
//        return world.getRandom();
//    }
//
//    @Override
//    public void notifyNeighbors(BlockPos pos, Block blockIn)
//    {
//        world.notifyNeighbors(pos, blockIn);
//    }
//
//    @Override
//    public BlockPos getSpawnPoint()
//    {
//        return world.getSpawnPoint();
//    }
//
//    @Override
//    public void playSound(PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch)
//    {
//        world.playSound(player, pos, soundIn, category, volume, pitch);
//    }
//
//    @Override
//    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
//    {
//        world.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
//    }
//
//    @Override
//    public void playEvent(PlayerEntity player, int type, BlockPos pos, int data)
//    {
//        world.playEvent(player, type, pos, data);
//    }
//
//}
