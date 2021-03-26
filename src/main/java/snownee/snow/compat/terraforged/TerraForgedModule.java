package snownee.snow.compat.terraforged;

import com.terraforged.mod.api.event.SetupEvent;
import com.terraforged.mod.api.material.layer.LayerMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.snow.MainModule;
import snownee.snow.ModUtil;
import snownee.snow.world.gen.feature.ModIceAndSnowFeature;

@KiwiModule(value = "terraforged", dependencies = "terraforged")
@KiwiModule.Subscriber
public class TerraForgedModule extends AbstractModule {
    public static void freezeGround(IWorld world, IChunk chunk, Biome biome, BlockPos.Mutable snowPos, BlockPos.Mutable underPos) {
        if (!biome.doesSnowGenerate(world, snowPos) && ModIceAndSnowFeature.placeAdditional(biome, world, snowPos)) {
            if (chunk instanceof ChunkPrimer) {
                ((ChunkPrimer) chunk).deferredTileEntities.remove(snowPos);
            }
            BlockState blockstate = world.getBlockState(underPos);
            if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
                world.setBlockState(underPos, blockstate.with(SnowyDirtBlock.SNOWY, true), 2);
            }
        }
    }

    public static boolean isIn(BlockState state, Block block) {
        return state.getBlock() instanceof SnowBlock;
    }

    @Override
    protected void preInit() {
        ModUtil.terraforged = true;
    }

    @SubscribeEvent
    public void setupLayers(SetupEvent.Layers event) {
        event.getManager().register(LayerMaterial.of(Blocks.SNOW_BLOCK, MainModule.TILE_BLOCK));
    }
}
