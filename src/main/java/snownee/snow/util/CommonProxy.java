package snownee.snow.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import snownee.kiwi.Mod;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;

@Mod(SnowRealMagic.MODID)
public class CommonProxy implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (SnowCommonConfig.debugSpawningCommand)
				DebugMobSpawningCommand.register(dispatcher);
		});
	}
}
