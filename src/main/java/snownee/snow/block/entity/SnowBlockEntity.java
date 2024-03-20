package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.ModBlockEntity;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;

public class SnowBlockEntity extends ModBlockEntity {

	public static class Options {
		public boolean renderOverlay;

		public boolean update(boolean ro) {
			boolean changed = ro != renderOverlay;
			renderOverlay = ro;
			return changed;
		}
	}

	public Options options = new Options();
	protected BlockState state = Blocks.AIR.defaultBlockState();

	public SnowBlockEntity(BlockPos pos, BlockState state) {
		this(CoreModule.TILE.get(), pos, state);
	}

	public SnowBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public BlockState getContainedState() {
		return state;
	}

	public void setContainedState(BlockState state) {
		setContainedState(state, true);
	}

	public boolean setContainedState(BlockState state, boolean update) {
		if (state == null) {
			state = Blocks.AIR.defaultBlockState();
		}
		if (this.state == state || state.getBlock() instanceof SnowVariant) {
			return false;
		}
		this.state = state;
		if (hasLevel()) {
			//			if (level.isClientSide) {
			//				getModelData().setData(BLOCKSTATE, state);
			//				onStateChanged();
			//			}
			if (update) {
				if (level.isClientSide) {
					level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 11);
				} else {
					refresh();
				}
			}
		}
		return true;
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		loadState(compound, false);
	}

	@Override
	protected void readPacketData(CompoundTag data) {
		loadState(data, true);
	}

	public void loadState(CompoundTag data, boolean network) {
		boolean changed = false;
		if (data.contains("RO")) {
			changed = options.update(data.getBoolean("RO"));
			if (changed && network && hasLevel() && level.isClientSide) {
				//				requestModelDataUpdate();
			}
		}
		if (data.contains("Block")) {
			ResourceLocation id = Util.RL(data.getString("Block"));
			Block block = BuiltInRegistries.BLOCK.get(id);
			if (block != null && block != Blocks.AIR) {
				changed |= setContainedState(block.defaultBlockState(), network);
			}
		} else {
			changed |= setContainedState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound("State")), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	public void saveState(CompoundTag data, boolean network) {
		if (getContainedState() == getContainedState().getBlock().defaultBlockState()) {
			data.putString("Block", BuiltInRegistries.BLOCK.getKey(getContainedState().getBlock()).toString());
		} else {
			data.put("State", NbtUtils.writeBlockState(getContainedState()));
		}
		if (options.renderOverlay) {
			data.putBoolean("RO", options.renderOverlay);
		}
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		saveState(compound, false);
	}

	@Override
	protected CompoundTag writePacketData(CompoundTag data) {
		saveState(data, true);
		return data;
	}

}
