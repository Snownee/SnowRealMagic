package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;

public class SnowTextureTile extends SnowTile {

	public SnowTextureTile() {
		super(CoreModule.TEXTURE_TILE);
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		if (compound.contains("Items")) {
			String idStr = compound.getCompound("Items").getString("0");
			ResourceLocation id = Util.RL(idStr);
			if (id != null) {
				Item item = ForgeRegistries.ITEMS.getValue(id);
				if (item instanceof BlockItem) {
					BlockState state0 = ((BlockItem) item).getBlock().getDefaultState();
					setState(state0, false);
				}
			}
		}
		super.read(state, compound);
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		setState(ModSnowBlock.copyProperties(getBlockState(), state));
	}

}
