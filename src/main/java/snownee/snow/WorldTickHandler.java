package snownee.snow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import snownee.snow.block.ModSnowBlock;

public class WorldTickHandler {
    private static Method METHOD;

    static {
        try {
            METHOD = ObfuscationReflectionHelper.findMethod(ChunkManager.class, "func_223491_f");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tick(TickEvent.WorldTickEvent event) {
        if (SnowCommonConfig.retainOriginalBlocks || METHOD == null) {
            return;
        }
        ServerWorld world = (ServerWorld) event.world;
        if (!world.isRaining()) {
            return;
        }
        if (world.getChunkProvider().getChunkGenerator() instanceof DebugChunkGenerator) {
            return;
        }
        Iterable<ChunkHolder> holders;
        try {
            holders = (Iterable<ChunkHolder>) METHOD.invoke(world.getChunkProvider().chunkManager);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return;
        }
        holders.forEach(holder -> {
            Chunk chunk = holder.getChunkIfComplete();
            if (chunk == null || !world.getChunkProvider().isChunkLoaded(chunk.getPos())) {
                return;
            }
            if (world.rand.nextInt(16) == 0) {
                int x = chunk.getPos().getXStart();
                int y = chunk.getPos().getZStart();
                BlockPos pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(x, 0, y, 15)).down();
                Biome biome = world.getBiome(pos);
                if (world.isAreaLoaded(pos, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
                {
                    if (biome.getTemperature(pos) >= 0.15f) {
                        return;
                    }
                    BlockState state = world.getBlockState(pos);
                    if (!ModSnowBlock.canContainState(state)) {
                        return;
                    }
                    if (world.getLightFor(LightType.BLOCK, pos.up()) > 11) {
                        return;
                    }
                    ModSnowBlock.convert(world, pos, state, 1, 3);
                }
            }
        });
    }
}
