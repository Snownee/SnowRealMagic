package snownee.snow.grass;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MyceliumBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ModMyceliumBlock extends MyceliumBlock {

    public ModMyceliumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(BlockState blockState, World world, BlockPos blockPos, Random random) {
        if (!world.isRemote) {
            if (!ModGrassBlock.canSurvive(blockState, world, blockPos)) {
                world.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
            } else {
                if (world.getLight(blockPos.up()) >= 9) {
                    BlockState blockState2 = this.getDefaultState();

                    for (int i = 0; i < 4; ++i) {
                        BlockPos blockPos2 = blockPos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                        if (world.getBlockState(blockPos2).getBlock() == Blocks.DIRT && ModGrassBlock.canSpread(blockState2, world, blockPos2)) {
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
