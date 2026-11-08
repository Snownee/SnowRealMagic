package snownee.snow;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import snownee.snow.block.SnowVariant;
import snownee.snow.mixin.IceBlockAccess;
import snownee.snow.util.CommonProxy;

public class WorldTickHandler {
	private static final int PROBE_LIFETIME = 16;
	private static final int MAX_PROBES_PER_LEVEL = 256;
	private static final float PROBE_LATERAL_CHANCE = 0.5F;

	public interface ProbeHolder {
		List<Probe> srm$probes();
	}

	public static final class Probe {
		private final MutableBlockPos pos;
		private int age;
		private int snow;

		private Probe(BlockPos pos) {
			this.pos = pos.mutable();
		}

		private boolean tick(ServerLevel level) {
			if (!level.isLoaded(pos)) {
				return true;
			}
			BlockState state = level.getBlockState(pos);
			if (SnowAccumulation.isSupported(state)) {
				snow = SnowAccumulation.get(level.getChunkAt(pos), pos);
			}
			RandomSource random = level.getRandom();
			BlockPos below = pos.below();
			if (tryIncrease(level, below)) {
				pos.set(below);
			} else {
				BlockPos target = null;
				if (level.isLoaded(below) && level.getBlockState(below).isAir()) {
					target = tryIncreaseLowerNeighbor(level, pos, random);
				}
				if (target == null && random.nextFloat() < PROBE_LATERAL_CHANCE) {
					target = tryIncreaseNeighbor(level, pos, random);
				}
				if (target != null) {
					pos.set(target);
				} else if (canProbeEnter(level, below)) {
					pos.set(below);
				}
			}
			return ++age >= PROBE_LIFETIME;
		}

		private static boolean tryIncrease(ServerLevel level, BlockPos pos) {
			if (!level.isLoaded(pos)) {
				return false;
			}
			BlockState state = level.getBlockState(pos);
			if (!SnowAccumulation.isSupported(state)) {
				return false;
			}
			if (tooBrightToAccumulate(level, pos)) {
				return false;
			}
			LevelChunk chunk = level.getChunkAt(pos);
			int max = SnowAccumulation.maxSnowState(level, pos);
			int current = SnowAccumulation.get(chunk, pos);
			if (current >= max) {
				return false;
			}
			SnowAccumulation.set(chunk, pos, current + 1);
			return true;
		}

		private static @Nullable BlockPos tryIncreaseNeighbor(ServerLevel level, BlockPos pos, RandomSource random) {
			int offset = random.nextInt(4);
			for (int i = 0; i < 4; i++) {
				Direction direction = Direction.from2DDataValue((offset + i) & 3);
				BlockPos target = pos.relative(direction);
				if (tryIncrease(level, target)) {
					return target;
				}
			}
			BlockPos above = pos.above();
			if (tryIncrease(level, above)) {
				return above;
			}
			return null;
		}

		private @Nullable BlockPos tryIncreaseLowerNeighbor(ServerLevel level, BlockPos pos, RandomSource random) {
			if (snow <= 0) {
				return null;
			}
			int offset = random.nextInt(4);
			for (int i = 0; i < 4; i++) {
				Direction direction = Direction.from2DDataValue((offset + i) & 3);
				BlockPos target = pos.relative(direction);
				if (!level.isLoaded(target)) {
					continue;
				}
				BlockState targetState = level.getBlockState(target);
				if (!SnowAccumulation.isSupported(targetState)) {
					continue;
				}
				if (SnowAccumulation.get(level.getChunkAt(target), target) >= snow) {
					continue;
				}
				if (tryIncrease(level, target)) {
					return target;
				}
			}
			return null;
		}

		private static boolean canProbeEnter(ServerLevel level, BlockPos pos) {
			if (!level.isLoaded(pos)) {
				return false;
			}
			BlockState state = level.getBlockState(pos);
			return state.isAir() || state.is(Blocks.SNOW) || SnowAccumulation.isSupported(state);
		}
	}

