package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@NotNullByDefault
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
			Block.box(0, 0, 0, 16, 6, 16)};
	@Final
	@Shadow
	protected static VoxelShape[] SHAPE_BY_LAYER;

	public SnowLayerBlockMixin(Block.Properties properties) {
		super(properties);
	}

	@WrapMethod(method = "getCollisionShape")
	private VoxelShape srm_getCollisionShape(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			CollisionContext context,
			Operation<VoxelShape> original) {
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		if (!SnowCommonConfig.thinnerBoundingBox) {
			return SHAPE_BY_LAYER[layers - 1];
		}
		if (layers == 8) {
			return Shapes.block();
		}
		if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
			Entity entity = entityContext.getEntity();
			if (entity.getType() == EntityType.FALLING_BLOCK) {
				return SHAPE_BY_LAYER[layers - 1];
			}
		}
		return SNOW_SHAPES_MAGIC[layers - 1];
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (Hooks.isFallable(state)) {
			level.scheduleTick(pos, this, srm$getDelayAfterPlace());
		}
	}

	@WrapMethod(method = "updateShape")
	private BlockState srm_updateShape(
			BlockState stateIn,
			Direction facing,
			BlockState facingState,
			LevelAccessor level,
			BlockPos currentPos,
			BlockPos facingPos,
			Operation<BlockState> original) {
		if (Hooks.isFallable(stateIn)) {
			level.scheduleTick(currentPos, this, srm$getDelayAfterPlace());
			return stateIn;
		}
		return original.call(stateIn, facing, facingState, level, currentPos, facingPos);
	}

	@WrapOperation(
			method = "canSurvive", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
	private boolean srm_canSurvive(BlockState blockState, Block block, Operation<Boolean> original) {
		return blockState.getBlock() instanceof SnowLayerBlock || original.call(blockState, block);
	}

	@Unique
	protected int srm$getDelayAfterPlace() {
		return 2;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockPos posDown = pos.below();
		if (Hooks.canFallThrough(level.getBlockState(posDown), level, posDown)) {
			level.setBlockAndUpdate(pos, srm$getRaw(state, level, pos));
			FallingBlockEntity.fall(
					level,
					pos,
					Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, state.getValue(SnowLayerBlock.LAYERS)));
		}
	}

	@WrapMethod(method = "randomTick")
	private void srm_randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Operation<Void> original) {
		Hooks.randomTick(state, level, pos, random);
	}

	@WrapMethod(method = "canBeReplaced")
	private boolean srm_canBeReplaced(BlockState blockState, BlockPlaceContext useContext, Operation<Boolean> original) {
		return Hooks.canBeReplaced(blockState, useContext);
	}

	@Override
	public BlockState srm$decreaseLayer(BlockState state, Level level, BlockPos pos, boolean byPlayer) {
		int layers = state.getValue(SnowLayerBlock.LAYERS) - 1;
		if (layers > 0) {
			return state.setValue(SnowLayerBlock.LAYERS, layers);
		} else {
			return srm$getRaw(state, level, pos);
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

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			BlockState stateBelow = level.getBlockState(pos.below());
			if (stateBelow.is(CoreModule.SNOW_TAG)) {
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
	public int srm$layers(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(SnowLayerBlock.LAYERS);
	}

	@Override
	public int srm$maxLayers(BlockState state, Level level, BlockPos pos2) {
		return 8;
	}

}
