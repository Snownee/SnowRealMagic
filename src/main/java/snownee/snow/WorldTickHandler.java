package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import snownee.snow.block.SnowVariant;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.mixin.IceBlockAccess;
import snownee.snow.util.CommonProxy;

public class WorldTickHandler {

	// See ServerLevel.tickChunk
	public static boolean tick(ServerLevel level, BlockPos pos) {
		MutableBlockPos mutable = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).mutable();
		Holder<Biome> biomeHolder = level.getBiome(mutable);
		boolean coldEnoughToSnow = CommonProxy.coldEnoughToSnow(level, mutable, biomeHolder);
		if (coldEnoughToSnow) {
			return doSnow(level, mutable);
		} else {
			doMelt(level, mutable);
			return false;
		}
	}

	private static void doMelt(ServerLevel level, MutableBlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof IceBlockAccess ice) {
			Holder<Biome> biome = level.getBiome(pos);
			if (CommonProxy.snowAndIceMeltInWarmBiomes(level.dimension(), biome) && biome.value().warmEnoughToRain(pos)) {
				ice.callMelt(state, level, pos);
			}
			return;
		}
		BlockState stateAbove = level.getBlockState(pos.move(Direction.UP));
		if (stateAbove.getBlock() instanceof SnowVariant) {
			Hooks.randomTick(stateAbove, level, pos, level.random, 1);
		} else if (state.getBlock() instanceof SnowVariant) {
			pos.move(Direction.DOWN);
			Hooks.randomTick(state, level, pos, level.random, 1);
		}
	}

	private static boolean doSnow(ServerLevel level, MutableBlockPos pos) {
		if (!level.isRaining()) {
			return false;
		}
		int blizzard = SnowCommonConfig.snowGravity ? level.getGameRules().getInt(CoreModule.BLIZZARD_STRENGTH) : 0;
		if (blizzard > 0) {
			doBlizzard(level, pos, blizzard);
			return true;
		}

		if (SnowCommonConfig.snowAccumulationMaxLayers <= 0) {
			return false;
		}

		BlockState state = level.getBlockState(pos);
		if (!Hooks.canContainState(state) && !Hooks.canSnowSurvive(state, level, pos)) {
			return false;
		}
		if (level.getBrightness(LightLayer.BLOCK, pos.move(Direction.UP)) > SnowCommonConfig.snowSpawnMaxLightLevel) {
			return false;
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
				if (level.getBlockState(pos).getBlock() instanceof SnowLayerBlock || level.getBrightness(LightLayer.BLOCK, pos) >
						SnowCommonConfig.snowSpawnMaxLightLevel) {
					break;
				}
				Hooks.convert(level, pos.move(Direction.DOWN), state, 1, 3, SnowCommonConfig.placeSnowOnBlockNaturally);
				//FIXME I should make snow melts somehow
			}
		}
		return true;
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
