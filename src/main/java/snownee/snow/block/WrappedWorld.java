package snownee.snow.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;
import snownee.snow.MainModule;

public class WrappedWorld extends World {

    protected World world;

    public WrappedWorld(World world) {
        super((ISpawnWorldInfo) world.getWorldInfo(), world.func_234923_W_(), world.func_230315_m_(), world::getProfiler, world.isRemote, world.func_234925_Z_(), 0l);
        this.world = world;
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return world.getChunkProvider();
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        if (ModSnowBlock.canContainState(newState)) {
            BlockState oldState = world.getBlockState(pos);
            if (oldState.getBlock() instanceof ModSnowBlock) {
                return ModSnowBlock.convert(world, pos, newState, oldState.get(BlockStateProperties.LAYERS_1_8), flags);
            }
        }
        return world.setBlockState(pos, newState, flags);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isIn(MainModule.TILE_BLOCK)) {
            state = MainModule.TILE_BLOCK.getContainedState(world, pos);
        }
        return state;
    }

    @Override
    public int getLight(BlockPos pos) {
        return world.getLight(pos);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        world.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return world.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return world.getPendingFluidTicks();
    }

    @Override
    public void playEvent(PlayerEntity player, int type, BlockPos pos, int data) {
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return world.getPlayers();
    }

    @Override
    public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playMovingSound(PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {
    }

    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    @Override
    public MapData getMapData(String mapName) {
        return null;
    }

    @Override
    public boolean addEntity(Entity entityIn) {
        entityIn.setWorld(world);
        return world.addEntity(entityIn);
    }

    @Override
    public void registerMapData(MapData mapDataIn) {
    }

    @Override
    public int getNextMapId() {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public Scoreboard getScoreboard() {
        return world.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return world.getRecipeManager();
    }

    @Override
    public DynamicRegistries func_241828_r() {
        return world.func_241828_r();
    }

    @Override
    public Biome getNoiseBiomeRaw(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return world.getNoiseBiomeRaw(p_225604_1_, p_225604_2_, p_225604_3_);
    }

    @Override
    public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_) {
        return world.func_230487_a_(p_230487_1_, p_230487_2_);
    }

    @Override
    public ITagCollectionSupplier getTags() {
        return world.getTags();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return world.getBiomeManager();
    }

    public World getWorld() {
        return world;
    }
}
