package snownee.snow.loot;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import snownee.kiwi.tile.TextureTile;
import snownee.snow.MainModule;

public class NormalLootEntry extends StandaloneLootEntry {

    private NormalLootEntry(int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void func_216154_a(Consumer<ItemStack> consumer, LootContext context) {
        TileEntity tile = context.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TextureTile) {
            Item item = ((TextureTile) tile).getMark("0");
            if (item != null) {
                consumer.accept(new ItemStack(item));
            }
        }
    }

    public static StandaloneLootEntry.Builder<?> builder(IItemProvider itemIn) {
        return builder((weight, quality, conditions, functions) -> {
            return new NormalLootEntry(weight, quality, conditions, functions);
        });
    }

    @Override
    public LootPoolEntryType func_230420_a_() {
        return MainModule.NORMAL;
    }

    public static class Serializer extends StandaloneLootEntry.Serializer<NormalLootEntry> {
        @Override
        public void func_230422_a_(JsonObject json, NormalLootEntry lootEntry, JsonSerializationContext context) {
            super.func_230422_a_(json, lootEntry, context);
        }

        @Override
        protected NormalLootEntry func_212829_b_(JsonObject json, JsonDeserializationContext context, int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn) {
            return new NormalLootEntry(weightIn, qualityIn, conditionsIn, functionsIn);
        }
    }
}
