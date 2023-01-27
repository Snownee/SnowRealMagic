package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import snownee.kiwi.loader.Platform;

public class ModUtil {

	public static boolean terraforged = false;
	public static boolean fabricSeasons = Platform.isModLoaded("seasons");

	public static boolean shouldMelt(Level level, BlockPos pos) {
		return shouldMelt(level, pos, level.getBiome(pos));
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (!level.isDay())
			return false;
		if (SnowCommonConfig.snowNaturalMelt)
			return true;
		if (snowMeltsInWarmBiomes(biome) && biome.value().warmEnoughToRain(pos)) {
			return fabricSeasons ? level.getBrightness(LightLayer.SKY, pos) > 0 : level.canSeeSky(pos);
		}
		return false;
	}

	public static boolean snowMeltsInWarmBiomes(Holder<Biome> biome) {
		return SnowCommonConfig.snowMeltsInWarmBiomes;
	}

	public static boolean iceMeltsInWarmBiomes(Holder<Biome> biome) {
		return fabricSeasons;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		return false;
	}
}
