package snownee.snow.util;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class CommonProxy {

	public static LootContext.Builder copyLootContext(LootContext context) {
		LootContext.Builder builder = new LootContext.Builder(context);
		builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
		return builder;
	}

}
