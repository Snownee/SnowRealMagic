package snownee.snow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper;
import snownee.snow.CoreModule;

public class SnowTile extends BaseTile {
	private BlockState state = Blocks.AIR.getDefaultState();
	private boolean isFullHeight = false;

	public SnowTile() {
		super(CoreModule.TILE);
	}

	public BlockState getState() {
		return state;
	}

	public boolean isFullHeight() {
		return isFullHeight;
	}

	public void setState(BlockState state) {
		setState(state, true);
	}

	public void setState(BlockState state, boolean update) {
		if (state == null) {
			state = Blocks.AIR.getDefaultState();
		}
		if (this.state == state) {
			return;
		}
		this.state = state;
		Block block = state.getBlock();
		this.isFullHeight = block instanceof WallBlock || block instanceof FenceBlock || block instanceof PaneBlock;
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
}
