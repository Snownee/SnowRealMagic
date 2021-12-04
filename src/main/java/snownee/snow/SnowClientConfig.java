package snownee.snow;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;

@KiwiConfig(type = ConfigType.CLIENT)
public final class SnowClientConfig {

	public static boolean particleThroughLeaves = true;
	public static boolean colorTint = true;

}
