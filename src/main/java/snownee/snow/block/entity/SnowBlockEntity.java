package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.ModBlockEntity;
import snownee.kiwi.util.KUtil;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;

@NotNullByDefault
public class SnowBlockEntity extends ModBlockEntity {

	public static class Options {
		public boolean renderOverlay;

		public boolean update(boolean renderOverlay) {
			boolean changed = renderOverlay != this.renderOverlay;
			this.renderOverlay = renderOverlay;
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
		if (this.state == state || state.getBlock() instanceof SnowVariant) {
			return false;
		}
		this.state = state;
		if (level != null) {
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
		loadContainedState(data, true);
	}

	public void loadContainedState(CompoundTag data, boolean network) {
		boolean changed = false;
		if (data.contains("RO")) {
			changed = options.update(data.getBoolean("RO"));
			if (changed && network && level != null && level.isClientSide) {
				//				requestModelDataUpdate();
			}
		}
		changed |= setContainedState(parseContainedState(data), network);
		if (changed && network) {
			refresh();
		}
	}

	public static BlockState parseContainedState(CompoundTag data) {
		if (data.contains("Block")) {
			return BuiltInRegistries.BLOCK.get(KUtil.RL(data.getString("Block"))).defaultBlockState();
		} else {
			return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound("State"));
		}
	}

	public void saveContainedState(CompoundTag data, boolean network) {
		if (getContainedState() == getContainedState().getBlock().defaultBlockState()) {
			data.putString("Block", BuiltInRegistries.BLOCK.getKey(getContainedState().getBlock()).toString());
		} else {
			data.put("State", NbtUtils.writeBlockState(getContainedState()));
		}
		if (options.renderOverlay) {
			data.putBoolean("RO", true);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		loadContainedState(compoundTag, false);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		saveContainedState(compoundTag, false);
	}

	@Override
	protected CompoundTag writePacketData(CompoundTag compoundTag, HolderLookup.Provider provider) {
		saveContainedState(compoundTag, true);
		return compoundTag;
	}
}
