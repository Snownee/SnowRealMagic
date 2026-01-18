package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.Hooks;
import snownee.snow.mixin.FenceGateBlockAccess;

public class SnowFenceGateBlock extends FenceGateBlock implements OptionalLayerSnowVariant, WaterLoggableSnowVariant {

	public SnowFenceGateBlock(Properties properties) {
		super(WoodType.OAK, properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.COLLIDER,
				blockState,
				it -> super.getCollisionShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

//	@Override
//	public VoxelShape getOcclusionShape(BlockState blockState) {
//		return ShapeCaches.get(ShapeCaches.VISUAL, blockState, super::getOcclusionShape);
//	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE,
				blockState,
				it -> super.getShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

	@Override
	public VoxelShape getVisualShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return blockState.getCollisionShape(worldIn, pos, context);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		Hooks.randomTick(state, worldIn, pos, random);
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
		adjustSounds(blockState, level, blockPos);
		return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			Player player,
			BlockHitResult blockHitResult) {
		adjustSounds(blockState, level, blockPos);
		return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
	}

	private void adjustSounds(BlockState blockState, LevelReader level, BlockPos blockPos) {
		BlockState raw = srm$getRaw(blockState, level, blockPos);
		if (raw.getBlock() instanceof FenceGateBlock) {
			FenceGateBlockAccess rawFenceGate = (FenceGateBlockAccess) raw.getBlock();
			FenceGateBlockAccess fenceGate = (FenceGateBlockAccess) blockState.getBlock();
			fenceGate.setType(rawFenceGate.getType());
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OPTIONAL_LAYERS);
	}

	@Override
	protected BlockState updateShape(
			BlockState state,
			LevelReader level,
			ScheduledTickAccess ticks,
			BlockPos pos,
			Direction directionToNeighbour,
			BlockPos neighbourPos,
			BlockState neighbourState,
			RandomSource random) {
		adjustSounds(state, level, pos);
		state = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
		if (!Hooks.canSnowSurvive(level, pos)) {
			state = state.setValue(OPTIONAL_LAYERS, 0);
		}
		return state;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return Hooks.canBeReplaced(state, context);
	}

}
