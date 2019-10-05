package snownee.snow.block.state;

import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.IProperty;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.snow.block.SnowFenceBlock;

public class SnowFenceBlockState extends BlockState {

    public SnowFenceBlockState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties) {
        super(block, properties);
    }

    @Override
    public boolean func_224755_d(IBlockReader world, BlockPos pos, @Nonnull Direction side) {
        BlockPos neighborPos = pos.offset(side);
        BlockState neighborState = world.getBlockState(neighborPos);
        Block neighborBlock = neighborState.getBlock();
        //If the block requesting our "solid" status is a fence we want to fake it if our source material is the same as theirs
        if (neighborBlock.isIn(BlockTags.FENCES)) {
            Material ourMaterial = getMaterial(world.getBlockState(pos), world, pos);
            Material neighborMaterial = getMaterial(neighborState, world, neighborPos);
            return ourMaterial == neighborMaterial;
        }
        return super.func_224755_d(world, pos, side);
    }

    private Material getMaterial(BlockState blockState, IBlockReader world, BlockPos pos) {
        Block block = blockState.getBlock();
        if (block instanceof SnowFenceBlock) {
            return ((SnowFenceBlock) block).getMaterial(blockState, world, pos);
        }
        return blockState.getMaterial();
    }
}