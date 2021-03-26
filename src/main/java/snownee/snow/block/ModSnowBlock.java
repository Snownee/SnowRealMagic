package snownee.snow.block;

import java.util.Random;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.MushroomBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.tile.TextureTile;
import snownee.snow.MainModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowClientConfig;
import snownee.snow.SnowCommonConfig;
import snownee.snow.entity.FallingSnowEntity;

public class ModSnowBlock extends SnowBlock implements ISnowVariant {
    public static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[] { VoxelShapes.empty(), Block.makeCuboidShape(0, 0, 0, 16, 1, 16), Block.makeCuboidShape(0, 0, 0, 16, 2, 16), Block.makeCuboidShape(0, 0, 0, 16, 3, 16), Block.makeCuboidShape(0, 0, 0, 16, 4, 16), Block.makeCuboidShape(0, 0, 0, 16, 5, 16), Block.makeCuboidShape(0, 0, 0, 16, 6, 16), Block.makeCuboidShape(0, 0, 0, 16, 7, 16), Block.makeCuboidShape(0, 0, 0, 16, 8, 16) };

    public ModSnowBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (ModUtil.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
            return super.getCollisionShape(state, worldIn, pos, context);
        }
        int layers = state.get(LAYERS);
        if (layers == 8) {
            return VoxelShapes.fullCube();
        }
        BlockState upState = worldIn.getBlockState(pos.up());
        if (!upState.getBlock().isAir(upState, worldIn, pos)) {
            return VoxelShapes.fullCube();
        }
        return SNOW_SHAPES_MAGIC[layers - 1];
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (SnowCommonConfig.snowGravity) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, tickRate());
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (SnowCommonConfig.snowGravity) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, tickRate());
            return stateIn;
        } else {
            return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return isValidPosition(state, worldIn, pos, false);
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos, boolean ignoreSelf) {
        BlockState blockstate = worldIn.getBlockState(pos.down());
        Block block = blockstate.getBlock();
        if (block instanceof ModSnowBlock && blockstate.get(LAYERS) == 8) {
            return true;
        } else if ((SnowCommonConfig.snowOnIce && (block == Blocks.ICE || block == Blocks.PACKED_ICE)) || !block.isIn(MainModule.INVALID_SUPPORTERS)) {
            if (ignoreSelf || state.getMaterial().isReplaceable() || canContainState(state)) {
                if (block.isIn(BlockTags.LEAVES) || Block.doesSideFillSquare(blockstate.getCollisionShape(worldIn, pos.down()), Direction.UP)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected int tickRate() {
        return 2;
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        this.checkFallable(worldIn, pos, state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (ModUtil.shouldMelt(worldIn, pos)) {
            if (state.getBlock() == MainModule.TILE_BLOCK) {
                state.removedByPlayer(worldIn, pos, null, false, null);
            } else {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
            return;
        }
        if (ModUtil.terraforged) {
            return;
        }
        if (!SnowCommonConfig.snowAccumulationDuringSnowfall && !SnowCommonConfig.snowAccumulationDuringSnowstorm) {
            return;
        }
        if (random.nextInt(8) > 0) {
            return;
        }
        BlockPos height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE, pos);
        if (height.getY() != pos.getY() + 1) {
            return;
        }

        Biome biome = worldIn.getBiome(pos);
        boolean flag = false;
        if (worldIn.isRaining() && ModUtil.isColdAt(worldIn, biome, pos)) {
            if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
                flag = true;
            } else if (SnowCommonConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering()) {
                flag = true;
            }
        }

        int layers = state.get(LAYERS);

        if (flag && layers < SnowCommonConfig.snowAccumulationMaxLayers) {
            accumulate(worldIn, pos, state, (w, p) -> (SnowCommonConfig.snowAccumulationMaxLayers > 8 || !(w.getBlockState(p.down()).getBlock() instanceof ModSnowBlock)) && w.getLightFor(LightType.BLOCK, p) < 10, true);
        } else if (!SnowCommonConfig.snowNeverMelt && SnowCommonConfig.snowNaturalMelt && layers > 1 && !worldIn.isRaining()) {
            accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.up()).getBlock() instanceof ModSnowBlock), false);
        }
    }

    private static void accumulate(World world, BlockPos pos, BlockState centerState, BiPredicate<IWorld, BlockPos> filter, boolean accumulate) {
        int i = centerState.get(LAYERS);
        for (int j = 0; j < 4; j++) {
            Direction direction = Direction.byHorizontalIndex(j);
            BlockPos pos2 = pos.offset(direction);
            if (!filter.test(world, pos2)) {
                continue;
            }
            BlockState state = world.getBlockState(pos2);
            if (!MainModule.BLOCK.isValidPosition(state, world, pos2)) {
                continue;
            }
            int l;
            if (state.getBlock() instanceof ModSnowBlock) {
                l = state.get(LAYERS);
            } else {
                l = 0;
            }
            if (accumulate ? i > l : i < l) {
                if (accumulate) {
                    placeLayersOn(world, pos2, 1, false, new DirectionalPlaceContext(world, pos2, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
                } else {
                    world.setBlockState(pos2, state.with(LAYERS, l - 1));
                }
                return;
            }
        }
        if (accumulate) {
            placeLayersOn(world, pos, 1, false, new DirectionalPlaceContext(world, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
        } else {
            world.setBlockState(pos, centerState.with(LAYERS, i - 1));
        }
    }

    protected boolean checkFallable(World worldIn, BlockPos pos, BlockState state) {
        BlockPos posDown = pos.down();
        if ((worldIn.isAirBlock(posDown) || canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) && pos.getY() >= 0) {
            if (!worldIn.isRemote) {
                worldIn.setBlockState(pos, getContainedState(worldIn, pos));
                FallingSnowEntity entity = new FallingSnowEntity(worldIn, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, state.get(LAYERS));
                worldIn.addEntity(entity);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean placeLayersOn(World world, BlockPos pos, int layers, boolean falling, BlockItemUseContext useContext, boolean playSound) {
        layers = MathHelper.clamp(layers, 1, 8);
        BlockState state = world.getBlockState(pos);
        int originLayers = 0;
        if (state.getBlock() instanceof ModSnowBlock) {
            originLayers = state.get(LAYERS);
            world.setBlockState(pos, state.with(LAYERS, MathHelper.clamp(originLayers + layers, 1, 8)));
        } else if (canContainState(state) && state.isValidPosition(world, pos)) {
            convert(world, pos, state, MathHelper.clamp(layers, 1, 8), 3);
        } else if (MainModule.BLOCK.isValidPosition(state, world, pos)) {
            world.setBlockState(pos, MainModule.BLOCK.getDefaultState().with(LAYERS, MathHelper.clamp(layers, 1, 8)));
        } else {
            return false;
        }
        if (falling) {
            world.addBlockEvent(pos, MainModule.BLOCK, originLayers, layers);
        } else if (playSound) {
            SoundType soundtype = MainModule.BLOCK.getSoundType(MainModule.BLOCK.getDefaultState());
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
        }
        if (originLayers + layers > 8) {
            pos = pos.up();
            if (MainModule.BLOCK.isValidPosition(MainModule.BLOCK.getDefaultState(), world, pos) && world.getBlockState(pos).isReplaceable(useContext)) {
                world.setBlockState(pos, MainModule.BLOCK.getDefaultState().with(LAYERS, MathHelper.clamp(originLayers + layers - 8, 1, 8)));
            }
        }
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        int i = state.get(LAYERS);
        if (useContext.getItem().getItem() == MainModule.BLOCK.asItem() && i < 8) {
            if (useContext.replacingClickedOnBlock() && state.getBlock() == MainModule.BLOCK) {
                return useContext.getFace() == Direction.UP;
            } else {
                return true;
            }
        }
        return (SnowCommonConfig.snowAlwaysReplaceable && state.get(LAYERS) < 8) || i == 1;
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int originLayers, int layers) {
        double offsetY = originLayers / 8D;
        layers *= 10;
        for (int i = 0; i < layers; ++i) {
            // TODO better particle
            double d0 = RANDOM.nextGaussian() * 0.1D;
            double d1 = RANDOM.nextGaussian() * 0.02D;
            double d2 = RANDOM.nextGaussian() * 0.1D;
            worldIn.addParticle(ParticleTypes.SPIT, pos.getX() + RANDOM.nextFloat(), pos.getY() + offsetY, pos.getZ() + RANDOM.nextFloat(), d0, d1, d2);
        }
        SoundType soundtype = getSoundType(state, worldIn, pos, null);
        worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!SnowClientConfig.particleThroughLeaves || rand.nextInt(32) > 0) {
            return;
        }
        Entity entity = Minecraft.getInstance().getRenderViewEntity();
        if (entity != null && entity.getPosition().distanceSq(pos) > 256) {
            return;
        }
        BlockState stateDown = worldIn.getBlockState(pos.down());
        if (stateDown.getBlock().isIn(BlockTags.LEAVES)) {
            double d0 = pos.getX() + rand.nextDouble();
            double d1 = pos.getY() - 0.05D;
            double d2 = pos.getZ() + rand.nextDouble();
            worldIn.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, stateIn), d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getDustColor(BlockState state) {
        return 0xffffffff;
    }

    public static boolean canFallThrough(BlockState state, World worldIn, BlockPos pos) {
        return FallingBlock.canFallThrough(state) && state.getCollisionShape(worldIn, pos).isEmpty();
    }

    @Override
    public BlockState onShovel(BlockState state, World world, BlockPos pos) {
        int layers = state.get(LAYERS) - 1;
        if (layers > 0) {
            return state.with(LAYERS, layers);
        } else {
            return getRaw(state, world, pos);
        }
    }

    @Override
    public BlockState getRaw(BlockState state, IBlockReader world, BlockPos pos) {
        return getContainedState(world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (state.getBlock() == MainModule.BLOCK) {
            BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, handIn, hit));
            Block block = Block.getBlockFromItem(context.getItem().getItem());
            if (block != null && context.replacingClickedOnBlock()) {
                BlockState state2 = block.getStateForPlacement(context);
                if (state2 != null && canContainState(state2) && state2.isValidPosition(worldIn, pos)) {
                    if (!worldIn.isRemote) {
                        worldIn.setBlockState(pos, state2, 16 | 32);
                        int i = state.get(LAYERS);
                        if (placeLayersOn(worldIn, pos, i, false, context, true) && !player.isCreative()) {
                            context.getItem().shrink(1);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getPos());
        if (blockstate.getBlock() instanceof ModSnowBlock) {
            int i = blockstate.get(LAYERS);
            return blockstate.with(LAYERS, Math.min(8, i + 1));
        } else {
            return super.getStateForPlacement(context);
        }
    }

    public BlockState getContainedState(IBlockReader world, BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    public static boolean canContainState(BlockState state) {
        if (!SnowCommonConfig.placeSnowInBlock || state.getBlock().hasTileEntity(state) || !state.getFluidState().isEmpty()) {
            return false;
        }
        Block block = state.getBlock();
        if (block.isIn(MainModule.NOT_CONTAINABLES)) {
            return false;
        }
        if (block.isIn(MainModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
            return true;
        }
        if (block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof WallBlock || (block instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.BOTTOM) || (block instanceof StairsBlock && state.get(StairsBlock.HALF) == Half.BOTTOM)) {
            return true;
            // return block.getRenderLayer() == BlockRenderLayer.SOLID;
        }
        return false;
    }

    public static boolean convert(IWorld world, BlockPos pos, BlockState state, int layers, int flags) {
        if (!SnowCommonConfig.placeSnowInBlock || state.hasTileEntity()) {
            return false;
        }
        if (state.getBlock().isAir(state, world, pos)) {
            if (state.getBlock() != MainModule.BLOCK) {
                world.setBlockState(pos, MainModule.BLOCK.getDefaultState().with(LAYERS, layers), flags);
            }
            return true;
        }
        Block block = state.getBlock();
        if (block.isIn(MainModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
            if (block != MainModule.TILE_BLOCK) {
                world.setBlockState(pos, MainModule.TILE_BLOCK.getDefaultState().with(LAYERS, layers), flags);
            }
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof SnowTile) {
                ((SnowTile) tile).setState(state);
            }
            return true;
        }

        BlockPos posDown = pos.down();
        BlockState stateDown = world.getBlockState(posDown);
        if (block instanceof StairsBlock && state.getBlock() != MainModule.STAIRS) {
            world.setBlockState(pos, MainModule.STAIRS.getDefaultState().with(StairsBlock.FACING, state.get(StairsBlock.FACING)).with(StairsBlock.HALF, state.get(StairsBlock.HALF)).with(StairsBlock.SHAPE, state.get(StairsBlock.SHAPE)), flags);
        } else if (block instanceof SlabBlock && state.getBlock() != MainModule.SLAB && state.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
            world.setBlockState(pos, MainModule.SLAB.getDefaultState(), flags);
        } else if (block instanceof FenceBlock && state.getBlock().getClass() != SnowFenceBlock.class) {
            Block newBlock = block.isIn(BlockTags.WOODEN_FENCES) ? MainModule.FENCE : MainModule.FENCE2;
            BlockState newState = newBlock.getDefaultState().with(FourWayBlock.NORTH, state.get(FourWayBlock.NORTH)).with(FourWayBlock.SOUTH, state.get(FourWayBlock.SOUTH)).with(FourWayBlock.WEST, state.get(FourWayBlock.WEST)).with(FourWayBlock.EAST, state.get(FourWayBlock.EAST));
            newState = newState.updatePostPlacement(Direction.DOWN, stateDown, world, pos, posDown);
            world.setBlockState(pos, newState, flags);
        } else if (block instanceof FenceGateBlock && state.getBlock() != MainModule.FENCE_GATE) {
            BlockState newState = MainModule.FENCE_GATE.getDefaultState().with(FenceGateBlock.OPEN, state.get(FenceGateBlock.OPEN)).with(FenceGateBlock.IN_WALL, state.get(FenceGateBlock.IN_WALL)).with(HorizontalBlock.HORIZONTAL_FACING, state.get(HorizontalBlock.HORIZONTAL_FACING));
            newState = newState.updatePostPlacement(Direction.DOWN, stateDown, world, pos, posDown);
            world.setBlockState(pos, newState, flags);
        } else if (block instanceof WallBlock && state.getBlock() != MainModule.WALL) {
            BlockState newState = MainModule.WALL.getDefaultState().with(BlockStateProperties.WALL_HEIGHT_EAST, state.get(BlockStateProperties.WALL_HEIGHT_EAST)).with(BlockStateProperties.WALL_HEIGHT_NORTH, state.get(BlockStateProperties.WALL_HEIGHT_NORTH)).with(BlockStateProperties.WALL_HEIGHT_SOUTH, state.get(BlockStateProperties.WALL_HEIGHT_SOUTH)).with(BlockStateProperties.WALL_HEIGHT_WEST, state.get(BlockStateProperties.WALL_HEIGHT_WEST)).with(WallBlock.UP, state.get(WallBlock.UP));
            newState = newState.updatePostPlacement(Direction.DOWN, stateDown, world, pos, posDown);
            world.setBlockState(pos, newState, flags);
        } else {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TextureTile) {
            ((TextureTile) tile).setTexture("0", state);
        }
        return true;
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (SnowCommonConfig.snowReduceFallDamage) {
            BlockState state = worldIn.getBlockState(pos.down());
            if (state.getBlock() == MainModule.BLOCK || state.getBlock() == MainModule.TILE_BLOCK) {
                entityIn.onLivingFall(fallDistance, 0.2F);
                return;
            }
            state = worldIn.getBlockState(pos);
            entityIn.onLivingFall(fallDistance, 1 - state.get(LAYERS) * 0.1F);
        }
        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (SnowCommonConfig.thinnerBoundingBox) {
            double d0 = Math.abs(entityIn.getMotion().y);
            if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
                int layers = worldIn.getBlockState(pos).get(LAYERS) - 1;
                double d1 = 1 - layers * 0.05f;
                entityIn.setMotion(entityIn.getMotion().mul(d1, 1.0D, d1));
            }
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }
}
