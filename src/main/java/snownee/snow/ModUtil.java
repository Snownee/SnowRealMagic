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
		if (world.getBrightness(LightLayer.BLOCK, pos) >= 10)
			return true;
		Biome biome = world.getBiome(pos);
		return snowMeltsInWarmBiomes(world, biome) && !biome.shouldSnow(world, pos) && world.canSeeSky(pos);
	}

	public static boolean snowMeltsInWarmBiomes(CommonLevelAccessor level, Biome biome) {
		return fabricSeasons || SnowCommonConfig.snowMeltsInWarmBiomes;
	}
}
