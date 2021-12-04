package snownee.snow.loot;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

public class NormalLootEntry extends LootPoolSingletonContainer {

	private NormalLootEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) {
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
		BlockEntity tile = context.getParam(LootContextParams.BLOCK_ENTITY);
		if (tile instanceof SnowBlockEntity) {
			BlockState state = ((SnowBlockEntity) tile).getState();
			if (!state.isAir()) {
				ResourceLocation resourcelocation = state.getBlock().getLootTable();
				if (resourcelocation != BuiltInLootTables.EMPTY) {
					LootContext.Builder builder = new LootContext.Builder(context);
					LootContext lootcontext = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
					ServerLevel serverworld = lootcontext.getLevel();
					LootTable loottable = serverworld.getServer().getLootTables().get(resourcelocation);
					loottable.getRandomItems(lootcontext).forEach(consumer::accept);
				}
			}
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder(ItemLike itemIn) {
		return simpleBuilder(NormalLootEntry::new);
	}

	@Override
	public LootPoolEntryType getType() {
		return CoreModule.NORMAL;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<NormalLootEntry> {
		@Override
		protected NormalLootEntry deserialize(JsonObject json, JsonDeserializationContext context, int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) {
			return new NormalLootEntry(weightIn, qualityIn, conditionsIn, functionsIn);
		}
	}
}
