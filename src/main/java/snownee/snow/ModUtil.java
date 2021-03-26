package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.ModList;

public class ModUtil {

    private static Method getBiomeTemperature;
    private static Method enablesSeasonalEffects;
    public static boolean terraforged = false;

    static {
        if (ModList.get().isLoaded("sereneseasons")) {
            try {
                Class<?> seasonHooks = Class.forName("sereneseasons.season.SeasonHooks");
                Class<?> biomeConfig = Class.forName("sereneseasons.config.BiomeConfig");
                getBiomeTemperature = seasonHooks.getDeclaredMethod("getBiomeTemperature", World.class, Biome.class, BlockPos.class);
                enablesSeasonalEffects = biomeConfig.getDeclaredMethod("enablesSeasonalEffects", RegistryKey.class);
                SnowRealMagic.LOGGER.info("Serene Seasons compatibility enabled");
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                SnowRealMagic.LOGGER.catching(e);
            }
        }
    }

    public static boolean shouldMelt(World world, BlockPos pos) {
        if (SnowCommonConfig.snowNeverMelt)
            return false;
        if (world.getLightFor(LightType.BLOCK, pos) > 11)
            return true;
        Biome biome = world.getBiome(pos);
        return snowMeltsInWarmBiomes(biome) && !isColdAt(world, biome, pos) && world.canSeeSky(pos);
    }

    public static boolean isColdAt(World world, Biome biome, BlockPos pos) {
        if (getBiomeTemperature != null) {
            try {
                return (float) getBiomeTemperature.invoke(null, world, biome, pos) < 0.15f;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                SnowRealMagic.LOGGER.catching(e);
                getBiomeTemperature = null;
            }
        }
        return biome.getTemperature(pos) < 0.15f;
    }

    public static boolean snowMeltsInWarmBiomes(Biome biome) {
        if (enablesSeasonalEffects != null) {
            RegistryKey<Biome> biomeKey = RegistryKey.func_240903_a_(Registry.BIOME_KEY, biome.getRegistryName());
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
