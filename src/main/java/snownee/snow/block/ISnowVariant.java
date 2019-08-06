package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.kiwi.tile.TextureTile;

public interface ISnowVariant
{
    default BlockState getRaw(BlockState state, World world, BlockPos pos)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TextureTile)
        {
            Item item = ((TextureTile) tile).getMark("0");
            if (item instanceof BlockItem)
            {
                return ((BlockItem) item).getBlock().getDefaultState();
            }
        }
        return Blocks.AIR.getDefaultState();
    }

    default BlockState onShovel(BlockState state, World world, BlockPos pos)
    {
        return getRaw(state, world, pos);
    }
}
