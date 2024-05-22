package snownee.snow;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.KUtil;

public class SnowRealMagic {
	public static final String MODID = "snowrealmagic";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourceLocation id(final String path) {
		return KUtil.RL(path, MODID);
	}
}
