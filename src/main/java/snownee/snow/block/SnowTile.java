package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper;
import snownee.snow.CoreModule;

public class SnowTile extends BaseTile {

	public static final ModelProperty<BlockState> BLOCKSTATE = new ModelProperty<>();
	protected BlockState state = Blocks.AIR.getDefaultState();
	private IModelData modelData;

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

	public void setState(BlockState state, boolean update) {
		if (state == null) {
			state = Blocks.AIR.getDefaultState();
		}
		if (this.state == state || state.getBlock() instanceof ISnowVariant) {
			return;
		}
		this.state = state;
		if (FMLEnvironment.dist.isClient()) {
			getModelData().setData(BLOCKSTATE, state);
			requestModelDataUpdate();
		}
		if (update && world != null) {
			if (world.isRemote) {
				world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 11);
			} else {
				refresh();
			}
		}
	}

	@Override
	protected void refresh() {
		super.refresh();
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		setState(NBTHelper.of(compound).getBlockState("State"), false);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		writePacketData(compound);
		return compound;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos);
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		setState(NBTHelper.of(data).getBlockState("State"));
	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		NBTHelper.of(data).setBlockState("State", state);
		return data;
	}

	@Override
	public IModelData getModelData() {
		if (modelData == null) {
			modelData = new ModelDataMap.Builder().withInitial(BLOCKSTATE, state).build();
		}
		return modelData;
	}
}
