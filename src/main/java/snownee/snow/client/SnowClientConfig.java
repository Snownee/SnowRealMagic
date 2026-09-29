package snownee.snow.client;

import net.minecraft.client.Minecraft;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;

@KiwiConfig(type = ConfigType.CLIENT)
public final class SnowClientConfig {

	public static boolean particleThroughLeaves = true;

	public static boolean snowVariants = true;

	@KiwiConfig.Listen("snowVariants")
	public static void toggleSnowVariants(String key) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null) {
			minecraft.levelRenderer.invalidateCompiledGeometry(
					minecraft.level, minecraft.options, minecraft.gameRenderer.mainCamera(), minecraft.getBlockColors());
		}
	}

}
