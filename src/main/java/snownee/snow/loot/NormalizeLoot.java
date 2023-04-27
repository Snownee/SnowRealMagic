package snownee.snow.loot;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.util.CommonProxy;

public class NormalizeLoot extends LootPoolSingletonContainer {

	private NormalizeLoot(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) {
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
		BlockEntity tile = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
		if (tile instanceof SnowBlockEntity) {
			BlockState state = ((SnowBlockEntity) tile).getState();
			if (!state.isAir()) {
				ResourceLocation resourcelocation = state.getBlock().getLootTable();
				if (resourcelocation != BuiltInLootTables.EMPTY) {
					LootContext.Builder builder = CommonProxy.copyLootContext(context);
					LootContext lootcontext = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
					LootTable loottable = lootcontext.getLevel().getServer().getLootTables().get(resourcelocation);
					loottable.getRandomItems(lootcontext).forEach(consumer::accept);
				}
			}
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder(ItemLike itemIn) {
		return simpleBuilder(NormalizeLoot::new);
	}

	@Override
	public LootPoolEntryType getType() {
		return CoreModule.NORMALIZE.get();
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<NormalizeLoot> {
		@Override
		protected NormalizeLoot deserialize(JsonObject json, JsonDeserializationContext context, int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) {
			return new NormalizeLoot(weightIn, qualityIn, conditionsIn, functionsIn);
		}
	}
}
