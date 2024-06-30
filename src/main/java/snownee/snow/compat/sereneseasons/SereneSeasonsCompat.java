package snownee.snow.compat.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.init.ModConfig;
import sereneseasons.init.ModTags;
import sereneseasons.season.SeasonHooks;
import snownee.snow.SnowCommonConfig;

public class SereneSeasonsCompat {

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome) {
		if (!snowAndIceMeltInWarmBiomes(level.dimension(), biome)) {
			return false;
		}
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		SeasonsConfig.SeasonProperties meltInfo = ModConfig.seasons.getSeasonProperties(subSeason);
		if (meltInfo == null) {
			return false;
		}
		float meltChance = meltInfo.meltChance() * meltInfo.meltRolls() * 0.01f;
		return meltChance > 0 && level.random.nextFloat() < meltChance && !coldEnoughToSnow(level, pos, biome);
	}

	public static boolean snowAndIceMeltInWarmBiomes(ResourceKey<Level> dimension, Holder<Biome> biome) {
		if (!ModConfig.seasons.generateSnowAndIce) {
			return false;
		}
		if (biome.is(ModTags.Biomes.BLACKLISTED_BIOMES)) {
			return false;
		}
		if (!ModConfig.seasons.isDimensionWhitelisted(dimension)) {
			return false;
		}
		return true;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		if (ModConfig.seasons.generateSnowAndIce) {
			return SeasonHooks.getBiomeTemperature(level, biome, pos) < 0.15F;
		}
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		if (!isSeasonal(level.dimension(), biome)) {
			return false;
		}
		return SeasonHelper.getSeasonState(level).getSeason() == Season.WINTER;
	}

	public static boolean isSeasonal(ResourceKey<Level> dimension, Holder<Biome> biome) {
		return !biome.is(ModTags.Biomes.BLACKLISTED_BIOMES) && !biome.is(ModTags.Biomes.TROPICAL_BIOMES) &&
				ModConfig.seasons.isDimensionWhitelisted(dimension);
	}

	public static void weatherTick(ServerLevel level, Runnable action) {
		if (!ModConfig.seasons.isDimensionWhitelisted(level.dimension())) {
			return;
		}
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		// we assume that winter is always snowy
		if (subSeason.getSeason() == Season.WINTER) {
			if (level.random.nextInt(SnowCommonConfig.weatherTickSlowness) == 0) {
				action.run();
			}
			return;
		}
		SeasonsConfig.SeasonProperties meltInfo = ModConfig.seasons.getSeasonProperties(subSeason);
		if (meltInfo == null) {
			action.run();
			return;
		}
		int meltRolls = meltInfo.meltRolls();
		if (meltRolls == 0) {
			return;
		}
		float meltChance = meltInfo.meltChance() * 0.01f;
		if (meltChance == 0) {
			return;
		}
		for (int i = 0; i < meltRolls; i++) {
			if (level.random.nextFloat() < meltChance) {
				action.run();
			}
		}
	}
}