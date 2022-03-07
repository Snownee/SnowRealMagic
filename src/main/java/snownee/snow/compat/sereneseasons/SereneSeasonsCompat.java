/*package snownee.snow.compat.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.config.ServerConfig;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsCompat {

	public static boolean shouldMelt(Level level, BlockPos pos, Biome biome) {
		if (!BiomeConfig.enablesSeasonalEffects(level.getBiomeName(pos).orElse(null))) {
			return false;
		}
		if (!((Boolean) SeasonsConfig.generateSnowAndIce.get()).booleanValue() || !ServerConfig.isDimensionWhitelisted(level.dimension())) {
			return false;
		}
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		Season season = subSeason.getSeason();
		if (season == Season.WINTER) {
			return false;
		}
		int meltRand;
		switch (subSeason) {
		case EARLY_SPRING:
			meltRand = 16;
			break;
		case MID_SPRING:
			meltRand = 12;
			break;
		case LATE_SPRING:
			meltRand = 8;
			break;
		default:
			meltRand = 4;
			break;
		}
		return level.random.nextInt(meltRand >> 1) == 0 && !coldEnoughToSnow(level, pos, biome);
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Biome biome) {
		return SeasonHooks.getBiomeTemperature(level, biome, pos) < 0.15F;
	}

}
*/