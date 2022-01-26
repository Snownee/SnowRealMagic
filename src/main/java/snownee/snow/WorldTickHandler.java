package snownee.snow;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import snownee.snow.block.ModSnowLayerBlock;
import snownee.snow.entity.FallingSnowEntity;

public class WorldTickHandler {

	// See ServerLevel.tickChunk
	@SuppressWarnings("deprecation")
	public static void tick(ServerLevel level, LevelChunk chunk, Random random) {
		if (random.nextInt(16) != 0) {
			return;
		}
		int x = chunk.getPos().getMinBlockX();
		int y = chunk.getPos().getMinBlockZ();
		MutableBlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level.getBlockRandomPos(x, 0, y, 15)).mutable();

		if (!level.isAreaLoaded(pos, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
			return;

		pos.move(Direction.DOWN);
		Biome biome = level.getBiome(pos);
		if (biome.shouldFreeze(level, pos)) {
			level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
		}

		BlockState state = null;
		if (level.isRaining()) {
			state = level.getBlockState(pos);
			int blizzard = SnowCommonConfig.snowGravity ? level.getGameRules().getInt(CoreModule.BLIZZARD_STRENGTH) : 0;
			if (blizzard > 0) {
				doBlizzard(level, pos, blizzard);
				return;
			}

			Biome.Precipitation biome$precipitation = level.getBiome(pos).getPrecipitation();
			if (biome$precipitation == Biome.Precipitation.RAIN && biome.coldEnoughToSnow(pos)) {
				biome$precipitation = Biome.Precipitation.SNOW;
			}

			state.getBlock().handlePrecipitation(state, level, pos, biome$precipitation);
		} else {
			return;
		}

		if (!biome.coldEnoughToSnow(pos)) {
			return;
		}
		if (!ModSnowLayerBlock.canContainState(state)) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9 && state.is(Blocks.SNOW)) {
				return;
			}
			state = level.getBlockState(pos.move(Direction.UP));
			if (!state.isAir() && !ModSnowLayerBlock.canContainState(state)) {
				return;
			}
		}

		if (state.isAir() && !Blocks.SNOW.defaultBlockState().canSurvive(level, pos)) {
			return;
		}
		if (level.getBrightness(LightLayer.BLOCK, pos.move(Direction.UP)) >= 10) {
			return;
		}
		ModSnowLayerBlock.convert(level, pos.move(Direction.DOWN), state, 1, 3);

		for (int i = 0; i < 5; i++) {
			if (state.is(BlockTags.SLABS) || state.is(BlockTags.STAIRS)) {
				break;
			}
			state = level.getBlockState(pos.move(Direction.DOWN));
			if (!state.isAir() && !ModSnowLayerBlock.canContainState(state)) {
				break;
			}
			if (CoreModule.BLOCK.canSurvive(state, level, pos)) {
				pos.move(Direction.UP);
				if (level.getBlockState(pos).getBlock() instanceof SnowLayerBlock || level.getBrightness(LightLayer.BLOCK, pos) >= 10) {
					break;
				}
				ModSnowLayerBlock.convert(level, pos.move(Direction.DOWN), state, 1, 3);
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
