package snownee.snow.block.entity;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.ModBlockEntity;
import snownee.kiwi.util.KUtil;
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
			ResourceLocation id = KUtil.RL(data.getString("Block"));
			Block block = BuiltInRegistries.BLOCK.get(id);
			if (block != Blocks.AIR) {
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
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		loadState(compoundTag, false);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		saveState(compoundTag, false);
	}

	@Override
	protected @NotNull CompoundTag writePacketData(CompoundTag compoundTag, HolderLookup.Provider provider) {
		saveState(compoundTag, true);
		return compoundTag;
	}
}
