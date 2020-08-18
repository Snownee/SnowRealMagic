package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kiwi.tile.TextureTile;

public interface ISnowVariant {
    default BlockState getRaw(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TextureTile) {
            Item item = ((TextureTile) tile).getMark("0");
            if (item instanceof BlockItem) {
                BlockState newState = ((BlockItem) item).getBlock().getDefaultState();
                for (Property property : state.getProperties()) {
                    if (newState.hasProperty(property)) {
                        newState = newState.with(property, state.get(property));
                    }
                }
                return newState;
            }
        }
        return Blocks.AIR.getDefaultState();
    }

    default BlockState onShovel(BlockState state, World world, BlockPos pos) {
        return getRaw(state, world, pos);
    }
}
