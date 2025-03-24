package snownee.snow;

import snownee.kiwi.KiwiModule.Skip;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig
public final class SnowCommonConfig {

	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean snowGravity = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean snowMakingIce = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean snowAlwaysReplaceable = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean thinnerBoundingBox = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean snowNeverMelt = false;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static int snowSpawnMaxLightLevel = 9;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static int snowPersistMaxLightLevel = 11;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean snowReduceFallDamage = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean sustainGrassIfLayerMoreThanOne = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean sneakSnowball = true;
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean fancySnowOnUpperSlab = true;
	@Range(min = 1, max = 8)
	@ConfigUI.Slider
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static int mobSpawningMaxLayers = 8;
	@Path("snow-cover.placeSnowOnBlock")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean placeSnowOnBlock = true;
	@Path("snow-cover.placeNaturally")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean placeSnowOnBlockNaturally = true;
	@Path("snow-cover.replaceWorldgenFeature")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean replaceWorldFeature = true;
	@Path("snow-cover.restoreOriginalBlocks")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean restoreOriginalBlocks = false;
	@Path("accumulation.accumulatesDuringSnowfall")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.MEDIUM)
	public static boolean snowAccumulationDuringSnowfall = false;
	@Path("accumulation.accumulatesDuringSnowstorm")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.MEDIUM)
	public static boolean snowAccumulationDuringSnowstorm = true;
	@Path("accumulation.maxLayers")
	@Range(min = 0, max = 9)
	@ConfigUI.Slider
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static int snowAccumulationMaxLayers = 6;
	@Path("accumulation.snowAndIceMeltInWarmBiomes")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean snowAndIceMeltInWarmBiomes = false;
	@Path("accumulation.naturalMelting")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.MEDIUM)
	public static boolean snowNaturalMelt = true;
	@Path("accumulation.smoothAccumulation")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.LOW)
	public static boolean smoothAccumulation = true;

	@Skip // Unavailable on Fabric
	@Path("integration.accumulationWinterOnly")
	@KiwiConfig.PerformanceImpact(KiwiConfig.PerformanceType.NONE)
	public static boolean accumulationWinterOnly = false;

	@Path("debug.forceVanillaIceSnowLogic")
	public static boolean forceVanillaIceSnowLogic = false;
	@Path("debug.mobSpawningCommand")
	public static boolean debugSpawningCommand = false;

	public static boolean canPlaceSnowInBlock() {
		return placeSnowOnBlock && !restoreOriginalBlocks;
	}
}
