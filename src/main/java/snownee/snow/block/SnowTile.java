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
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;

public class SnowTile extends BaseTile {

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
		if (FMLEnvironment.dist.isClient()) {
			getModelData().setData(BLOCKSTATE, state);
			onStateChanged();
		}
		if (update && world != null) {
			if (world.isRemote) {
				world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 11);
			} else {
				refresh();
			}
		}
		return true;
	}

	@Override
	protected void refresh() {
		super.refresh();
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		loadState(state, compound, false);
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		loadState(getBlockState(), data, true);
	}

	public void loadState(BlockState state, CompoundNBT data, boolean network) {
		if (data.contains("Block")) {
			ResourceLocation id = Util.RL(data.getString("Block"));
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block != null && block != Blocks.AIR) {
				setState(block.getDefaultState(), network);
			}
		} else {
			setState(NBTUtil.readBlockState(data.getCompound("State")), network);
		}
	}

	public void saveState(CompoundNBT data, boolean network) {
		if (getState() == getState().getBlock().getDefaultState()) {
			data.putString("Block", getState().getBlock().getRegistryName().toString());
		} else {
			data.put("State", NBTUtil.writeBlockState(getState()));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		saveState(compound, false);
		return compound;
	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		saveState(data, true);
		return data;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos);
	}

	@Override
	public IModelData getModelData() {
		if (modelData == null) {
			modelData = new ModelDataMap.Builder().withInitial(BLOCKSTATE, state).build();
		}
		return modelData;
	}

	// only run in client!
	public void onStateChanged() {
		requestModelDataUpdate();
	}
}
