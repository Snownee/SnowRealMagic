package snownee.snow.util;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class CommonProxy {

	@SuppressWarnings("rawtypes")
	public static LootContext.Builder copyLootContext(LootContext context) {
		LootContext.Builder builder = new LootContext.Builder(context.getLevel());
		for (LootContextParam param : LootContextParamSets.BLOCK.getAllowed()) {
			if (param == LootContextParams.BLOCK_ENTITY) {
				continue;
			}
			builder.withOptionalParameter(param, context.getParamOrNull(param));
		}
		builder.withRandom(context.getRandom());
		builder.withLuck(context.getLuck());
		return builder;
	}

}
