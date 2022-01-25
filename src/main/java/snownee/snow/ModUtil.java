package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import snownee.kiwi.loader.Platform;

public class ModUtil {

	private static Method getBiomeTemperature;
	private static Method enablesSeasonalEffects;
	public static boolean terraforged = false;
	public static boolean fabricSeasons = Platform.isModLoaded("seasons");

	static {
		if (Platform.isModLoaded("sereneseasons")) {
			try {
				Class<?> seasonHooks = Class.forName("sereneseasons.season.SeasonHooks");
				Class<?> biomeConfig = Class.forName("sereneseasons.config.BiomeConfig");
				getBiomeTemperature = seasonHooks.getDeclaredMethod("getBiomeTemperature", Level.class, Biome.class, BlockPos.class);
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
		if (world.getBrightness(LightLayer.BLOCK, pos) > 11)
			return true;
		Biome biome = world.getBiome(pos);
		return snowMeltsInWarmBiomes(world, biome) && !isColdAt(world, biome, pos) && world.canSeeSky(pos);
	}

	public static boolean isColdAt(Level world, Biome biome, BlockPos pos) {
		if (getBiomeTemperature != null) {
			try {
				return (float) getBiomeTemperature.invoke(null, world, biome, pos) < 0.15f;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				SnowRealMagic.LOGGER.catching(e);
				getBiomeTemperature = null;
			}
		}
		return biome.coldEnoughToSnow(pos);
	}

	public static boolean snowMeltsInWarmBiomes(CommonLevelAccessor level, Biome biome) {
		if (enablesSeasonalEffects != null) {
			ResourceKey<Biome> biomeKey = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(biome).orElseThrow();
			try {
				return (boolean) enablesSeasonalEffects.invoke(null, biomeKey);
			} catch (Exception e) {
				SnowRealMagic.LOGGER.catching(e);
				enablesSeasonalEffects = null;
			}
		}
		return fabricSeasons || SnowCommonConfig.snowMeltsInWarmBiomes;
	}
}
