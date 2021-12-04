package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import snownee.snow.block.ModSnowLayerBlock;
import snownee.snow.entity.FallingSnowEntity;

public class WorldTickHandler {
	private static Method METHOD;

	static {
		try {
			METHOD = ObfuscationReflectionHelper.findMethod(ChunkMap.class, "m_140416_"); //getChunks
		} catch (Exception e) {
			SnowRealMagic.LOGGER.catching(e);
		}
	}

	@SuppressWarnings("deprecation")
	public static void tick(TickEvent.WorldTickEvent event) {
		if (SnowCommonConfig.retainOriginalBlocks || METHOD == null) {
			return;
		}
		ServerLevel world = (ServerLevel) event.world;
		int blizzard = SnowCommonConfig.snowGravity ? world.getGameRules().getInt(CoreModule.BLIZZARD_STRENGTH) : 0;
		if (blizzard <= 0 && !world.isRaining()) {
			return;
		}
		if (world.isDebug()) {
			return;
		}
		Iterable<ChunkHolder> holders;
		try {
			holders = (Iterable<ChunkHolder>) METHOD.invoke(world.getChunkSource().chunkMap);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			SnowRealMagic.LOGGER.catching(e);
			METHOD = null;
			return;
		}
		holders.forEach(holder -> {
			LevelChunk chunk = holder.getTickingChunk();
			if (chunk == null || !world.shouldTickBlocksAt(chunk.getPos().toLong())) {
				return;
			}
			// See ServerLevel.tickChunk
			if (world.random.nextInt(16) == 0) {
				int x = chunk.getPos().getMinBlockX();
				int y = chunk.getPos().getMinBlockZ();
				MutableBlockPos pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, world.getBlockRandomPos(x, 0, y, 15)).mutable();

				if (!world.isAreaLoaded(pos, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
					return;

				if (blizzard > 0) {
					doBlizzard(world, pos, blizzard);
					return;
				}

				pos.move(Direction.DOWN);
				Biome biome = world.getBiome(pos);
				if (!ModUtil.isColdAt(world, biome, pos)) {
					return;
				}
				BlockState state = world.getBlockState(pos);
				if (!ModSnowLayerBlock.canContainState(state)) {
					state = world.getBlockState(pos.move(Direction.UP));
					if (!ModSnowLayerBlock.canContainState(state)) {
						return;
					}
				}

				if (world.getBrightness(LightLayer.BLOCK, pos.move(Direction.UP)) > 11) {
					return;
				}
				ModSnowLayerBlock.convert(world, pos.move(Direction.DOWN), state, 1, 3);

				for (int i = 0; i < 5; i++) {
					if (state.is(BlockTags.SLABS) || state.is(BlockTags.STAIRS)) {
						break;
					}
					state = world.getBlockState(pos.move(Direction.DOWN));
					if (!state.isAir() && !ModSnowLayerBlock.canContainState(state)) {
						break;
					}
					if (CoreModule.BLOCK.canSurvive(state, world, pos)) {
						pos.move(Direction.UP);
						if (world.getBlockState(pos).getBlock() instanceof SnowLayerBlock || world.getBrightness(LightLayer.BLOCK, pos) > 11) {
							break;
						}
						ModSnowLayerBlock.convert(world, pos.move(Direction.DOWN), state, 1, 3);
						//FIXME I should make snow melts somehow
					}
				}
			}
		});
	}

	private static void doBlizzard(ServerLevel world, BlockPos pos, int blizzard) {
		if (pos.getY() == world.getHeight()) {
			return;
		}
		int frequency = world.getGameRules().getInt(CoreModule.BLIZZARD_FREQUENCY);
		frequency = Mth.clamp(frequency, 0, 10000);
		if (frequency == 0) {
			return;
		}
		int i = world.random.nextInt(10000);
		if (frequency != 10000 && i >= frequency) {
			return;
		}
		blizzard = Mth.clamp(blizzard, 1, 8);
		if (blizzard > 1) {
			blizzard = world.random.nextInt(blizzard) + 1;
		}
		pos = pos.above(64);
		FallingSnowEntity entity = new FallingSnowEntity(world, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, blizzard);
		world.addFreshEntity(entity);
	}
}
