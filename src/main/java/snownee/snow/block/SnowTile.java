package snownee.snow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;

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
    public void read(CompoundNBT compound) {
        super.read(compound);
        state = NBTHelper.of(compound).getBlockState("State");
        Block block = state.getBlock();
        isFullHeight = block instanceof WallBlock || block instanceof FenceBlock || block instanceof PaneBlock;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        NBTHelper.of(compound).setBlockState("State", state);
        return compound;
    }

    @Override
    public boolean hasFastRenderer() {
        return !SnowCommonConfig.forceNormalTESR && state.getBlock().getRenderLayer() != BlockRenderLayer.CUTOUT;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos);
    }

    @Override
    protected void readPacketData(CompoundNBT data) {
        read(data);
    }

    @Override
    protected CompoundNBT writePacketData(CompoundNBT data) {
        return write(data);
    }
}
