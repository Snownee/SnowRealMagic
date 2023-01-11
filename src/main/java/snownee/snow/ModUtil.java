package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import snownee.kiwi.loader.Platform;
import snownee.snow.compat.sereneseasons.SereneSeasonsCompat;

public class ModUtil {

	public static boolean terraforged;
	public static boolean sereneseasons;

	public static void init() {
		sereneseasons = Platform.isModLoaded("sereneseasons");
		if (sereneseasons) {
			SnowRealMagic.LOGGER.info("SereneSeasons detected. Overriding melting behavior.");
		}
	}

	public static boolean shouldMelt(Level level, BlockPos pos) {
		return shouldMelt(level, pos, level.getBiome(pos));
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (level.getBrightness(LightLayer.BLOCK, pos) >= 10)
			return true;
		if (!level.isDay())
			return false;
		if (sereneseasons && SereneSeasonsCompat.shouldMelt(level, pos, biome)) {
			return true;
		}
		return snowMeltsInWarmBiomes(biome) && biome.value().warmEnoughToRain(pos) && level.canSeeSky(pos);
	}

	public static boolean snowMeltsInWarmBiomes(Holder<Biome> biome) {
		return SnowCommonConfig.snowMeltsInWarmBiomes;
	}

	public static boolean iceMeltsInWarmBiomes(Holder<Biome> biome) {
		return sereneseasons;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneseasons) {
			return SereneSeasonsCompat.coldEnoughToSnow(level, pos, biome);
		}
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneseasons) {
			return SereneSeasonsCompat.isWinter(level, pos, biome);
		}
		return false;
	}

}
