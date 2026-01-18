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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SnowBlockEntity(pos, state);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.VISUAL, state, worldIn, pos, () -> {
					VoxelShape shape = super.getVisualShape(state, worldIn, pos, context);
					return Shapes.or(shape, srm$getRaw(state, worldIn, pos).getVisualShape(worldIn, pos, context));
				});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE, state, worldIn, pos, () -> {
					VoxelShape shape = super.getShape(state, worldIn, pos, context);
					return Shapes.or(shape, srm$getRaw(state, worldIn, pos).getShape(worldIn, pos, context));
				});
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return Shapes.block();
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext useContext) {
		if (blockState.getValue(LAYERS) == 8) {
			return false;
		}
		if (useContext.getItemInHand().is(Items.SNOW)) {
			return super.canBeReplaced(blockState, useContext);
		}
		if (super.canBeReplaced(blockState, useContext)) {
			return true;
		}
		Level level = useContext.getLevel();
		BlockPos pos = useContext.getClickedPos();
		return srm$getRaw(blockState, level, pos).canBeReplaced(useContext);
	}

	@Override
	public BlockState updateShape(
			BlockState stateIn,
			LevelReader level,
			ScheduledTickAccess ticks,
			BlockPos pos,
			Direction directionToNeighbour,
			BlockPos neighbourPos,
			BlockState neighbourState,
			RandomSource random) {
		BlockState state = super.updateShape(stateIn, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
		if (!(level instanceof WorldGenRegion) && state.getBlock() instanceof SRMSnowLayerBlock) {
			BlockState contained = srm$getRaw(state, level, pos);
			BlockState containedNew = contained.updateShape(level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
			if (contained != containedNew) {
				if (containedNew.isAir()) {
//					level.destroyBlock(pos, true);
					return srm$getSnowState(stateIn, level, pos);
				} else {
					setContainedState(level, pos, containedNew);
				}
			}
		}
		return state;
	}

	public static void setContainedState(LevelReader world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof SnowBlockEntity be) {
			be.setContainedState(state);
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack itemStack) {
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
		var blockState = srm$getRaw(state, level, pos);
		var block = blockState.getBlock();

		if (blockState.is(CoreModule.ENTITY_INSIDE)) {
			try {
				((BlockBehaviourAccess) block).callEntityInside(state, level, pos, entity, effectApplier, isPrecise);
			} catch (Throwable ignored) {
			}
		}
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		BlockState stateIn = srm$getRaw(state, worldIn, pos);
		try {
			super.randomTick(state, worldIn, pos, random);
		} catch (Throwable e) {
			return;
		}
		if (stateIn.getBlock() instanceof TallGrassBlock || stateIn.getBlock() instanceof DoublePlantBlock) {
			return;
		}
		BlockState stateNow = worldIn.getBlockState(pos);
		if (!stateNow.is(this)) {
			return;
		}
		try {
			stateIn.randomTick(worldIn, pos, random);
			BlockState stateNow2 = worldIn.getBlockState(pos);
			if (!stateNow2.is(this)) {
				Hooks.convert(worldIn, pos, stateNow2, stateNow.getValue(LAYERS), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE, true);
			}
		} catch (Throwable ignored) {
		}
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			BlockHitResult blockHitResult) {
		try {
			InteractionResult result = srm$getRaw(blockState, level, blockPos).useWithoutItem(level, player, blockHitResult);
			if (result.consumesAction()) {
				BlockState stateNow = level.getBlockState(blockPos);
				if (!stateNow.is(this)) {
					Hooks.convert(
							level,
							blockPos,
							stateNow,
							blockState.getValue(LAYERS),
							Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE,
							true);
				}
				return result;
			}
		} catch (Throwable ignored) {
		}
		return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
	}

	@Override
	protected InteractionResult useItemOn(
			ItemStack itemStack,
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			InteractionHand interactionHand,
			BlockHitResult blockHitResult) {
		try {
			InteractionResult result = srm$getRaw(blockState, level, blockPos).useItemOn(
					itemStack,
					level,
					player,
					interactionHand,
					blockHitResult);
			if (result.consumesAction()) {
				BlockState stateNow = level.getBlockState(blockPos);
				if (!stateNow.is(this)) {
					Hooks.convert(
							level,
							blockPos,
							stateNow,
							blockState.getValue(LAYERS),
							Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE,
							true);
				}
				return result;
			}
		} catch (Throwable ignored) {
		}
		return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide()) {
			return;
		}
		try {
			BlockState contained = srm$getRaw(state, worldIn, pos);
			if (!contained.isAir() && contained.getDestroySpeed(worldIn, pos) == 0) {
				worldIn.levelEvent(2001, pos, Block.getId(contained));
				Block.dropResources(contained, worldIn, pos, null, player, ItemStack.EMPTY);
				int layers = state.getValue(LAYERS);
				worldIn.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(LAYERS, layers));
			}
		} catch (Throwable ignored) {
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		BlockState contained = srm$getRaw(blockState, levelReader, blockPos);
		Block block = contained.getBlock();
		return block instanceof BonemealableBlock && ((BonemealableBlock) block).isValidBonemealTarget(levelReader, blockPos, contained);
	}

	@Override
	public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
		BlockState contained = srm$getRaw(state, worldIn, pos);
		Block block = contained.getBlock();
		return block instanceof BonemealableBlock && ((BonemealableBlock) block).isBonemealSuccess(worldIn, rand, pos, contained);
	}

	@Override
	public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
		BlockState contained = srm$getRaw(state, worldIn, pos);
		Block block = contained.getBlock();
		if (block instanceof BonemealableBlock) {
			((BonemealableBlock) block).performBonemeal(worldIn, rand, pos, contained);
			BlockState stateNow = worldIn.getBlockState(pos);
			Hooks.convert(worldIn, pos, stateNow, state.getValue(LAYERS), Block.UPDATE_ALL, true);
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
