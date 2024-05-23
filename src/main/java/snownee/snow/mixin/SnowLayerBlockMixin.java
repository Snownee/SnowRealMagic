package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.util.CommonProxy;

@Mixin(value = SnowLayerBlock.class, priority = 500)
public class SnowLayerBlockMixin extends Block implements SnowVariant {
	// NaturalSpawner#getTopNonCollidingPos
	@Unique
	private static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[]{
			Shapes.empty(),
			Block.box(0, 0, 0, 16, 1, 16),
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(0, 0, 0, 16, 4, 16),
			Block.box(0, 0, 0, 16, 5, 16),
			Block.box(0, 0, 0, 16, 6, 16),
			Block.box(0, 0, 0, 16, 8, 16)};
	@Final
	@Shadow
	protected static VoxelShape[] SHAPE_BY_LAYER;

	public SnowLayerBlockMixin(Block.Properties properties) {
		super(properties);
	}

	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	private void getCollisionShape(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			CollisionContext context,
			CallbackInfoReturnable<VoxelShape> ci) {
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		if (CommonProxy.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
			ci.setReturnValue(SHAPE_BY_LAYER[layers - 1]);
			return;
		}
		if (layers == 8) {
			ci.setReturnValue(Shapes.block());
			return;
		}
		if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
			Entity entity = entityContext.getEntity();
			if (entity.getType() == EntityType.FALLING_BLOCK || CoreModule.ENTITY.is(entity.getType())) {
				ci.setReturnValue(SHAPE_BY_LAYER[layers - 1]);
				return;
			}
		}
		ci.setReturnValue(SNOW_SHAPES_MAGIC[layers - 1]);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (SnowCommonConfig.snowGravity) {
			level.scheduleTick(pos, this, getDelayAfterPlace());
		}
	}

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void updateShape(
			BlockState stateIn,
			Direction facing,
			BlockState facingState,
			LevelAccessor level,
			BlockPos currentPos,
			BlockPos facingPos,
			CallbackInfoReturnable<BlockState> ci) {
		if (SnowCommonConfig.snowGravity) {
			level.scheduleTick(currentPos, this, getDelayAfterPlace());
			ci.setReturnValue(stateIn);
		}
	}

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void canSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		ci.setReturnValue(Hooks.canSnowSurvive(state, level, pos));
	}

	@Unique
	protected int getDelayAfterPlace() {
		return 2;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockPos posDown = pos.below();
		if (Hooks.canFallThrough(level.getBlockState(posDown), level, posDown)) {
			level.setBlockAndUpdate(pos, getRaw(state, level, pos));
			FallingSnowEntity entity = new FallingSnowEntity(
					level,
					pos.getX() + 0.5D,
					pos.getY() - 0.5D,
					pos.getZ() + 0.5D,
					state.getValue(SnowLayerBlock.LAYERS));
			level.addFreshEntity(entity);
		}
	}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		Hooks.randomTick(state, level, pos, random);
		ci.cancel();
	}

	@Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
	private void canBeReplaced(BlockState state, BlockPlaceContext useContext, CallbackInfoReturnable<Boolean> ci) {
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		if (useContext.getItemInHand().is(Items.SNOW) && layers < 8) {
			if (useContext.replacingClickedOnBlock()) {
				ci.setReturnValue(useContext.getClickedFace() == Direction.UP);
			} else {
				ci.setReturnValue(true);
			}
		}
		ci.setReturnValue(layers == 1 || (SnowCommonConfig.snowAlwaysReplaceable && layers < 8));
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

	// Right click with block item to place it inside snow layer
	@Override
	protected ItemInteractionResult useItemOn(
			ItemStack itemStack,
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			InteractionHand interactionHand,
			BlockHitResult blockHitResult) {
		if (Hooks.useSnowWithItem(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult, this)) {
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	// Right click with empty hand to toggle snow overlay on the block below
	@Override
	protected InteractionResult useWithoutItem(
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			BlockHitResult blockHitResult) {
		if (Hooks.useSnowWithEmptyHand(blockState, level, blockPos, player, blockHitResult)) {
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
	}

	@Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
	private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> ci) {
		ci.setReturnValue(Hooks.getStateForPlacement(context));
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			BlockState stateBelow = level.getBlockState(pos.below());
			if (stateBelow.is(CoreModule.SNOW)) {
				entityIn.causeFallDamage(fallDistance, 0.2F, level.damageSources().fall());
				return;
			}
			state = level.getBlockState(pos);
			entityIn.causeFallDamage(fallDistance, 1 - state.getValue(SnowLayerBlock.LAYERS) * 0.1F, level.damageSources().fall());
			return;
		}
		super.fallOn(level, state, pos, entityIn, fallDistance);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entityIn) {
		if (!SnowCommonConfig.thinnerBoundingBox || !state.is(this)) {
			return;
		}
		int layers = state.getValue(SnowLayerBlock.LAYERS) - 2;
		if (layers > 0) {
			double d0 = Math.abs(entityIn.getDeltaMovement().y);
			if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
				double d1 = 1 - layers * 0.05f;
				entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
			}
		}
	}

	//	@Override
	//	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
	//		ItemStack stack = getRaw(state, world, pos).getCloneItemStack(target, world, pos, player);
	//		return stack.isEmpty() ? new ItemStack(CoreModule.ITEM) : stack;
	//	}

	@Override
	public int layers(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(BlockStateProperties.LAYERS);
	}

	@Override
	public int maxLayers(BlockState state, Level level, BlockPos pos2) {
		return 8;
	}

}
