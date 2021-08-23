package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

@Mixin(ItemBlock.class)
public interface ItemBlockAccessor {
	@Accessor("block")
	void setBlock(Block block);
}
