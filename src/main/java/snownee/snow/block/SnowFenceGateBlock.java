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
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.COLLIDER,
				state,
				it -> super.getCollisionShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

//	@Override
//	public VoxelShape getOcclusionShape(BlockState blockState) {
//		return ShapeCaches.get(ShapeCaches.VISUAL, blockState, super::getOcclusionShape);
//	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.OUTLINE,
				state,
				it -> super.getShape(it, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getCollisionShape(level, pos, context);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		Hooks.randomTick(state, level, pos, random);
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
		adjustSounds(state, level, pos);
		return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			BlockHitResult hitResult) {
		adjustSounds(state, level, pos);
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	private void adjustSounds(BlockState blockState, LevelReader level, BlockPos blockPos) {
		BlockState raw = srm$getRaw(blockState, level, blockPos);
		if (raw.getBlock() instanceof FenceGateBlock) {
			FenceGateBlockAccess rawFenceGate = (FenceGateBlockAccess) raw.getBlock();
			FenceGateBlockAccess fenceGate = (FenceGateBlockAccess) blockState.getBlock();
			fenceGate.setType(rawFenceGate.getType());
			fenceGate.setOpenSound(rawFenceGate.getOpenSound());
			fenceGate.setCloseSound(rawFenceGate.getCloseSound());
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
