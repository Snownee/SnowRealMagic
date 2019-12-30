package snownee.snow.grass;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;

public class ModGrassBlock extends GrassBlock {

    public ModGrassBlock(Properties properties) {
        super(properties);
    }

    public static boolean canSurvive(BlockState blockState, IWorldReader viewableWorld, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.up();
        BlockState blockState2 = viewableWorld.getBlockState(blockPos2);
        if (blockState2.getBlock().isIn(GrassModule.BOTTOM_SNOW)) {
            if (blockState2.getBlock() == Blocks.SNOW) {
                return blockState2.get(SnowBlock.LAYERS) == 1;
            }
            return true;
        } else {
            int i = LightEngine.func_215613_a(viewableWorld, blockState, blockPos, blockState2, blockPos2, Direction.UP, blockState2.getOpacity(viewableWorld, blockPos2));
            return i < viewableWorld.getMaxLightLevel();
        }
    }

    public static boolean canSpread(BlockState blockState, IWorldReader viewableWorld, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.up();
        return canSurvive(blockState, viewableWorld, blockPos) && !viewableWorld.getFluidState(blockPos2).isTagged(FluidTags.WATER);
    }

    @Override
    public void func_225534_a_/*onScheduledTick*/(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        if (!world.isRemote) {
            if (!canSurvive(blockState, world, blockPos)) {
                world.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
            } else {
                if (world.getLight(blockPos.up()) >= 9) {
                    BlockState blockState2 = this.getDefaultState();

                    for (int i = 0; i < 4; ++i) {
                        BlockPos blockPos2 = blockPos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                        if (world.getBlockState(blockPos2).getBlock() == Blocks.DIRT && canSpread(blockState2, world, blockPos2)) {
                            Block upBlock = world.getBlockState(blockPos2.up()).getBlock();
                            world.setBlockState(blockPos2, blockState2.with(SNOWY, upBlock.isIn(GrassModule.BOTTOM_SNOW)));
                        }
                    }
                }

            }
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing != Direction.UP) {
            return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        } else {
            Block block = facingState.getBlock();
            return stateIn.with(SNOWY, block == Blocks.SNOW_BLOCK || block.isIn(GrassModule.BOTTOM_SNOW));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Block block = context.getWorld().getBlockState(context.getPos().up()).getBlock();
        return this.getDefaultState().with(SNOWY, block == Blocks.SNOW_BLOCK || block.isIn(GrassModule.BOTTOM_SNOW));
    }
}
