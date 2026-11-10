package snownee.snow.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig(type = ConfigType.CLIENT)
public final class SnowClientConfig {

	public static boolean particleThroughLeaves = true;

	public static boolean snowVariants = true;

	@Path("visual.snowyLeavesIntensity")
	@Range(min = 0, max = 100)
	@ConfigUI.Slider
	public static int snowyLeavesIntensity = 0;

	@Path("visual.snowyLeavesStateIntensities")
	public static List<Number> snowyLeavesStateIntensities = new ArrayList<>(List.of(0, 13, 25, 38, 50, 63, 75, 88, 100));

	@Path("visual.snowyLeavesMinOpacity")
	@Range(min = 0, max = 100)
	@ConfigUI.Slider
	public static int snowyLeavesMinOpacity = 30;

	@Path("visual.snowyLeavesBrightnessInfluence")
	@Range(min = 0, max = 100)
	@ConfigUI.Slider
	public static int snowyLeavesBrightnessInfluence = 100;

	@Path("visual.snowyLeavesSideGradient")
	@Range(min = 0, max = 100)
	@ConfigUI.Slider
	public static int snowyLeavesSideGradient = 100;

	@Path("visual.snowyLeavesSideGradientSteps")
	@Range(min = 1, max = 16)
	@ConfigUI.Slider
	public static int snowyLeavesSideGradientSteps = 4;

	@Path("visual.snowyLeavesSideTopBoost")
	@Range(min = 0, max = 100)
	@ConfigUI.Slider
	public static int snowyLeavesSideTopBoost = 100;

	@KiwiConfig.Listen("snowVariants")
	public static void toggleSnowVariants(String key) {
		if (Minecraft.getInstance().level != null) {
			Minecraft.getInstance().levelExtractor.allChanged();
		}
	}

	@KiwiConfig.Listen("visual.snowyLeavesIntensity")
	@KiwiConfig.Listen("visual.snowyLeavesStateIntensities")
	@KiwiConfig.Listen("visual.snowyLeavesMinOpacity")
	@KiwiConfig.Listen("visual.snowyLeavesBrightnessInfluence")
	@KiwiConfig.Listen("visual.snowyLeavesSideGradient")
	@KiwiConfig.Listen("visual.snowyLeavesSideGradientSteps")
	@KiwiConfig.Listen("visual.snowyLeavesSideTopBoost")
	public static void onSnowyLeavesConfigChange(String key) {
		SnowyLeaves.scheduleReload();
	}

}
