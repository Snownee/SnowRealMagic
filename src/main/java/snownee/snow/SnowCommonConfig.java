package snownee.snow;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Listen;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.PerformanceImpact;
import snownee.kiwi.config.KiwiConfig.PerformanceType;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.loader.Platform;
import snownee.snow.block.ShapeCaches;
import snownee.snow.util.CommonProxy;

@KiwiConfig
public final class SnowCommonConfig {

	@PerformanceImpact(PerformanceType.LOW)
	public static boolean snowGravity = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean snowMakingIce = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean snowAlwaysReplaceable = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean thinnerBoundingBox = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean snowNeverMelt = false;
	@PerformanceImpact(PerformanceType.NONE)
	public static int snowSpawnMaxLightLevel = 9;
	@PerformanceImpact(PerformanceType.NONE)
	public static int snowPersistMaxLightLevel = 11;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean snowReduceFallDamage = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean sustainGrassIfLayerMoreThanOne = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean sneakSnowball = true;
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean fancySnowOnUpperSlab = true;
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean rightClickToggleFancySnow = true;
	@Range(min = 1, max = 8)
	@ConfigUI.Slider
	@PerformanceImpact(PerformanceType.NONE)
	public static int mobSpawningMaxLayers = 8;
	@Path("snow-cover.placeSnowOnBlock")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean placeSnowOnBlock = true;
	@Path("snow-cover.placeNaturally")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean placeSnowOnBlockNaturally = true;
	@Path("snow-cover.replaceWorldgenFeature")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean replaceWorldFeature = true;
	@Path("snow-cover.restoreOriginalBlocks")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean restoreOriginalBlocks = false;
	@Path("accumulation.accumulatesDuringSnowfall")
	@PerformanceImpact(PerformanceType.MEDIUM)
	public static boolean snowAccumulationDuringSnowfall = false;
	@Path("accumulation.accumulatesDuringSnowstorm")
	@PerformanceImpact(PerformanceType.MEDIUM)
	public static boolean snowAccumulationDuringSnowstorm = true;
	@Path("accumulation.maxLayers")
	@Range(min = 0, max = 9)
	@ConfigUI.Slider
	@PerformanceImpact(PerformanceType.LOW)
	public static int snowAccumulationMaxLayers = 6;
	@Path("accumulation.snowAndIceMeltInWarmBiomes")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean snowAndIceMeltInWarmBiomes = false;
	@Path("accumulation.naturalMelting")
	@PerformanceImpact(PerformanceType.MEDIUM)
	public static boolean snowNaturalMelt = true;
	@Path("accumulation.smoothAccumulation")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean smoothAccumulation = true;

	@Path("integration.accumulationWinterOnly")
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean accumulationWinterOnly = false;

	@Path("debug.forceVanillaIceSnowLogic")
	public static boolean forceVanillaIceSnowLogic = false;
	@Path("debug.mobSpawningCommand")
	public static boolean debugSpawningCommand = false;

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean canPlaceSnowInBlock() {
		return placeSnowOnBlock && !restoreOriginalBlocks;
	}

	@Listen("thinnerBoundingBox")
	public static void onThinnerBoundingBoxChange(String path) {
		ShapeCaches.invalidateAll();
		//noinspection ConstantValue
		if (Platform.isPhysicalClient() && Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
			for (Block block : CommonProxy.allSnowBlocks()) {
				for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
					blockState.initCache();
				}
			}
		}
	}
}
