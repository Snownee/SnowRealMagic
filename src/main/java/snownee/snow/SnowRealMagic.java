package snownee.snow;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;

public class SnowRealMagic {
	public static final String ID = "snowrealmagic";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
