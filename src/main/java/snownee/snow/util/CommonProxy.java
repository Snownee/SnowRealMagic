package snownee.snow.util;

import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;

@Mod(SnowRealMagic.MODID)
public class CommonProxy {
	public CommonProxy() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, event -> {
			if (SnowCommonConfig.debugSpawningCommand)
				DebugMobSpawningCommand.register(event.getDispatcher());
		});
	}
}
