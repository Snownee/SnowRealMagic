/*
package snownee.snow.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import snownee.kiwi.util.GameObjectLookup;
import snownee.snow.SnowRealMagic;
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
		LootTable.Builder table = LootTable.lootTable().withPool(normalizePool);
		GameObjectLookup.all(Registries.BLOCK, SnowRealMagic.ID).forEach(block -> add(block, table));
	}
}
*/
