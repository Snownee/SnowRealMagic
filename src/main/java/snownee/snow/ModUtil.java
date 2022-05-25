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

	public static boolean shouldMelt(Level world, BlockPos pos) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (world.getBrightness(LightLayer.BLOCK, pos) >= 10)
			return true;
		Holder<Biome> biome = world.getBiome(pos);
		if (sereneseasons && SereneSeasonsCompat.shouldMelt(world, pos, biome)) {
			return true;
		}
		return snowMeltsInWarmBiomes(biome) && !biome.value().shouldSnow(world, pos) && world.canSeeSky(pos);
	}

	public static boolean snowMeltsInWarmBiomes(Holder<Biome> biome) {
		return SnowCommonConfig.snowMeltsInWarmBiomes;
	}

	public static boolean iceMeltsInWarmBiomes(Biome biome) {
		return sereneseasons;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneseasons) {
			return SereneSeasonsCompat.coldEnoughToSnow(level, pos, biome);
		}
		return biome.value().coldEnoughToSnow(pos);
	}

}
