package snownee.snow.compat.jade;

import static snownee.snow.CoreModule.*;

import java.util.List;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.world.level.block.Block;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		for (Block block : List.of(TILE_BLOCK, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL))
			registration.usePickedResult(block);
	}

}
