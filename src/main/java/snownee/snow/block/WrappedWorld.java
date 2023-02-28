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
import snownee.snow.CoreModule;

import javax.annotation.Nonnull;

public class WrappedWorld extends World {

	protected World world;

	public WrappedWorld(World world) {
		super((ISpawnWorldInfo) world.getWorldInfo(), world.getDimensionKey(), world.getDimensionType(), world::getProfiler, world.isRemote, world.isDebug(), 0L);
		this.world = world;
	}

	@Nonnull
	@Override
	public AbstractChunkProvider getChunkProvider() {
		return world.getChunkProvider();
	}

	@Override
	public TileEntity getTileEntity(@Nonnull BlockPos pos) {
		return null;
	}

	@Override
	public boolean setBlockState(@Nonnull BlockPos pos, @Nonnull BlockState newState, int flags) {
		if (ModSnowBlock.canContainState(newState)) {
			BlockState oldState = world.getBlockState(pos);
			if (oldState.getBlock() instanceof ModSnowBlock) {
				return ModSnowBlock.convert(world, pos, newState, oldState.get(BlockStateProperties.LAYERS_1_8), flags);
			}
		}
		return world.setBlockState(pos, newState, flags);
	}

	@Nonnull
	@Override
	public BlockState getBlockState(@Nonnull BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isIn(CoreModule.TILE_BLOCK)) {
			state = CoreModule.TILE_BLOCK.getRaw(state, world, pos);
		}
		return state;
	}

	@Override
	public int getLight(@Nonnull BlockPos pos) {
		return world.getLight(pos);
	}

	@Override
	public void notifyBlockUpdate(@Nonnull BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState, int flags) {
		world.notifyBlockUpdate(pos, oldState, newState, flags);
	}

	@Nonnull
	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return world.getPendingBlockTicks();
	}

	@Nonnull
	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return world.getPendingFluidTicks();
	}

	@Override
	public void playEvent(PlayerEntity player, int type, @Nonnull BlockPos pos, int data) {
	}

	@Nonnull
	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return world.getPlayers();
	}

	@Override
	public void playSound(PlayerEntity player, double x, double y, double z, @Nonnull SoundEvent soundIn, @Nonnull SoundCategory category, float volume, float pitch) {
	}

	@Override
	public void playMovingSound(PlayerEntity p_217384_1_, @Nonnull Entity p_217384_2_, @Nonnull SoundEvent p_217384_3_, @Nonnull SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {
	}

	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Override
	public MapData getMapData(@Nonnull String mapName) {
		return null;
	}

	@Override
	public boolean addEntity(Entity entityIn) {
		entityIn.setWorld(world);
		return world.addEntity(entityIn);
	}

	@Override
	public void registerMapData(@Nonnull MapData mapDataIn) {
	}

	@Override
	public int getNextMapId() {
		return 0;
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress) {
	}

	@Nonnull
	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}

	@Nonnull
	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Nonnull
	@Override
	public DynamicRegistries func_241828_r() {
		return world.func_241828_r();
	}

	@Nonnull
	@Override
	public Biome getNoiseBiomeRaw(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getNoiseBiomeRaw(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	@Override
	public float func_230487_a_(@Nonnull Direction p_230487_1_, boolean p_230487_2_) {
		return world.func_230487_a_(p_230487_1_, p_230487_2_);
	}

	@Nonnull
	@Override
	public ITagCollectionSupplier getTags() {
		return world.getTags();
	}

	@Nonnull
	@Override
	public BiomeManager getBiomeManager() {
		return world.getBiomeManager();
	}

	public World getWorld() {
		return world;
	}
}
