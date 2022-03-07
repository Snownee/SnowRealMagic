package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;

public class ModUtil {

	public static boolean terraforged;
	public static boolean sereneseasons;

	public static void init() {
		//		sereneseasons = ModList.get().isLoaded("sereneseasons");
		if (sereneseasons) {
			SnowRealMagic.LOGGER.info("SereneSeasons detected. Overriding melting behavior.");
		}
	}

	public static boolean shouldMelt(Level world, BlockPos pos) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (world.getBrightness(LightLayer.BLOCK, pos) >= 10)
			return true;
		Biome biome = world.getBiome(pos).value();
		//		if (sereneseasons && SereneSeasonsCompat.shouldMelt(world, pos, biome)) {
		//			return true;
		//		}
		return snowMeltsInWarmBiomes(biome) && !biome.shouldSnow(world, pos) && world.canSeeSky(pos);
	}

	public static boolean snowMeltsInWarmBiomes(Biome biome) {
		return SnowCommonConfig.snowMeltsInWarmBiomes;
	}

	public static boolean iceMeltsInWarmBiomes(Biome biome) {
		return sereneseasons;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Biome biome) {
		//		if (sereneseasons) {
		//			return SereneSeasonsCompat.coldEnoughToSnow(level, pos, biome);
		//		}
		return biome.coldEnoughToSnow(pos);
	}

}
