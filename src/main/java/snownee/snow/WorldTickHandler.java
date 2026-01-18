package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import snownee.snow.block.SnowVariant;
import snownee.snow.mixin.IceBlockAccess;
import snownee.snow.util.CommonProxy;

public class WorldTickHandler {

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
		int blizzard = SnowCommonConfig.snowGravity ? level.getGameRules().get(CoreModule.BLIZZARD_STRENGTH) : 0;
		if (blizzard > 0) {
			doBlizzard(level, pos, blizzard);
			return true;
		}

		if (SnowCommonConfig.snowAccumulationMaxLayers <= 0) {
			return false;
		}

		BlockState blockState = level.getBlockState(pos);
		if (!snowHereIfPossible(level, pos, blockState)) {
			if (!snowHereIfPossible(level, pos, blockState = level.getBlockState(pos.move(Direction.DOWN)))) {
				return false;
			}
		}

		for (int i = 0; i < 5; i++) {
			if (blockState.is(BlockTags.SLABS) || blockState.is(BlockTags.STAIRS)) {
				break;
			}
			blockState = level.getBlockState(pos.move(Direction.DOWN));
			if (!blockState.isAir() && !Hooks.canContainState(blockState)) {
				break;
			}
			if (Hooks.canSnowSurvive(level, pos)) {
				pos.move(Direction.UP);
				if (level.getBlockState(pos).getBlock() instanceof SnowLayerBlock || level.getBrightness(LightLayer.BLOCK, pos) >
						SnowCommonConfig.snowSpawnMaxLightLevel) {
					break;
				}
				Hooks.convert(level, pos.move(Direction.DOWN), blockState, 1, Block.UPDATE_ALL, SnowCommonConfig.placeSnowOnBlockNaturally);
				//FIXME I should make snow melts somehow
			}
		}
		return true;
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
