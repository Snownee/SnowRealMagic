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
import snownee.snow.MainModule;

public class SnowTile extends BaseTile {
    private BlockState state = Blocks.AIR.getDefaultState();
    private boolean isFullHeight = false;

    public SnowTile() {
        super(MainModule.TILE);
    }

    public BlockState getState() {
        return state;
    }

    public boolean isFullHeight() {
        return isFullHeight;
    }

    public void setState(BlockState state) {
        if (this.state.equals(state)) {
            return;
        }
        this.state = state;
        Block block = state.getBlock();
        this.isFullHeight = block instanceof WallBlock || block instanceof FenceBlock || block instanceof PaneBlock;
        if (world != null && !world.isRemote) {
            refresh();
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        readPacketData(compound);
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
        state = NBTHelper.of(data).getBlockState("State");
        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }
        Block block = state.getBlock();
        isFullHeight = block instanceof WallBlock || block instanceof FenceBlock || block instanceof PaneBlock;
    }

    @Override
    protected CompoundNBT writePacketData(CompoundNBT data) {
        NBTHelper.of(data).setBlockState("State", state);
        return data;
    }
}
