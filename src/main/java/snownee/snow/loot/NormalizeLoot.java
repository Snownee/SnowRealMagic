package snownee.snow.loot;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.mixin.LootContextAccess;
import snownee.snow.mixin.LootParamsAccess;

public class NormalizeLoot extends LootPoolSingletonContainer {

	private NormalizeLoot(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) {
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	public static LootPoolSingletonContainer.Builder<?> builder() {
		return simpleBuilder(NormalizeLoot::new);
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
		BlockEntity tile = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
		if (tile instanceof SnowBlockEntity) {
			BlockState state = ((SnowBlockEntity) tile).getContainedState();
			if (!state.isAir()) {
				ResourceLocation resourcelocation = state.getBlock().getLootTable();
				if (resourcelocation != BuiltInLootTables.EMPTY) {
					LootParams.Builder builder = new LootParams.Builder(context.getLevel());
					builder.withParameter(LootContextParams.BLOCK_STATE, state);
					((LootParamsAccess) ((LootContextAccess) context).getParams()).getParams().forEach((p, v) -> {
						if (p != LootContextParams.BLOCK_STATE && p != LootContextParams.BLOCK_ENTITY) {
							builder.withOptionalParameter((LootContextParam<Object>) p, v);
						}
					});
					builder.withLuck(context.getLuck());
					state.getDrops(builder).forEach(consumer);
				}
			}
		}
	}

	@Override
	public LootPoolEntryType getType() {
		return CoreModule.NORMALIZE.get();
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<NormalizeLoot> {
		@Override
		protected NormalizeLoot deserialize(
				JsonObject json,
				JsonDeserializationContext context,
				int weightIn,
				int qualityIn,
				LootItemCondition[] conditionsIn,
				LootItemFunction[] functionsIn) {
			return new NormalizeLoot(weightIn, qualityIn, conditionsIn, functionsIn);
		}
	}
}
