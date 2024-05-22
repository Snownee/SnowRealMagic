package snownee.snow.loot;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

public class NormalizeLoot extends LootPoolSingletonContainer {
	public static final MapCodec<NormalizeLoot> CODEC = RecordCodecBuilder.mapCodec(
			instance -> singletonFields(instance)
					.apply(instance, NormalizeLoot::new)
	);

	private NormalizeLoot(int weightIn, int qualityIn, List<LootItemCondition> conditionsIn, List<LootItemFunction> functionsIn) {
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
				var lootTable = state.getBlock().getLootTable();
				if (lootTable != BuiltInLootTables.EMPTY) {
					LootParams.Builder builder = new LootParams.Builder(context.getLevel());
					builder.withParameter(LootContextParams.BLOCK_STATE, state);
					context.params.params.forEach((p, v) -> {
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
	public @NotNull LootPoolEntryType getType() {
		return CoreModule.NORMALIZE.get();
	}
}
