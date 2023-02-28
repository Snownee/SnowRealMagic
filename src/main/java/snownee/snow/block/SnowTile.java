package snownee.snow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SnowTile extends BaseTile {

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
	protected BlockState state = Blocks.AIR.getDefaultState();
	protected IModelData modelData;

	public SnowTile() {
		this(CoreModule.TILE);
	}

	public SnowTile(TileEntityType<?> type) {
		super(type);
	}

	public BlockState getState() {
		return state;
	}

	public void setState(BlockState state) {
		setState(state, true);
	}

	public boolean setState(BlockState state, boolean update) {
		if (state == null) {
			state = Blocks.AIR.getDefaultState();
		}
		if (this.state == state || state.getBlock() instanceof SnowVariant) {
			return false;
		}
		this.state = state;
		if (hasWorld()) {
			if (Objects.requireNonNull(world).isRemote) {
				getModelData().setData(BLOCKSTATE, state);
				onStateChanged();
			}
			if (update) {
				if (world.isRemote) {
					world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 11);
				} else {
					refresh();
				}
			}
		}
		return true;
	}

	@Override
	public void refresh() {
		super.refresh();
	}

	@Override
	public void read(@Nonnull BlockState state, @Nonnull CompoundNBT compound) {
		super.read(state, compound);
		loadState(state, compound, false);
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		loadState(getBlockState(), data, true);
	}

	public void loadState(BlockState state, CompoundNBT data, boolean network) {
		boolean changed = false;
		if (data.contains("RO")) {
			changed = options.update(data.getBoolean("RO"), false);
			if (changed && network && hasWorld() && Objects.requireNonNull(world).isRemote) {
				requestModelDataUpdate();
			}
		}
		if (data.contains("Block")) {
			ResourceLocation id = Util.RL(data.getString("Block"));
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block != null && block != Blocks.AIR) {
				changed |= setState(block.getDefaultState(), network);
			}
		} else {
			changed |= setState(NBTUtil.readBlockState(data.getCompound("State")), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	public void saveState(CompoundNBT data, boolean network) {
		if (getState() == getState().getBlock().getDefaultState()) {
			data.putString("Block", Objects.requireNonNull(getState().getBlock().getRegistryName()).toString());
		} else {
			data.put("State", NBTUtil.writeBlockState(getState()));
		}
		if (options.renderOverlay) {
			data.putBoolean("RO", options.renderOverlay);
		}
	}

	@Nonnull
    @Override
	public CompoundNBT write(@Nonnull CompoundNBT compound) {
		super.write(compound);
		saveState(compound, false);
		return compound;
	}

	@Nonnull
    @Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		saveState(data, true);
		return data;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos);
	}

	@Nonnull
    @Override
	public IModelData getModelData() {
		if (modelData == null) {
			modelData = new ModelDataMap.Builder().withInitial(BLOCKSTATE, state).withInitial(OPTIONS, options).build();
		}
		return modelData;
	}

	// only run in client!
	public void onStateChanged() {
		requestModelDataUpdate();
	}
}
