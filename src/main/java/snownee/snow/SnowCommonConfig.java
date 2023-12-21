package snownee.snow;

import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.loader.Platform;

@KiwiConfig
public final class SnowCommonConfig {

	public static boolean snowGravity = true;
	public static boolean snowMakingIce = true;
	public static boolean snowAlwaysReplaceable = true;
	public static boolean thinnerBoundingBox = true;
	public static boolean snowNeverMelt = false;
	public static boolean snowReduceFallDamage = true;
	public static boolean sustainGrassIfLayerMoreThanOne = true;
	public static boolean sneakSnowball = true;
	public static boolean fancySnowOnUpperSlab = true;
	@Range(min = 1, max = 8)
	@ConfigUI.Slider
	public static int mobSpawningMaxLayers = 8;
	@Path("snow-cover.placeSnowOnBlock")
	public static boolean placeSnowOnBlock = true;
	@Path("snow-cover.placeNaturally")
	public static boolean placeSnowOnBlockNaturally = true;
	@Path("snow-cover.replaceWorldgenFeature")
	public static boolean replaceWorldFeature = true;
	@Path("snow-cover.retainOriginalBlocks")
	public static boolean retainOriginalBlocks = false;
	@Path("accumulation.accumulatesDuringSnowfall")
	public static boolean snowAccumulationDuringSnowfall = false;
	@Path("accumulation.accumulatesDuringSnowstorm")
	public static boolean snowAccumulationDuringSnowstorm = true;
	@Path("accumulation.maxLayers")
	@Range(min = 0, max = 9)
	@ConfigUI.Slider
	public static int snowAccumulationMaxLayers = 8;
	@Path("accumulation.snowAndIceMeltInWarmBiomes")
	public static boolean snowAndIceMeltInWarmBiomes = false;
	@Path("accumulation.naturalMelting")
	public static boolean snowNaturalMelt = !Platform.isModLoaded("terraforged");

	@Path("integration.accumulationWinterOnly")
	public static boolean accumulationWinterOnly = false;
	@Path("debug.mobSpawningCommand")
	public static boolean debugSpawningCommand = false;
	@Path("debug.weatherTickSlowness")
	@Range(min = 1)
	public static int weatherTickSlowness = 16;

	public static boolean canPlaceSnowInBlock() {
		return placeSnowOnBlock && !retainOriginalBlocks;
	}

}
