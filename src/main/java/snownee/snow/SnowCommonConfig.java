package snownee.snow;

import java.util.ArrayList;
import java.util.List;

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
	@Path("accumulation.worldGenSnowAndFreezeEverywhere")
	@PerformanceImpact(PerformanceType.HIGH)
	public static boolean worldGenSnowAndFreezeEverywhere = false;
	@Path("accumulation.initialSnowOnLeaves")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean initialSnowOnLeaves = true;
	@Path("accumulation.supportedBlocksWhitelist")
	@ConfigUI.Typed(String.class)
	public static List<String> supportedBlockWhitelist = new ArrayList<>(List.of(
			".*:.*_leaves",
			".*:vine",
			".*:.*_vine",
			"biomesoplenty:dead_branch"));
	@Path("accumulation.supportedBlocksBlacklist")
	@ConfigUI.Typed(String.class)
	public static List<String> supportedBlockBlacklist = new ArrayList<>();
	@Path("accumulation.initialSnowDownfallThresholds")
	public static List<Number> initialSnowDownfallThresholds = new ArrayList<>(List.of(
			// Convex curve: layers rise quickly at low downfall and flatten out at high downfall.
			0.02D,
			0.06D,
			0.12D,
			0.21D,
			0.33D,
			0.48D,
			0.66D,
			0.88D));
	@Path("accumulation.blockSnowAccumulation")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean blockSnowAccumulation = true;
	@Path("accumulation.blockSnowSpread")
	@PerformanceImpact(PerformanceType.LOW)
	public static boolean blockSnowSpread = true;
	@Path("accumulation.blockSnowSpreadChance")
	@Range(min = 0, max = 100)
	@ConfigUI.Slider
	@PerformanceImpact(PerformanceType.LOW)
	public static int blockSnowSpreadChance = 80;

	@Path("integration.accumulationWinterOnly")
	@PerformanceImpact(PerformanceType.NONE)
	public static boolean accumulationWinterOnly = false;

	@Path("debug.forceVanillaIceSnowLogic")
	public static boolean forceVanillaIceSnowLogic = false;
	@Path("debug.preventWorldgenLogSpam")
	public static boolean preventWorldgenLogSpam = true;
	@Path("debug.mobSpawningCommand")
	public static boolean debugSpawningCommand = false;
	@Path("debug.logBlockError")
	public static boolean logBlockError = false;

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean canPlaceSnowInBlock() {
		return placeSnowOnBlock && !restoreOriginalBlocks;
	}

	@Listen("accumulation.supportedBlocksWhitelist")
	@Listen("accumulation.supportedBlocksBlacklist")
	public static void onSupportedBlocksChange(String path) {
		SnowAccumulation.invalidateSupportedBlocks();
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
