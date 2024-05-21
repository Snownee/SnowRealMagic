package snownee.snow.compat.jade;

import static snownee.snow.CoreModule.FENCE;
import static snownee.snow.CoreModule.FENCE2;
import static snownee.snow.CoreModule.FENCE_GATE;
import static snownee.snow.CoreModule.SLAB;
import static snownee.snow.CoreModule.SNOW_BLOCK;
import static snownee.snow.CoreModule.SNOW_DOUBLEPLANT_LOWER_BLOCK;
import static snownee.snow.CoreModule.SNOW_DOUBLEPLANT_UPPER_BLOCK;
import static snownee.snow.CoreModule.SNOW_NO_COLLISION_BLOCK;
import static snownee.snow.CoreModule.SNOW_PLANT_BLOCK;
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
				SNOW_BLOCK,
				SNOW_NO_COLLISION_BLOCK,
				SNOW_PLANT_BLOCK,
				SNOW_DOUBLEPLANT_LOWER_BLOCK,
				SNOW_DOUBLEPLANT_UPPER_BLOCK,
				FENCE,
				FENCE2,
				STAIRS,
				SLAB,
				FENCE_GATE,
				WALL))
			registration.usePickedResult(block.get());
	}

}