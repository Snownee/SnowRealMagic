package snownee.snow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;

import java.util.Objects;

public class SnowTextureTile extends SnowTile {

	public SnowTextureTile() {
		super(CoreModule.TEXTURE_TILE);
		options.renderOverlay = true;
	}

	@Override
	public void loadState(BlockState state, CompoundNBT data, boolean network) {
		boolean changed = false;
		if (!network && data.contains("Items")) {
			String idStr = data.getCompound("Items").getString("0");
			ResourceLocation id = Util.RL(idStr);
			if (id != null) {
				Item item = ForgeRegistries.ITEMS.getValue(id);
				if (item instanceof BlockItem) {
					Block block = ((BlockItem) item).getBlock();
					changed |= setState(ModSnowBlock.copyProperties(state, block.getDefaultState()), network);
				}
			}
		} else if (data.contains("Block")) {
			ResourceLocation id = Util.RL(data.getString("Block"));
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block != null && block != Blocks.AIR) {
				changed |= setState(ModSnowBlock.copyProperties(state, block.getDefaultState()), network);
			}
		} else {
			changed |= setState(NBTUtil.readBlockState(data.getCompound("State")), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	@Override
	public void saveState(CompoundNBT data, boolean network) {
		data.putString("Block", Objects.requireNonNull(getState().getBlock().getRegistryName()).toString());
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		setState(ModSnowBlock.copyProperties(getBlockState(), state));
	}

	@Override
	public void refresh() {
		super.refresh();
		if (hasWorld() && Objects.requireNonNull(world).isRemote) {
			BlockState state = getBlockState();
			world.markAndNotifyBlock(pos, world.getChunkAt(pos), state, state, 11, 512);
		}
	}

}
