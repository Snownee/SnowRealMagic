package snownee.snow.util;

import net.minecraft.data.DataGenerator;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.loader.Platform;
import snownee.snow.GameEvents;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;
import snownee.snow.datagen.SnowBlockTagsProvider;

@Mod(SnowRealMagic.MODID)
public class CommonProxy {
	public CommonProxy() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
			if (SnowCommonConfig.debugSpawningCommand)
				DebugMobSpawningCommand.register(event.getDispatcher());
		});
		MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			InteractionResult result = GameEvents.onItemUse(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
			if (result.consumesAction()) {
				event.setCanceled(true);
				event.setCancellationResult(result);
			}
		});
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener((GatherDataEvent event) -> {
			DataGenerator generator = event.getGenerator();
			SnowBlockTagsProvider blockTagsProvider = new SnowBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
			generator.addProvider(event.includeServer(), blockTagsProvider);
		});

		if (Platform.isPhysicalClient())
			ClientProxy.init();
	}
}
