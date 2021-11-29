package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import snownee.snow.block.ModSnowBlock;
import snownee.snow.entity.FallingSnowEntity;

public class WorldTickHandler {
	private static Method METHOD;

	static {
		try {
			METHOD = ObfuscationReflectionHelper.findMethod(ChunkManager.class, "func_223491_f");
		} catch (Exception e) {
			SnowRealMagic.LOGGER.catching(e);
		}
	}

	@SuppressWarnings("deprecation")
	public static void tick(TickEvent.WorldTickEvent event) {
		if (SnowCommonConfig.retainOriginalBlocks || METHOD == null) {
			return;
		}
		ServerWorld world = (ServerWorld) event.world;
		int blizzard = SnowCommonConfig.snowGravity ? world.getGameRules().getInt(CoreModule.BLIZZARD_STRENGTH) : 0;
		if (blizzard <= 0 && !world.isRaining()) {
			return;
		}
		if (world.getChunkProvider().getChunkGenerator() instanceof DebugChunkGenerator) {
			return;
		}
		Iterable<ChunkHolder> holders;
		try {
			holders = (Iterable<ChunkHolder>) METHOD.invoke(world.getChunkProvider().chunkManager);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			SnowRealMagic.LOGGER.catching(e);
			METHOD = null;
			return;
		}
		holders.forEach(holder -> {
			Chunk chunk = holder.getChunkIfComplete();
			if (chunk == null || !world.getChunkProvider().isChunkLoaded(chunk.getPos())) {
				return;
			}
			if (world.rand.nextInt(16) == 0) {
				int x = chunk.getPos().getXStart();
				int y = chunk.getPos().getZStart();
				BlockPos.Mutable pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(x, 0, y, 15)).toMutable();

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
				if (!ModSnowBlock.canContainState(state)) {
					state = world.getBlockState(pos.move(Direction.UP));
					if (!ModSnowBlock.canContainState(state)) {
						return;
					}
				}

				if (world.getLightFor(LightType.BLOCK, pos.move(Direction.UP)) > 11) {
					return;
				}
				ModSnowBlock.convert(world, pos.move(Direction.DOWN), state, 1, 3);

				for (int i = 0; i < 5; i++) {
					if (state.isIn(BlockTags.SLABS) || state.isIn(BlockTags.STAIRS)) {
						break;
					}
					state = world.getBlockState(pos.move(Direction.DOWN));
					if (!state.isAir() && !ModSnowBlock.canContainState(state)) {
						break;
					}
					if (CoreModule.BLOCK.isValidPosition(state, world, pos)) {
						pos.move(Direction.UP);
						if (world.getBlockState(pos).getBlock() instanceof SnowBlock || world.getLightFor(LightType.BLOCK, pos) > 11) {
							break;
						}
						ModSnowBlock.convert(world, pos.move(Direction.DOWN), state, 1, 3);
						//FIXME I should make snow melts somehow
					}
				}
			}
		});
	}

	private static void doBlizzard(ServerWorld world, BlockPos pos, int blizzard) {
		if (pos.getY() == world.getHeight()) {
			return;
		}
		int frequency = world.getGameRules().getInt(CoreModule.BLIZZARD_FREQUENCY);
		frequency = MathHelper.clamp(frequency, 0, 10000);
		if (frequency == 0) {
			return;
		}
		int i = world.rand.nextInt(10000);
		if (frequency != 10000 && i >= frequency) {
			return;
		}
		blizzard = MathHelper.clamp(blizzard, 1, 8);
		if (blizzard > 1) {
			blizzard = world.rand.nextInt(blizzard) + 1;
		}
		pos = pos.up(64);
		FallingSnowEntity entity = new FallingSnowEntity(world, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, blizzard);
		world.addEntity(entity);
	}
}
