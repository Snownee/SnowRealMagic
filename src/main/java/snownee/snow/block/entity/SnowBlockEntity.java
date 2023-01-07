package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.block.entity.BaseBlockEntity;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;

public class SnowBlockEntity extends BaseBlockEntity {

	public static class Options {
		public boolean renderOverlay;
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
	public static final ModelProperty<BlockState> BLOCKSTATE = new ModelProperty<>();
	protected BlockState state = Blocks.AIR.defaultBlockState();
	protected ModelData modelData;

	public SnowBlockEntity(BlockPos pos, BlockState state) {
		this(CoreModule.TILE.get(), pos, state);
	}

	public SnowBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		options.renderBottom = !CoreModule.STAIRS.is(state);
	}

	public BlockState getState() {
		return state;
	}

	public void setState(BlockState state) {
		setState(state, true);
	}

	public boolean setState(BlockState state, boolean update) {
		if (state == null) {
			state = Blocks.AIR.defaultBlockState();
		}
		if (this.state == state || state.getBlock() instanceof SnowVariant) {
			return false;
		}
		this.state = state;
		if (hasLevel()) {
			if (level.isClientSide) {
				modelData = getModelData().derive().with(BLOCKSTATE, state).build();
				onStateChanged();
			}
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
			changed = options.update(data.getBoolean("RO"), true);
			if (changed && network && hasLevel() && level.isClientSide) {
				requestModelDataUpdate();
			}
		}
		if (data.contains("Block")) {
			ResourceLocation id = Util.RL(data.getString("Block"));
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block != null && block != Blocks.AIR) {
				changed |= setState(block.defaultBlockState(), network);
			}
		} else {
			changed |= setState(NbtUtils.readBlockState(data.getCompound("State")), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	public void saveState(CompoundTag data, boolean network) {
		if (getState() == getState().getBlock().defaultBlockState()) {
			data.putString("Block", ForgeRegistries.BLOCKS.getKey(getState().getBlock()).toString());
		} else {
			data.put("State", NbtUtils.writeBlockState(getState()));
		}
		if (options.renderOverlay)
			data.putBoolean("RO", options.renderOverlay);
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

	@Override
	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition);
	}

	@Override
	public ModelData getModelData() {
		if (modelData == null) {
			modelData = ModelData.builder().with(BLOCKSTATE, state).with(OPTIONS, options).build();
			//			if (hasLevel()) {
			//				Block block = getBlockState().getBlock();
			//				if (block instanceof WatcherSnowVariant) {
			//					((WatcherSnowVariant) block).onUpdateOptions(getBlockState(), level, worldPosition, options);
			//				}
			//			}
		}
		return modelData;
	}

	// only run in client!
	public void onStateChanged() {
		requestModelDataUpdate();
	}
}
