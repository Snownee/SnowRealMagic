package snownee.snow.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.util.CommonProxy;

@Mixin(SnowLayerBlock.class)
public class SnowLayerBlockMixin extends Block implements SnowVariant {
	// NaturalSpawner#getTopNonCollidingPos
	private static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[] { Shapes.empty(), Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 0, 0, 16, 6, 16), Block.box(0, 0, 0, 16, 7, 16) };
	@Final
	@Shadow
	protected static VoxelShape[] SHAPE_BY_LAYER;

	public SnowLayerBlockMixin(Block.Properties properties) {
		super(properties);
	}

	@Override
	@Overwrite
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (CommonProxy.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
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
		if (useContext.getItemInHand().is(Items.SNOW) && i < 8) {
			if (useContext.replacingClickedOnBlock() && state.is(Blocks.SNOW)) {
				return useContext.getClickedFace() == Direction.UP;
			} else {
				return true;
			}
		}
		return (SnowCommonConfig.snowAlwaysReplaceable && state.getValue(SnowLayerBlock.LAYERS) < 8) || i == 1;
	}

	@Override
	public BlockState decreaseLayer(BlockState state, Level level, BlockPos pos, boolean byPlayer) {
		int layers = state.getValue(SnowLayerBlock.LAYERS) - 1;
		if (layers > 0) {
			return state.setValue(SnowLayerBlock.LAYERS, layers);
		} else {
			return getRaw(state, level, pos);
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
					if (CoreModule.TILE_BLOCK.is(state) && snowTile.getContainedState().isAir()) {
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
		if (getRaw(state, worldIn, pos).isAir()) {
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

	/**
	 * @author Snownee
	 * @reason
	 */
	@Override
	@Nullable
	@Overwrite
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return Hooks.getStateForPlacement(context);
	}

	@Override
	public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			BlockState stateBelow = worldIn.getBlockState(pos.below());
			if (stateBelow.is(Blocks.SNOW) || CoreModule.TILE_BLOCK.is(stateBelow)) {
				entityIn.causeFallDamage(fallDistance, 0.2F, worldIn.damageSources().fall());
				return;
			}
			state = worldIn.getBlockState(pos);
			entityIn.causeFallDamage(fallDistance, 1 - state.getValue(SnowLayerBlock.LAYERS) * 0.1F, worldIn.damageSources().fall());
			return;
		}
		super.fallOn(worldIn, state, pos, entityIn, fallDistance);
	}

	@Override
	public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
		if (!SnowCommonConfig.thinnerBoundingBox || !state.is(this))
			return;
		int layers = state.getValue(SnowLayerBlock.LAYERS) - 2;
		if (layers > 0) {
			double d0 = Math.abs(entityIn.getDeltaMovement().y);
			if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
				double d1 = 1 - layers * 0.05f;
				entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		ItemStack stack = getRaw(state, level, pos).getCloneItemStack(target, level, pos, player);
		return stack.isEmpty() ? new ItemStack(Items.SNOW) : stack;
	}

	@Override
	public int layers(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(BlockStateProperties.LAYERS);
	}

	@Override
	public int maxLayers(BlockState state, Level level, BlockPos pos2) {
		return 8;
	}
}
