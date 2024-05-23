package snownee.snow.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.entity.SnowBlockEntity;

@KiwiModule.RenderLayer(RenderLayerEnum.CUTOUT)
public class SRMSnowLayerBlock extends SnowLayerBlock implements EntityBlock, BonemealableBlock, SnowVariant {
	public SRMSnowLayerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SnowBlockEntity(pos, state);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(ShapeCaches.VISUAL, state, worldIn, pos, () -> {
			VoxelShape shape = super.getVisualShape(state, worldIn, pos, context);
			return Shapes.or(shape, srm$getRaw(state, worldIn, pos).getVisualShape(worldIn, pos, context));
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(ShapeCaches.OUTLINE, state, worldIn, pos, () -> {
			VoxelShape shape = super.getShape(state, worldIn, pos, context);
			return Shapes.or(shape, srm$getRaw(state, worldIn, pos).getShape(worldIn, pos, context));
		});
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
		return super.getShape(p_60578_, p_60579_, p_60580_, CollisionContext.empty());
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		if (useContext.getItemInHand().is(Items.SNOW)) {
			return super.canBeReplaced(state, useContext);
		}
		if (!super.canBeReplaced(state, useContext)) {
			return false;
		}
		Level world = useContext.getLevel();
		BlockPos pos = useContext.getClickedPos();
		return srm$getRaw(state, world, pos).canBeReplaced(useContext);
	}

	@Override
	public BlockState updateShape(
			BlockState stateIn,
			Direction facing,
			BlockState facingState,
			LevelAccessor worldIn,
			BlockPos currentPos,
			BlockPos facingPos) {
		BlockState state = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		if (state.getBlock() instanceof SRMSnowLayerBlock) {
			BlockState contained = srm$getRaw(state, worldIn, currentPos);
			BlockState containedNew = contained.updateShape(facing, facingState, worldIn, currentPos, facingPos);
			if (contained != containedNew) {
				setContainedState(worldIn, currentPos, containedNew, state);
			}
		}
		return state;
	}

	public void setContainedState(LevelAccessor world, BlockPos pos, BlockState state, BlockState snow) {
		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof SnowBlockEntity) {
			if (state.isAir()) {
				world.setBlock(pos, srm$getSnowState(snow, world, pos), 3);
			} else {
				((SnowBlockEntity) tile).setContainedState(state);
			}
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		Hooks.setPlacedBy(level, pos, state);
	}

	@Override
	public String getDescriptionId() {
		return Blocks.SNOW.getDescriptionId();
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		var blockState = srm$getRaw(state, worldIn, pos);
		var block = blockState.getBlock();

		if (blockState.is(CoreModule.ENTITY_INSIDE)) {
			block.entityInside(blockState, worldIn, pos, entityIn);
		}
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		BlockState stateIn = srm$getRaw(state, worldIn, pos);
		if (SnowCommonConfig.retainOriginalBlocks) {
			worldIn.setBlockAndUpdate(pos, stateIn);
			return;
		}
		super.randomTick(state, worldIn, pos, random);
		if (stateIn.getBlock() instanceof TallGrassBlock || stateIn.getBlock() instanceof DoublePlantBlock) {
			return;
		}
		BlockState stateNow = worldIn.getBlockState(pos);
		if (!stateNow.is(this)) {
			return;
		}
		stateIn.randomTick(worldIn, pos, random);
		BlockState stateNow2 = worldIn.getBlockState(pos);
		if (!stateNow2.is(this)) {
			Hooks.convert(worldIn, pos, stateNow2, stateNow.getValue(LAYERS), 18, true);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			BlockHitResult blockHitResult) {
		InteractionResult result = srm$getRaw(blockState, level, blockPos).useWithoutItem(level, player, blockHitResult);
		if (result.consumesAction()) {
			BlockState stateNow = level.getBlockState(blockPos);
			if (!stateNow.is(this)) {
				Hooks.convert(level, blockPos, stateNow, blockState.getValue(LAYERS), 18, true);
			}
			return result;
		}
		return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
	}

	@Override
	protected ItemInteractionResult useItemOn(
			ItemStack itemStack,
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			InteractionHand interactionHand,
			BlockHitResult blockHitResult) {
		ItemInteractionResult result = srm$getRaw(blockState, level, blockPos).useItemOn(
				itemStack,
				level,
				player,
				interactionHand,
				blockHitResult);
		if (result.consumesAction()) {
			BlockState stateNow = level.getBlockState(blockPos);
			if (!stateNow.is(this)) {
				Hooks.convert(level, blockPos, stateNow, blockState.getValue(LAYERS), 18, true);
			}
			return result;
		}
		return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide) {
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
			Hooks.convert(worldIn, pos, stateNow, state.getValue(LAYERS), 3, true);
		}
	}

	@Override
	public @NotNull Item asItem() {
		return Items.SNOW;
	}
}
