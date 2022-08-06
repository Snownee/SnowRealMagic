package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import snownee.kiwi.loader.Platform;

public class ModUtil {

	public static boolean terraforged = false;
	public static boolean fabricSeasons = Platform.isModLoaded("seasons");

	public static boolean shouldMelt(Level world, BlockPos pos) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (fabricSeasons && world.getBrightness(LightLayer.SKY, pos) > 0 && world.getBiome(pos).value().warmEnoughToRain(pos))
			return true;
		if (world.getBrightness(LightLayer.BLOCK, pos) >= 10)
			return true;
		Biome biome = world.getBiome(pos).value();
		return snowMeltsInWarmBiomes(world, biome) && !biome.shouldSnow(world, pos) && world.canSeeSky(pos);
	}

	public static boolean snowMeltsInWarmBiomes(CommonLevelAccessor level, Biome biome) {
		return SnowCommonConfig.snowMeltsInWarmBiomes;
	}
}
