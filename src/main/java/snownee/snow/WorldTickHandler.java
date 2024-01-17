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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import snownee.snow.block.SnowVariant;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.mixin.IceBlockAccess;
import snownee.snow.util.CommonProxy;

public class WorldTickHandler {

	// See ServerLevel.tickChunk
	public static void tick(ServerLevel level, LevelChunk chunk) {
		int x = chunk.getPos().getMinBlockX();
		int y = chunk.getPos().getMinBlockZ();
		MutableBlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level.getBlockRandomPos(x, 0, y, 15)).mutable();

		pos.move(Direction.DOWN);
		Holder<Biome> biomeHolder = level.getBiome(pos);
		boolean coldEnoughToSnow = CommonProxy.coldEnoughToSnow(level, pos, biomeHolder);
		if (coldEnoughToSnow) {
			doSnow(level, pos);
		} else {
			doMelt(level, pos);
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

	private static void doSnow(ServerLevel level, MutableBlockPos pos) {
		if (!level.isRaining()) {
			return;
		}
		int blizzard = SnowCommonConfig.snowGravity ? level.getGameRules().getInt(CoreModule.BLIZZARD_STRENGTH) : 0;
		if (blizzard > 0) {
			doBlizzard(level, pos, blizzard);
			return;
		}

		BlockState state = level.getBlockState(pos);
		if (SnowCommonConfig.snowAccumulationMaxLayers <= 0) {
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
