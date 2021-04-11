package snownee.snow.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.WitherRoseBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;

public class ModSnowTileBlock extends ModSnowBlock {
    public ModSnowTileBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SnowTile();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = super.getCollisionShape(state, worldIn, pos, context);
        shape = VoxelShapes.combine(shape, getContainedState(worldIn, pos).getCollisionShape(worldIn, pos), IBooleanFunction.OR);
        return VoxelShapes.combineAndSimplify(shape, VoxelShapes.fullCube(), IBooleanFunction.AND);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = super.getShape(state, worldIn, pos, context);
        return VoxelShapes.combineAndSimplify(shape, getContainedState(worldIn, pos).getShape(worldIn, pos, context), IBooleanFunction.OR);
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        if (useContext.getItem().getItem() == MainModule.BLOCK.asItem()) {
            return super.isReplaceable(state, useContext);
        }
        if (!super.isReplaceable(state, useContext)) {
            return false;
        }
        return getContainedState(useContext.getWorld(), useContext.getPos()).isReplaceable(useContext);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return getContainedState(worldIn, pos).allowsMovement(worldIn, pos, type);
    }

    @Override
    public BlockState getContainedState(IBlockReader world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof SnowTile) {
            return ((SnowTile) tile).getState();
        }
        return super.getContainedState(world, pos);
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (willHarvest) {
            getBlock().onBlockHarvested(world, pos, state, player);
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof SnowTile) {
            BlockState newState = ((SnowTile) tile).getState();
            world.setBlockState(pos, newState);
        }
        return true;
    }

    @Override
    public String getTranslationKey() {
        return MainModule.BLOCK.getTranslationKey();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getContainedState(world, pos).getPickBlock(target, world, pos, player);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity) {
            if (!worldIn.isRemote && worldIn.getDifficulty() != Difficulty.PEACEFUL) {
                if (getContainedState(worldIn, pos).getBlock() instanceof WitherRoseBlock) {
                    LivingEntity livingentity = (LivingEntity) entityIn;
                    if (!livingentity.isInvulnerableTo(DamageSource.WITHER)) {
                        livingentity.addPotionEffect(new EffectInstance(Effects.WITHER, 40));
                    }
                }
            } else if (entityIn.getType() != EntityType.FOX && entityIn.getType() != EntityType.BEE) {
                BlockState stateIn = getContainedState(worldIn, pos);
                if (stateIn.getBlock() instanceof SweetBerryBushBlock) {
                    entityIn.setMotionMultiplier(state, new Vector3d(0.8F, 0.75D, 0.8F));
                    if (!worldIn.isRemote && stateIn.get(SweetBerryBushBlock.AGE) > 0 && (entityIn.lastTickPosX != entityIn.getPosX() || entityIn.lastTickPosZ != entityIn.getPosZ())) {
                        double d0 = Math.abs(entityIn.getPosX() - entityIn.lastTickPosX);
                        double d1 = Math.abs(entityIn.getPosZ() - entityIn.lastTickPosZ);
                        if (d0 >= 0.003F || d1 >= 0.003F) {
                            entityIn.attackEntityFrom(DamageSource.SWEET_BERRY_BUSH, 1.0F);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        BlockState stateIn = getContainedState(worldIn, pos);
        if (SnowCommonConfig.retainOriginalBlocks) {
            worldIn.setBlockState(pos, stateIn);
            return;
        }
        super.randomTick(state, worldIn, pos, random);
        if (stateIn.getBlock() instanceof TallGrassBlock) {
            return;
        }
        BlockState stateNow = worldIn.getBlockState(pos);
        if (stateNow.getBlock() != state.getBlock()) {
            return;
        }
        stateIn.randomTick(worldIn, pos, random);
        BlockState stateNow2 = worldIn.getBlockState(pos);
        if (stateNow2.getBlock() != state.getBlock()) {
            convert(worldIn, pos, stateNow2, stateNow.get(LAYERS), 3);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        getContainedState(worldIn, pos).onBlockActivated(wrapWorld(worldIn), player, handIn, hit);
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isRemote) {
            return;
        }
        try {
            BlockState contained = getContainedState(worldIn, pos);
            if (contained.getBlockHardness(worldIn, pos) == 0) {
                worldIn.playEvent(2001, pos, Block.getStateId(contained));
                Block.spawnDrops(contained, worldIn, pos, null, player, ItemStack.EMPTY);
                int layers = state.get(LAYERS);
                worldIn.setBlockState(pos, MainModule.BLOCK.getDefaultState().with(LAYERS, layers));
            }
        } catch (Exception e) {
        }
    }

    public static World wrapWorld(World world) {
        if (SnowCommonConfig.advancedBlockInteraction) {
            return new WrappedWorld(world);
        } else {
            return world;
        }
    }

}
