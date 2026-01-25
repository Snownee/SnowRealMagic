package snownee.snow.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.RenderLayerEnum;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.mixin.BlockBehaviourAccess;

@KiwiModule.RenderLayer(RenderLayerEnum.CUTOUT)
public class SRMSnowLayerBlock extends SnowLayerBlock implements EntityBlock, BonemealableBlock, SnowVariant {
	public SRMSnowLayerBlock(Properties properties) {
		super(properties.overrideDescription(Blocks.SNOW.getDescriptionId()));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
		return new SnowBlockEntity(worldPosition, blockState);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.VISUAL, state, level, pos, () -> {
					VoxelShape shape = super.getVisualShape(state, level, pos, context);
					return Shapes.or(shape, srm$getRaw(state, level, pos).getVisualShape(level, pos, context));
				});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE, state, level, pos, () -> {
					VoxelShape shape = super.getShape(state, level, pos, context);
					return Shapes.or(shape, srm$getRaw(state, level, pos).getShape(level, pos, context));
				});
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return super.getShape(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (state.getValue(LAYERS) == 8) {
			return false;
		}
		if (context.getItemInHand().is(Items.SNOW)) {
			return super.canBeReplaced(state, context);
		}
		if (super.canBeReplaced(state, context)) {
			return true;
		}
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		return srm$getRaw(state, level, pos).canBeReplaced(context);
	}

	@Override
	public BlockState updateShape(
			BlockState state,
			LevelReader level,
			ScheduledTickAccess ticks,
			BlockPos pos,
			Direction directionToNeighbour,
			BlockPos neighbourPos,
			BlockState neighbourState,
			RandomSource random) {
		BlockState stateNew = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
		if (!(level instanceof WorldGenRegion) && stateNew.getBlock() instanceof SRMSnowLayerBlock) {
			BlockState contained = srm$getRaw(stateNew, level, pos);
			BlockState containedNew = contained.updateShape(level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
			if (contained != containedNew) {
				if (containedNew.isAir()) {
//					level.destroyBlock(pos, true);
					return srm$getSnowState(state, level, pos);
				} else {
					setContainedState(level, pos, containedNew);
				}
			}
		}
		return stateNew;
	}

	public static void setContainedState(LevelReader world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof SnowBlockEntity be) {
			be.setContainedState(state);
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
		Hooks.processFancyOverlay(level, pos, itemStack);
	}

	@Override
	protected void entityInside(
			BlockState state,
			Level level,
			BlockPos pos,
			Entity entity,
			InsideBlockEffectApplier effectApplier,
			boolean isPrecise) {
		var raw = srm$getRaw(state, level, pos);
		if (raw.is(CoreModule.ENTITY_INSIDE)) {
			try {
				((BlockBehaviourAccess) raw.getBlock()).callEntityInside(raw, level, pos, entity, effectApplier, isPrecise);
			} catch (Throwable _) {
			}
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		var raw = srm$getRaw(state, level, pos);
		if (raw.is(CoreModule.ANIMATE_TICK)) {
			try {
				raw.getBlock().animateTick(raw, level, pos, random);
			} catch (Throwable _) {
			}
		}
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockState stateIn = srm$getRaw(state, level, pos);
		try {
			super.randomTick(state, level, pos, random);
		} catch (Throwable e) {
			return;
		}
		if (stateIn.getBlock() instanceof TallGrassBlock || stateIn.getBlock() instanceof DoublePlantBlock) {
			return;
		}
		BlockState stateNow = level.getBlockState(pos);
		if (!stateNow.is(this)) {
			return;
		}
		try {
			stateIn.randomTick(level, pos, random);
			BlockState stateNow2 = level.getBlockState(pos);
			if (!stateNow2.is(this)) {
				Hooks.convert(level, pos, stateNow2, stateNow.getValue(LAYERS), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE, true);
			}
		} catch (Throwable ignored) {
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		try {
			InteractionResult result = srm$getRaw(state, level, pos).useWithoutItem(level, player, hitResult);
			if (result.consumesAction()) {
				BlockState stateNow = level.getBlockState(pos);
				if (!stateNow.is(this)) {
					Hooks.convert(level, pos, stateNow, state.getValue(LAYERS), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE, true);
				}
				return result;
			}
		} catch (Throwable ignored) {
		}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	protected InteractionResult useItemOn(
			ItemStack itemStack,
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hitResult) {
		try {
			InteractionResult result = srm$getRaw(state, level, pos).useItemOn(itemStack, level, player, hand, hitResult);
			if (result.consumesAction()) {
				BlockState stateNow = level.getBlockState(pos);
				if (!stateNow.is(this)) {
					Hooks.convert(level, pos, stateNow, state.getValue(LAYERS), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE, true);
				}
				return result;
			}
		} catch (Throwable ignored) {
		}
		return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
	}

	@Override
	public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if (level.isClientSide()) {
			return;
		}
		try {
			BlockState contained = srm$getRaw(state, level, pos);
			if (!contained.isAir() && contained.getDestroySpeed(level, pos) == 0) {
				level.levelEvent(2001, pos, Block.getId(contained));
				Block.dropResources(contained, level, pos, null, player, ItemStack.EMPTY);
				int layers = state.getValue(LAYERS);
				level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(LAYERS, layers));
			}
		} catch (Throwable ignored) {
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		BlockState contained = srm$getRaw(state, level, pos);
		Block block = contained.getBlock();
		return block instanceof BonemealableBlock && ((BonemealableBlock) block).isValidBonemealTarget(level, pos, contained);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		BlockState contained = srm$getRaw(state, level, pos);
		Block block = contained.getBlock();
		return block instanceof BonemealableBlock && ((BonemealableBlock) block).isBonemealSuccess(level, random, pos, contained);
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		BlockState contained = srm$getRaw(state, level, pos);
		Block block = contained.getBlock();
		if (block instanceof BonemealableBlock) {
			((BonemealableBlock) block).performBonemeal(level, random, pos, contained);
			BlockState stateNow = level.getBlockState(pos);
			Hooks.convert(level, pos, stateNow, state.getValue(LAYERS), Block.UPDATE_ALL, true);
		}
	}

	@Override
	public Item asItem() {
		return Items.SNOW;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return false;
	}

	@Override
	protected int getLightBlock(BlockState state) {
		return 0;
	}
}
