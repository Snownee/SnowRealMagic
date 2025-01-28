package snownee.snow.compat.jade;

import static snownee.snow.CoreModule.FENCE;
import static snownee.snow.CoreModule.FENCE2;
import static snownee.snow.CoreModule.FENCE_GATE;
import static snownee.snow.CoreModule.SLAB;
import static snownee.snow.CoreModule.SOUL_TORCH;
import static snownee.snow.CoreModule.SOUL_WALL_TORCH;
import static snownee.snow.CoreModule.STAIRS;
import static snownee.snow.CoreModule.TILE_BLOCK;
import static snownee.snow.CoreModule.TORCH;
import static snownee.snow.CoreModule.WALL;
import static snownee.snow.CoreModule.WALL_TORCH;

import java.util.List;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.kiwi.KiwiGO;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		for (KiwiGO<? extends Block> block : List.of(TILE_BLOCK, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL, TORCH, SOUL_TORCH, WALL_TORCH, SOUL_WALL_TORCH))
			registration.usePickedResult(block.get());
	}

}
