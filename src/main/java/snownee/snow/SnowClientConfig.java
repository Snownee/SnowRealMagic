package snownee.snow;

import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig(type = ModConfig.Type.CLIENT)
public final class SnowClientConfig {

	public static boolean particleThroughLeaves = true;
	public static boolean colorTint = true;

}
