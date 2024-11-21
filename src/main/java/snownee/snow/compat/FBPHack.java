package snownee.snow.compat;

import java.util.List;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import snownee.snow.SnowRealMagic;

public final class FBPHack {
	@SubscribeEvent(priority = EventPriority.LOW)
	public void hackFBP(WorldEvent.Load event) {
		try {
			Class<?> FBPClass = Class.forName("com.TominoCZ.FBP.FBP");
			Object mod = FBPClass.getDeclaredField("INSTANCE").get(null);
			List<String> list = (List<String>) FBPClass.getDeclaredField("blockAnimBlacklist").get(mod);
			if (list.contains("minecraft:snow_layer"))
				return;
			list.add("minecraft:snow_layer");
			Class<?> FBPConfigHandlerClass = Class.forName("com.TominoCZ.FBP.handler.FBPConfigHandler");
			FBPConfigHandlerClass.getDeclaredMethod("writeAnimExceptions").invoke(null);
		} catch (Throwable e) {
			SnowRealMagic.logger.catching(e);
		}
	}
}
