package snownee.snow.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.entity.FallingSnowEntity;

@Mixin(SnowLayerBlock.class)
public class SnowLayerBlockMixin extends Block implements SnowVariant {
	private static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[] { Shapes.empty(), Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 0, 0, 16, 6, 16), Block.box(0, 0, 0, 16, 7, 16) };
	@Shadow
	private static VoxelShape[] SHAPE_BY_LAYER;

	public SnowLayerBlockMixin(Block.Properties properties) {
		super(properties);
	}

	@Override
	@Overwrite
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (ModUtil.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
			return SHAPE_BY_LAYER[state.getValue(SnowLayerBlock.LAYERS) - 1];
		}
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		if (layers == 8) {
			return Shapes.block();
		}
		return SNOW_SHAPES_MAGIC[layers - 1];
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.scheduleTick(pos, this, tickRate());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	@Overwrite
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.scheduleTick(currentPos, this, tickRate());
			return stateIn;
		} else {
			return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}
	}

	@Override
	@Overwrite
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return Hooks.canSnowSurvive(state, worldIn, pos);
	}

	protected int tickRate() {
		return 2;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		checkFallable(worldIn, pos, state);
	}

	@Override
	@Overwrite
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		Hooks.randomTick(state, worldIn, pos, random);
	}

	protected boolean checkFallable(Level worldIn, BlockPos pos, BlockState state) {
		BlockPos posDown = pos.below();
		if (Hooks.canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) {
			if (!worldIn.isClientSide) {
				worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
				FallingSnowEntity entity = new FallingSnowEntity(worldIn, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, state.getValue(SnowLayerBlock.LAYERS));
				worldIn.addFreshEntity(entity);
			}
			return true;
		}
		return false;
	}

	@Override
	@Overwrite
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		int i = state.getValue(SnowLayerBlock.LAYERS);
		if (useContext.getItemInHand().is(Blocks.SNOW.asItem()) && i < 8) {
			if (useContext.replacingClickedOnBlock() && state.is(Blocks.SNOW)) {
				return useContext.getClickedFace() == Direction.UP;
			} else {
				return true;
			}
		}
		return (SnowCommonConfig.snowAlwaysReplaceable && state.getValue(SnowLayerBlock.LAYERS) < 8) || i == 1;
	}

	@Override
	public BlockState onShovel(BlockState state, Level world, BlockPos pos) {
		int layers = state.getValue(SnowLayerBlock.LAYERS) - 1;
		if (layers > 0) {
			return state.setValue(SnowLayerBlock.LAYERS, layers);
		} else {
			return getRaw(state, world, pos);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
			BlockState stateDown = worldIn.getBlockState(pos.below());
			if (!(stateDown.getBlock() instanceof SnowLayerBlock) && !stateDown.hasProperty(BlockStateProperties.SNOWY)) {
				if (state.is(Blocks.SNOW)) {
					worldIn.setBlock(pos, Hooks.copyProperties(state, CoreModule.TILE_BLOCK.defaultBlockState()), 16 | 32);
				}
				BlockEntity blockEntity = worldIn.getBlockEntity(pos);
				if (blockEntity instanceof SnowBlockEntity) {
					SnowBlockEntity snowTile = (SnowBlockEntity) blockEntity;
					if (CoreModule.TILE_BLOCK.is(state) && snowTile.getState().isAir()) {
						worldIn.setBlock(pos, Hooks.copyProperties(state, Blocks.SNOW.defaultBlockState()), 16 | 32);
					} else {
						snowTile.options.renderOverlay = !snowTile.options.renderOverlay;
						if (worldIn.isClientSide) {
							worldIn.sendBlockUpdated(pos, state, state, 11);
						}
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		if (state.is(Blocks.SNOW)) {
			BlockPlaceContext context = new BlockPlaceContext(player, handIn, player.getItemInHand(handIn), hit);
			Block block = Block.byItem(context.getItemInHand().getItem());
			if (block != null && block != Blocks.AIR && context.replacingClickedOnBlock()) {
				BlockState state2 = block.getStateForPlacement(context);
				if (state2 != null && Hooks.canContainState(state2) && state2.canSurvive(worldIn, pos)) {
					if (!worldIn.isClientSide) {
						worldIn.setBlock(pos, state2, 16 | 32);
						block.setPlacedBy(worldIn, pos, state, player, context.getItemInHand());
						int i = state.getValue(SnowLayerBlock.LAYERS);
						if (Hooks.placeLayersOn(worldIn, pos, i, false, context, true, true) && !player.isCreative()) {
							context.getItemInHand().shrink(1);
						}
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		return super.use(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	@Nullable
	@Overwrite
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
		if (blockstate.getBlock() instanceof SnowLayerBlock) {
			int i = blockstate.getValue(SnowLayerBlock.LAYERS);
			return blockstate.setValue(SnowLayerBlock.LAYERS, Math.min(8, i + 1));
		}
		ItemStack stack = context.getItemInHand();
		CompoundTag tag = BlockItem.getBlockEntityData(stack);
		if (tag != null && "snowrealmagic:snow".equals(tag.getString("id"))) {
			return CoreModule.TILE_BLOCK.defaultBlockState();
		}
		return defaultBlockState();
	}

	@Override
	public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			if (!state.is(this))
				return;
			//FIXME why
			if (state.is(Blocks.SNOW) || CoreModule.TILE_BLOCK.is(state)) {
				entityIn.causeFallDamage(fallDistance, 0.2F, DamageSource.FALL);
				return;
			}
			state = worldIn.getBlockState(pos);
			entityIn.causeFallDamage(fallDistance, 1 - state.getValue(SnowLayerBlock.LAYERS) * 0.1F, DamageSource.FALL);
			return;
		}
		super.fallOn(worldIn, state, pos, entityIn, fallDistance);
	} //FIXME what if below block is snow as well

	@Override
	public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
		if (SnowCommonConfig.thinnerBoundingBox) {
			double d0 = Math.abs(entityIn.getDeltaMovement().y);
			if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
				if (!state.is(this))
					return;
				int layers = state.getValue(SnowLayerBlock.LAYERS) - 1;
				double d1 = 1 - layers * 0.05f;
				entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ItemStack stack = getRaw(state, world, pos).getCloneItemStack(target, world, pos, player);
		return stack.isEmpty() ? new ItemStack(Items.SNOW) : stack;
	}

}