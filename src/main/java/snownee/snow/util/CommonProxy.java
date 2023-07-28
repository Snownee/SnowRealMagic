package snownee.snow.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import snownee.kiwi.Mod;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;

@Mod(SnowRealMagic.MODID)
public class CommonProxy {
	public CommonProxy() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (SnowCommonConfig.debugSpawningCommand)
				DebugMobSpawningCommand.register(dispatcher);
		});
	}
}
