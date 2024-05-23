package snownee.snow.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import snownee.snow.CoreModule;
import snownee.snow.loot.NormalizeLoot;

public class SRMLootTableProvider extends FabricBlockLootTableProvider {
	protected SRMLootTableProvider(
			FabricDataOutput dataOutput,
			CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(dataOutput, registryLookup);
	}

	@Override
	public void generate() {
		var normalizePool = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(NormalizeLoot.builder());
		add(CoreModule.SNOW_BLOCK.get(), LootTable.lootTable().withPool(normalizePool));
		add(CoreModule.SNOW_PLANT_BLOCK.get(), LootTable.lootTable().withPool(normalizePool));
		add(CoreModule.SNOW_DOUBLEPLANT_LOWER_BLOCK.get(), LootTable.lootTable().withPool(normalizePool));
		add(CoreModule.SNOW_DOUBLEPLANT_UPPER_BLOCK.get(), LootTable.lootTable().withPool(normalizePool));
		add(CoreModule.SNOW_NO_COLLISION_BLOCK.get(), LootTable.lootTable().withPool(normalizePool));
	}
}
