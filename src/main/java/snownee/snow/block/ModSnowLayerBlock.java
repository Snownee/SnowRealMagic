package snownee.snow.block;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;

@ScheduledForRemoval(inVersion = "1.19.3")
public class ModSnowLayerBlock {

	public static boolean convert(LevelAccessor world, BlockPos pos, BlockState state, int layers, int flags) {
		return Hooks.convert(world, pos, state, layers, flags, SnowCommonConfig.placeSnowInBlockNaturally);
	}

}
