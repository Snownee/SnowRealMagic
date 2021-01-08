package snownee.snow.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import snownee.snow.SnowCommonConfig;

public class BlockUtil {
    public static boolean shouldMelt(World world, BlockPos pos) {
        if(SnowCommonConfig.snowNeverMelt)
            return false;
        if(world.getLightFor(LightType.BLOCK, pos) > 11)
            return true;
        return SnowCommonConfig.snowMeltsInWarmBiomes && world.canSeeSky(pos) && world.getBiome(pos).getTemperature(pos) >= 0.15f;
    }
}
