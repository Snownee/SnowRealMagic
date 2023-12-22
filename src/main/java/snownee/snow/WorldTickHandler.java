package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import snownee.snow.block.SnowVariant;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.mixin.IceBlockAccess;

public class WorldTickHandler {

	// See ServerLevel.tickChunk
	public static void tick(ServerLevel level, LevelChunk chunk, RandomSource random) {
		if (random.nextInt(SnowCommonConfig.weatherTickSlowness) != 0) {
			return;
		}
		int x = chunk.getPos().getMinBlockX();
		int y = chunk.getPos().getMinBlockZ();
		MutableBlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level.getBlockRandomPos(x, 0, y, 15)).mutable();

		//		if (!level.isAreaLoaded(pos, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
		//			return;

		pos.move(Direction.DOWN);
		Holder<Biome> biomeHolder = level.getBiome(pos);
		Biome biome = biomeHolder.value();
		boolean coldEnoughToSnow = ModUtil.coldEnoughToSnow(level, pos, biomeHolder);
		BlockState state = null;
		if (biome.shouldFreeze(level, pos)) {
			level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
		} else if (ModUtil.snowAndIceMeltInWarmBiomes(level.dimension(), biomeHolder)) {
			state = level.getBlockState(pos);
			if (state.is(Blocks.ICE) && !coldEnoughToSnow && level.canSeeSky(pos.above())) {
				((IceBlockAccess) state.getBlock()).callMelt(state, level, pos);
				state = level.getBlockState(pos);
			}
		}

		if (level.isRaining()) {
			if (state == null)
				state = level.getBlockState(pos);
			int blizzard = SnowCommonConfig.snowGravity ? level.getGameRules().getInt(CoreModule.BLIZZARD_STRENGTH) : 0;
			if (blizzard > 0) {
				doBlizzard(level, pos, blizzard);
				return;
			}

			Biome.Precipitation biome$precipitation = biome.getPrecipitationAt(pos);
			if (biome$precipitation != Biome.Precipitation.NONE) {
				state.getBlock().handlePrecipitation(state, level, pos, biome$precipitation);
			}
		} else {
			return;
		}

		if (!coldEnoughToSnow) {
			return;
		}
		if (!Hooks.canContainState(state)) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9 && state.getBlock() instanceof SnowVariant) {
				return;
			}
			state = level.getBlockState(pos.move(Direction.UP));
			if (!state.isAir() && !Hooks.canContainState(state)) {
				return;
			}
		}

		if (state.isAir() && !Hooks.canSnowSurvive(Blocks.SNOW.defaultBlockState(), level, pos)) {
			return;
		}
		if (level.getBrightness(LightLayer.BLOCK, pos.move(Direction.UP)) >= 10) {
			return;
		}
		Hooks.convert(level, pos.move(Direction.DOWN), state, 1, 3, SnowCommonConfig.placeSnowOnBlockNaturally);

		for (int i = 0; i < 5; i++) {
			if (state.is(BlockTags.SLABS) || state.is(BlockTags.STAIRS)) {
				break;
			}
			state = level.getBlockState(pos.move(Direction.DOWN));
			if (!state.isAir() && !Hooks.canContainState(state)) {
				break;
			}
			if (Hooks.canSnowSurvive(Blocks.SNOW.defaultBlockState(), level, pos)) {
				pos.move(Direction.UP);
				if (level.getBlockState(pos).getBlock() instanceof SnowLayerBlock || level.getBrightness(LightLayer.BLOCK, pos) >= 10) {
					break;
				}
				Hooks.convert(level, pos.move(Direction.DOWN), state, 1, 3, SnowCommonConfig.placeSnowOnBlockNaturally);
				//FIXME I should make snow melts somehow
			}
		}

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