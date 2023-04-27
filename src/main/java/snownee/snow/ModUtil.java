package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import snownee.kiwi.loader.Platform;
import snownee.snow.compat.sereneseasons.SereneSeasonsCompat;

//TODO: move to CommonProxy
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
		return shouldMelt(level, pos, level.getBiome(pos), 1);
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome, int layers) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (!level.isDay())
			return false;
		if (sereneseasons)
			return SereneSeasonsCompat.shouldMelt(level, pos, biome);
		if (snowAndIceMeltInWarmBiomes(level.dimension(), biome) && biome.value().warmEnoughToRain(pos) && level.canSeeSky(layers == 8 ? pos.above() : pos))
			return true;
		if (layers == 1) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9)
				return false;
			if (!(level.getBlockState(pos.below()).getBlock() instanceof SnowLayerBlock))
				return false;
		}
		return SnowCommonConfig.snowNaturalMelt;
	}

	public static boolean snowAndIceMeltInWarmBiomes(ResourceKey<Level> dimension, Holder<Biome> biome) {
		if (SnowCommonConfig.snowAndIceMeltInWarmBiomes) {
			return true;
		}
		if (sereneseasons) {
			return SereneSeasonsCompat.isSeasonal(dimension, biome);
		}
		return false;
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
