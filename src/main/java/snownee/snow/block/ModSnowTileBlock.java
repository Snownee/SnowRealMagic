package snownee.snow.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.IGrowable;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;

import javax.annotation.Nonnull;

public class ModSnowTileBlock extends ModSnowBlock implements IGrowable {
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

	@Nonnull
	@Override
	public BlockRenderType getRenderType(@Nonnull BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape shape = super.getCollisionShape(state, worldIn, pos, context);
		shape = VoxelShapes.combine(shape, getRaw(state, worldIn, pos).getCollisionShape(worldIn, pos), IBooleanFunction.OR);
		return VoxelShapes.combineAndSimplify(shape, VoxelShapes.fullCube(), IBooleanFunction.AND);
	}

	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
		VoxelShape shape = super.getShape(state, worldIn, pos, context);
		return VoxelShapes.combineAndSimplify(shape, getRaw(state, worldIn, pos).getShape(worldIn, pos, context), IBooleanFunction.OR);
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		if (useContext.getItem().getItem() == CoreModule.BLOCK.asItem()) {
			return super.isReplaceable(state, useContext);
		}
		if (!super.isReplaceable(state, useContext)) {
			return false;
		}
		World world = useContext.getWorld();
		BlockPos pos = useContext.getPos();
		return getRaw(state, world, pos).isReplaceable(useContext);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		BlockState state = super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		if (state.getBlock() instanceof ModSnowTileBlock) {
			BlockState contained = getRaw(state, worldIn, currentPos);
			BlockState containedNew = contained.updatePostPlacement(facing, facingState, worldIn, currentPos, facingPos);
			if (contained != containedNew) {
				setContainedState(worldIn, currentPos, containedNew, state);
			}
		}
		return state;
	}

	@Override
	public boolean allowsMovement(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull PathType type) {
		return getRaw(state, worldIn, pos).allowsMovement(worldIn, pos, type);
	}

	@SuppressWarnings("deprecation")
	public void setContainedState(IWorld world, BlockPos pos, BlockState state, BlockState snow) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof SnowTile) {
			if (state.isAir()) {
				snow = CoreModule.BLOCK.getDefaultState().with(LAYERS, snow.get(LAYERS));
				world.setBlockState(pos, snow, 3);
			} else {
				((SnowTile) tile).setState(state);
			}
		}
	}

	@Nonnull
	@Override
	public PushReaction getPushReaction(@Nonnull BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		if (willHarvest) {
			onBlockHarvested(world, pos, state, player);
		} else {
			world.playEvent(player, 2001, pos, getStateId(state));
		}
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof SnowTile) {
			BlockState newState = ((SnowTile) tile).getState();
			world.setBlockState(pos, newState);
		}
		return true;
	}

	@Nonnull
	@Override
	public String getTranslationKey() {
		return CoreModule.BLOCK.getTranslationKey();
	}

	@Override
	public void onEntityCollision(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn) {
		if (entityIn instanceof LivingEntity) {
			if (!worldIn.isRemote && worldIn.getDifficulty() != Difficulty.PEACEFUL) {
				if (getRaw(state, worldIn, pos).getBlock() instanceof WitherRoseBlock) {
					LivingEntity livingentity = (LivingEntity) entityIn;
					if (!livingentity.isInvulnerableTo(DamageSource.WITHER)) {
						livingentity.addPotionEffect(new EffectInstance(Effects.WITHER, 40));
					}
				}
			} else if (entityIn.getType() != EntityType.FOX && entityIn.getType() != EntityType.BEE) {
				BlockState stateIn = getRaw(state, worldIn, pos);
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
		BlockState stateIn = getRaw(state, worldIn, pos);
		if (SnowCommonConfig.retainOriginalBlocks) {
			worldIn.setBlockState(pos, stateIn);
			return;
		}
		super.randomTick(state, worldIn, pos, random);
		if (stateIn.getBlock() instanceof TallGrassBlock || stateIn.getBlock() instanceof DoublePlantBlock) {
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
		getRaw(state, worldIn, pos).onBlockActivated(wrapWorld(worldIn), player, handIn, hit);
		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onBlockClicked(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull PlayerEntity player) {
		if (worldIn.isRemote) {
			return;
		}
		try {
			BlockState contained = getRaw(state, worldIn, pos);
			if (!contained.isAir() && contained.getBlockHardness(worldIn, pos) == 0) {
				worldIn.playEvent(2001, pos, Block.getStateId(contained));
				Block.spawnDrops(contained, worldIn, pos, null, player, ItemStack.EMPTY);
				int layers = state.get(LAYERS);
				worldIn.setBlockState(pos, CoreModule.BLOCK.getDefaultState().with(LAYERS, layers));
			}
		} catch (Exception ignored) {
		}
	}

	public static World wrapWorld(World world) {
		if (SnowCommonConfig.advancedBlockInteraction) {
			return new WrappedWorld(world);
		} else {
			return world;
		}
	}

	@Override
	public boolean canGrow(@Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean isClient) {
		BlockState contained = getRaw(state, worldIn, pos);
		Block block = contained.getBlock();
		return block instanceof IGrowable && ((IGrowable) block).canGrow(worldIn, pos, contained, isClient);
	}

	@Override
	public boolean canUseBonemeal(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		BlockState contained = getRaw(state, worldIn, pos);
		Block block = contained.getBlock();
		return block instanceof IGrowable && ((IGrowable) block).canUseBonemeal(worldIn, rand, pos, contained);
	}

	@Override
	public void grow(@Nonnull ServerWorld worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		BlockState contained = getRaw(state, worldIn, pos);
		Block block = contained.getBlock();
		if (block instanceof IGrowable) {
			((IGrowable) block).grow(worldIn, rand, pos, contained);
			BlockState stateNow = worldIn.getBlockState(pos);
			if (stateNow.getBlock() != state.getBlock()) {
				convert(worldIn, pos, stateNow, state.get(LAYERS), 3);
			}
		}
	}

	@Nonnull
	@Override
	public Item asItem() {
		return CoreModule.ITEM;
	}

}
