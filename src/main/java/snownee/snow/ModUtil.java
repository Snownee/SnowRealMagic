package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import snownee.kiwi.loader.Platform;

public class ModUtil {

	private static Method enablesSeasonalEffects;
	public static boolean terraforged = false;

	static {
		if (Platform.isModLoaded("sereneseasons")) {
			try {
				Class<?> seasonHooks = Class.forName("sereneseasons.season.SeasonHooks");
				Class<?> biomeConfig = Class.forName("sereneseasons.config.BiomeConfig");
				seasonHooks.getDeclaredMethod("getBiomeTemperature", Level.class, Biome.class, BlockPos.class);
				enablesSeasonalEffects = biomeConfig.getDeclaredMethod("enablesSeasonalEffects", ResourceKey.class);
				SnowRealMagic.LOGGER.info("Serene Seasons compatibility enabled");
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
				SnowRealMagic.LOGGER.catching(e);
			}
		}
	}

	public static boolean shouldMelt(Level world, BlockPos pos) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (world.getBrightness(LightLayer.BLOCK, pos) >= 10)
			return true;
		Biome biome = world.getBiome(pos);
		return snowMeltsInWarmBiomes(biome) && !biome.shouldSnow(world, pos) && world.canSeeSky(pos);
	}

	public static boolean snowMeltsInWarmBiomes(Biome biome) {
		if (enablesSeasonalEffects != null) {
			ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName());
			try {
				return (boolean) enablesSeasonalEffects.invoke(null, biomeKey);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				SnowRealMagic.LOGGER.catching(e);
				enablesSeasonalEffects = null;
			}
		}
		return SnowCommonConfig.snowMeltsInWarmBiomes;
	}
}
