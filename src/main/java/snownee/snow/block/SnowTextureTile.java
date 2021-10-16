package snownee.snow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;

public class SnowTextureTile extends SnowTile {

	public static class Options {
		public boolean renderOverlay = true;
		public boolean renderBottom;

		public boolean update(boolean ro, boolean rb) {
			boolean changed = ro != renderOverlay || rb != renderBottom;
			renderOverlay = ro;
			renderBottom = rb;
			return changed;
		}
	}

	public Options options = new Options();
	public static final ModelProperty<Options> OPTIONS = new ModelProperty<>();

	public SnowTextureTile() {
		super(CoreModule.TEXTURE_TILE);
	}

	@Override
	public void loadState(BlockState state, CompoundNBT data, boolean network) {
		boolean changed = false;
		if (data.contains("RB")) {
			changed = options.update(data.getBoolean("RO"), data.getBoolean("RB"));
			if (changed && network && hasWorld() && world.isRemote) {
				requestModelDataUpdate();
			}
		}
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
		data.putString("Block", getState().getBlock().getRegistryName().toString());
		data.putBoolean("RB", options.renderBottom);
		data.putBoolean("RO", options.renderOverlay);
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		setState(ModSnowBlock.copyProperties(getBlockState(), state));
	}

	@Override
	public IModelData getModelData() {
		if (modelData == null) {
			modelData = new ModelDataMap.Builder().withInitial(BLOCKSTATE, state).withInitial(OPTIONS, options).build();
		}
		return modelData;
	}

	@Override
	public void onStateChanged() {
		updateOptions();
		super.onStateChanged();
	}

	public void updateOptions() {
		if (!hasWorld() || world.isRemote) {
			return;
		}
		Block block = getBlockState().getBlock();
		if (block instanceof WatcherSnowVariant) {
			if (((WatcherSnowVariant) block).onUpdateOptions(getBlockState(), world, pos, getModelData().getData(OPTIONS))) {
				refresh();
			}
		}
	}

	@Override
	protected void refresh() {
		super.refresh();
		if (hasWorld() && world.isRemote) {
			BlockState state = getBlockState();
			world.markAndNotifyBlock(pos, world.getChunkAt(pos), state, state, 11, 512);
		}
	}

}
