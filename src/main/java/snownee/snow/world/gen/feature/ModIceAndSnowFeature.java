package snownee.snow.world.gen.feature;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.IceAndSnowFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.ModSnowBlock;

public class ModIceAndSnowFeature extends IceAndSnowFeature {

    public ModIceAndSnowFeature() {
        super(NoFeatureConfig::deserialize);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        BlockPos.Mutable blockpos = new BlockPos.Mutable();
        BlockPos.Mutable blockpos1 = new BlockPos.Mutable();

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int k = pos.getX() + i;
                int l = pos.getZ() + j;
                int i1 = worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING, k, l);
                blockpos.setPos(k, i1, l);
                blockpos1.setPos(blockpos).move(Direction.DOWN, 1);
                Biome biome = worldIn.getBiome(blockpos);
                if (biome.doesWaterFreeze(worldIn, blockpos1, false)) {
                    worldIn.setBlockState(blockpos1, Blocks.ICE.getDefaultState(), 2);
                }

                boolean flag = false;
                if (biome.doesSnowGenerate(worldIn, blockpos)) {
                    worldIn.setBlockState(blockpos, MainModule.BLOCK.getDefaultState(), 2);
                    flag = true;
                } else if (placeAdditional(biome, worldIn, blockpos)) {
                    flag = true;
                }
                if (flag) {
                    BlockState blockstate = worldIn.getBlockState(blockpos1);
                    if (blockstate.has(SnowyDirtBlock.SNOWY)) {
                        worldIn.setBlockState(blockpos1, blockstate.with(SnowyDirtBlock.SNOWY, true), 2);
                    }
                }
            }
        }

        return true;
    }

    public boolean placeAdditional(Biome biome, IWorld worldIn, BlockPos.Mutable pos) {
        if (SnowCommonConfig.retainOriginalBlocks || !SnowCommonConfig.replaceWorldFeature) {
            return false;
        }
        if (biome.getTemperature(pos) >= 0.15F) {
            return false;
        }
        if (pos.getY() >= 0 && pos.getY() < 256 && worldIn.getLightFor(LightType.BLOCK, pos) < 10 && MainModule.BLOCK.getDefaultState().isValidPosition(worldIn, pos)) {
            BlockState blockstate = worldIn.getBlockState(pos);
            return ModSnowBlock.convert(worldIn, pos, blockstate, 1, 2);
        }
        return false;
    }
}
