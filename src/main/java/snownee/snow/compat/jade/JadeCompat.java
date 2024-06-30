package snownee.snow.compat.jade;

import static snownee.snow.CoreModule.FENCE;
import static snownee.snow.CoreModule.FENCE2;
import static snownee.snow.CoreModule.FENCE_GATE;
import static snownee.snow.CoreModule.SLAB;
import static snownee.snow.CoreModule.SNOWY_DOUBLE_PLANT_LOWER;
import static snownee.snow.CoreModule.SNOWY_DOUBLE_PLANT_UPPER;
import static snownee.snow.CoreModule.SNOWY_PLANT;
import static snownee.snow.CoreModule.SNOW_BLOCK;
import static snownee.snow.CoreModule.SNOW_EXTRA_COLLISION_BLOCK;
import static snownee.snow.CoreModule.STAIRS;
import static snownee.snow.CoreModule.WALL;

import java.util.List;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		for (var block : List.of(
				SNOW_EXTRA_COLLISION_BLOCK,
				SNOW_BLOCK,
				SNOWY_PLANT,
				SNOWY_DOUBLE_PLANT_LOWER,
				SNOWY_DOUBLE_PLANT_UPPER,
				FENCE,
				FENCE2,
				STAIRS,
				SLAB,
				FENCE_GATE,
				WALL))
			registration.usePickedResult(block.get());
	}
}