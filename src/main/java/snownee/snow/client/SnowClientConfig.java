package snownee.snow.client;

import net.minecraft.client.Minecraft;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.ConfigType;

@KiwiConfig(type = ConfigType.CLIENT)
public final class SnowClientConfig {

	public static boolean particleThroughLeaves = true;
	@Comment("Some snow-covered foliage will be rendered differently")
	public static boolean snowVariants = true;

	public static void onChanged(String key) {
		if ("snowVariants".equals(key)) {
			Minecraft.getInstance().levelRenderer.allChanged();
		}
	}

}
