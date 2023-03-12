package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import snownee.kiwi.loader.Platform;

public class ModUtil {

	public static boolean terraforged = false;
	public static boolean fabricSeasons = Platform.isModLoaded("seasons");

	public static boolean shouldMelt(Level level, BlockPos pos) {
		return shouldMelt(level, pos, level.getBiome(pos), 1);
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome, int layers) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (!level.isDay())
			return false;
		if (snowAndIceMeltInWarmBiomes(biome) && biome.value().warmEnoughToRain(pos)) {
			BlockPos pos2 = layers == 8 ? pos.above() : pos;
			if (fabricSeasons) {
				return level.getBrightness(LightLayer.SKY, pos2) > 0;
			} else {
				return level.canSeeSky(pos2);
			}
		}
		if (layers == 1) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9)
				return false;
			if (!(level.getBlockState(pos.below()).getBlock() instanceof SnowLayerBlock))
				return false;
		}
		return SnowCommonConfig.snowNaturalMelt;
	}

	public static boolean snowAndIceMeltInWarmBiomes(Holder<Biome> biome) {
		return fabricSeasons || SnowCommonConfig.snowAndIceMeltInWarmBiomes;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		return false;
	}
}
