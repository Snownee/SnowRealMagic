package snownee.snow.compat.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.config.ServerConfig;
import sereneseasons.init.ModTags;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsCompat {

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome) {
		if (biome.is(ModTags.Biomes.BLACKLISTED_BIOMES)) {
			return false;
		}
		if (!SeasonsConfig.generateSnowAndIce.get() || !ServerConfig.isDimensionWhitelisted(level.dimension())) {
			return false;
		}
		if (biome.is(ModTags.Biomes.TROPICAL_BIOMES)) {
			return true;
		}
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		Season season = subSeason.getSeason();
		if (season == Season.WINTER) {
			return false;
		}
		int meltRand = switch (subSeason) {
			case EARLY_SPRING -> 16;
			case MID_SPRING -> 12;
			case LATE_SPRING -> 8;
			default -> 4;
		};
		return level.random.nextInt(meltRand >> 1) == 0 && !coldEnoughToSnow(level, pos, biome);
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		return SeasonHooks.getBiomeTemperature(level, biome, pos) < 0.15F;
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		if (!isSeasonal(level.dimension(), biome)) {
			return false;
		}
		return SeasonHelper.getSeasonState(level).getSeason() == Season.WINTER;
	}

	public static boolean isSeasonal(ResourceKey<Level> dimension, Holder<Biome> biome) {
		return !biome.is(ModTags.Biomes.BLACKLISTED_BIOMES) && !biome.is(ModTags.Biomes.TROPICAL_BIOMES) && ServerConfig.isDimensionWhitelisted(dimension);
	}
}
