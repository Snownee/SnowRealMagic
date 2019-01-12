package snownee.snow;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class Hook
{
    public static void trySnowAt(World world, BlockPos pos, boolean checkLight)
    {
        if (!ModConfig.blackMagic || !checkLight || world.isRemote)
        {
            return;
        }

        Biome biome = world.getBiome(pos);
        float f = biome.getTemperature(pos);

        if (f >= 0.15F)
        {
            return;
        }
        else
        {
            if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
            {
                IBlockState iblockstate1 = world.getBlockState(pos);

                if (iblockstate1.getBlock().isAir(iblockstate1, world, pos))
                {
                    pos = pos.down();
                    iblockstate1 = world.getBlockState(pos);
                    if (iblockstate1.getBlock().isAir(iblockstate1, world, pos))
                    {
                        return;
                    }
                }
                if (SnowRealMagic.BLOCK.canPlaceBlockAt(world, pos) && BlockSnowLayer.canContainState(iblockstate1))
                {
                    world.setBlockState(pos, SnowRealMagic.BLOCK.getDefaultState().withProperty(BlockSnowLayer.TILE, true));
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TileSnowLayer)
                    {
                        ((TileSnowLayer) tile).setState(iblockstate1);
                    }
                }
            }
        }
    }
}
