package snownee.snow.loot;

import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import snownee.snow.block.entity.SnowBlockEntity;

public class NormalizeLoot extends SingleEntryContainerBase {
	public static final MapCodec<NormalizeLoot> CODEC = RecordCodecBuilder.mapCodec(
			instance -> uniformFields(instance)
					.apply(instance, NormalizeLoot::new)
	);

	private NormalizeLoot(
			int weight,
			int quality,
			Optional<Holder<LootItemCondition>> conditions,
			Optional<Holder<LootItemFunction>> functions) {
		super(weight, quality, conditions, functions);
	}

	@Override
	public MapCodec<? extends SingleEntryContainerBase> codec() {
		return CODEC;
	}

	public static UniformContainerBase.Builder<?> builder() {
		return simpleBuilder(NormalizeLoot::new);
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
		BlockEntity tile = context.getOptional(LootContextParams.BLOCK_ENTITY);
		if (!(tile instanceof SnowBlockEntity)) {
			return;
		}
		BlockState state = ((SnowBlockEntity) tile).getContainedState();
		var lootTable = state.getBlock().getLootTable();
		if (lootTable.isEmpty()) {
			return;
		}
		LootParams.Builder builder = new LootParams.Builder(context.getLevel());
		builder.withParameter(LootContextParams.BLOCK_STATE, state);
		context.params.contextMap().params.forEach((p, v) -> {
			if (p != LootContextParams.BLOCK_STATE && p != LootContextParams.BLOCK_ENTITY) {
				//noinspection unchecked,NullableProblems
				builder.withOptionalParameter((ContextKey<Object>) p, v);
			}
		});
		builder.withLuck(context.getLuck());
		state.getDrops(builder).forEach(output);
	}
}