	// See ServerLevel.tickChunk
	public static boolean tick(ServerLevel level, BlockPos pos) {
		MutableBlockPos mutable = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).mutable();
		boolean coldEnoughToSnow = CommonProxy.coldEnoughToSnow(level, mutable, level.getBiome(mutable));
		if (coldEnoughToSnow) {
			return doSnow(level, mutable);
		} else {
			doMelt(level, mutable);
			return false;
		}
	}

	private static void doMelt(ServerLevel level, MutableBlockPos pos) {
		BlockState blockState = level.getBlockState(pos.move(Direction.DOWN));
		if (blockState.getBlock() instanceof IceBlockAccess ice) {
			Holder<Biome> biome = level.getBiome(pos);
			if (CommonProxy.snowAndIceMeltInWarmBiomes(level.dimension(), biome) && biome.value().warmEnoughToRain(
					pos,
					level.getSeaLevel())) {
				ice.callMelt(blockState, level, pos);
			}
			return;
		}
		if (blockState.getBlock() instanceof SnowVariant) {
			Hooks.randomTick(blockState, level, pos, level.getRandom(), 1);
		}
		BlockState stateAbove = level.getBlockState(pos.move(Direction.UP));
		if (stateAbove.getBlock() instanceof SnowVariant) {
			Hooks.randomTick(stateAbove, level, pos, level.getRandom(), 1);
		}
	}

	private static boolean doSnow(ServerLevel level, MutableBlockPos pos) {
		if (!level.isRaining()) {
			return false;
		}
		if (SnowCommonConfig.blockSnowAccumulation) {
			accumulateLeafSnow(level, pos);
		}
		int blizzard = SnowCommonConfig.snowGravity ? level.getGameRules().get(CoreModule.BLIZZARD_STRENGTH) : 0;
		if (blizzard > 0) {
			doBlizzard(level, pos, blizzard);
			return true;
		}

		if (SnowCommonConfig.snowAccumulationMaxLayers <= 0) {
			return false;
		}

		if (!snowHereIfPossible(level, pos, level.getBlockState(pos))) {
			if (!snowHereIfPossible(level, pos, level.getBlockState(pos.move(Direction.DOWN)))) {
				return false;
			}
		}

		Hooks.convertBelow(level, pos, Block.UPDATE_ALL, SnowCommonConfig.placeSnowOnBlockNaturally);
		return true;
	}

	private static void accumulateLeafSnow(ServerLevel level, BlockPos topPos) {
		MutableBlockPos cursor = topPos.mutable();
		BlockPos leafPos = null;
		for (int i = 0; i < 8; i++) {
			cursor.move(Direction.DOWN);
			if (!level.isLoaded(cursor)) {
				return;
			}
			BlockState state = level.getBlockState(cursor);
			if (SnowAccumulation.isSupported(state)) {
				leafPos = cursor.immutable();
				break;
			}
			if (!state.isAir() && !(state.getBlock() instanceof SnowLayerBlock)) {
				return;
			}
		}
		if (leafPos == null) {
			return;
		}
		if (tooBrightToAccumulate(level, leafPos)) {
			return;
		}
		int max = SnowAccumulation.maxSnowState(level, leafPos);
		if (max <= 0) {
			return;
		}
		LevelChunk chunk = level.getChunkAt(leafPos);
		int current = SnowAccumulation.get(chunk, leafPos);
		if (current < max) {
			current++;
			SnowAccumulation.set(chunk, leafPos, current);
		}
		if (SnowCommonConfig.blockSnowSpread && level.getRandom().nextFloat() * 100F < Mth.clamp(
				SnowCommonConfig.blockSnowSpreadChance,
				0,
				100)) {
			addProbe(level, leafPos);
		}
	}

	private static void addProbe(ServerLevel level, BlockPos pos) {
		List<Probe> probes = ((ProbeHolder) level).srm$probes();
		if (probes.size() < MAX_PROBES_PER_LEVEL) {
			probes.add(new Probe(pos));
		}
	}

	public static void tickProbes(ServerLevel level) {
		List<Probe> probes = ((ProbeHolder) level).srm$probes();
		if (probes.isEmpty()) {
			return;
		}
		if (!SnowCommonConfig.blockSnowSpread || !level.isRaining()) {
			probes.clear();
			return;
		}
		probes.removeIf(probe -> probe.tick(level));
	}

	/**
	 * Melts accumulated snow that is exposed to strong block light or that sits in a biome warm enough to rain.
	 * Positions are sampled like vanilla random ticks (once per section per tick speed), so the selection chance of a
	 * single position stays constant no matter how many snowy blocks the chunk contains.
	 */
	public static void tickMelt(ServerLevel level, LevelChunk chunk, int tickSpeed) {
		if (SnowCommonConfig.snowNeverMelt || tickSpeed <= 0) {
			return;
		}
		if (SnowAccumulation.isEmpty(chunk)) {
			return;
		}
		RandomSource random = level.getRandom();
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();
		MutableBlockPos pos = new MutableBlockPos();
		for (int section = 0; section < chunk.getSectionsCount(); section++) {
			int minY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(section));
			for (int i = 0; i < tickSpeed; i++) {
				pos.set(minX + random.nextInt(16), minY + random.nextInt(16), minZ + random.nextInt(16));
				int current = SnowAccumulation.get(chunk, pos);
				if (current > 0 && (CommonProxy.blockLightEnoughToMelt(level, pos) ||
						CommonProxy.shouldMeltInWarmBiome(level, pos, current))) {
					SnowAccumulation.set(chunk, pos, current - 1);
				}
			}
		}
	}

	private static boolean tooBrightToAccumulate(ServerLevel level, BlockPos pos) {
		return level.getBrightness(LightLayer.BLOCK, pos) > SnowCommonConfig.snowSpawnMaxLightLevel;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean snowHereIfPossible(ServerLevel level, MutableBlockPos pos, BlockState blockState) {
		if (level.getBrightness(LightLayer.BLOCK, pos.move(Direction.UP)) > SnowCommonConfig.snowSpawnMaxLightLevel) {
			pos.move(Direction.DOWN);
			return false;
		}
		return Hooks.convert(level, pos.move(Direction.DOWN), blockState, 1, Block.UPDATE_ALL, SnowCommonConfig.placeSnowOnBlockNaturally);
	}

	private static void doBlizzard(ServerLevel world, BlockPos pos, int blizzard) {
		if (pos.getY() == world.getHeight()) {
			return;
		}
		int frequency = world.getGameRules().get(CoreModule.BLIZZARD_FREQUENCY);
		frequency = Mth.clamp(frequency, 0, 10000);
		if (frequency == 0) {
			return;
		}
		int i = world.getRandom().nextInt(10000);
		if (frequency != 10000 && i >= frequency) {
			return;
		}
		blizzard = Mth.clamp(blizzard, 1, 8);
		if (blizzard > 1) {
			blizzard = world.getRandom().nextInt(blizzard) + 1;
		}
		pos = pos.above(64);
		FallingBlockEntity.fall(world, pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, blizzard));
	}

}
