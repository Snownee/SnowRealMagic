package snownee.snow.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import snownee.kiwi.datagen.KiwiLanguageProvider;
import snownee.snow.SnowRealMagic;

@EventBusSubscriber
public class SRMDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(SRMBlockTagsProvider::new);
		pack.addProvider(SRMLootTableProvider::new);
		pack.addProvider(KiwiLanguageProvider::new);
	}

	@SubscribeEvent
	public static void on(GatherDataEvent event) {
		//noinspection UnstableApiUsage
		FabricDataGenHelper.runDatagenForMod(SnowRealMagic.ID, SnowRealMagic.ID, new SRMDataGen(), event);
	}
}
