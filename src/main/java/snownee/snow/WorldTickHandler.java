package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import snownee.snow.block.ModSnowBlock;

public class WorldTickHandler
{
    private static Method METHOD;

    static
    {
        try
        {
            METHOD = ChunkManager.class.getMethod("func_223491_f");
            METHOD.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            e.printStackTrace();
        }
    }

    public static void tick(TickEvent.WorldTickEvent event)
    {
        if (METHOD == null)
        {
            return;
        }
        ServerWorld world = (ServerWorld) event.world;
        if (!world.isRaining())
        {
            return;
        }
        if (world.getWorldInfo().getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES)
        {
            return;
        }
        Iterable<ChunkHolder> holders;
        try
        {
            holders = (Iterable<ChunkHolder>) METHOD.invoke(world.getChunkProvider().chunkManager);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            return;
        }
        holders.forEach(holder -> {
            Chunk chunk = holder.func_219298_c();
            if (chunk == null || !world.getChunkProvider().isChunkLoaded(chunk.getPos()))
            {
                return;
            }
            if (world.dimension.canDoRainSnowIce(chunk) && world.rand.nextInt(16) == 0)
            {
                int x = chunk.getPos().getXStart();
                int y = chunk.getPos().getZStart();
                BlockPos pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.func_217383_a(x, 0, y, 15)).down();
                Biome biome = world.getBiome(pos);
                if (world.isAreaLoaded(pos, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
                {
                    if (biome.func_225486_c(pos) < 0.15f)
                    {
                        return;
                    }
                    BlockState state = world.getBlockState(pos);
                    if (!ModSnowBlock.canContainState(state))
                    {
                        return;
                    }
                    ModSnowBlock.convert(world, pos, Blocks.SNOW.getDefaultState(), state, 1, 3);
                }
            }
        });
    }
}
