package snownee.snow.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.GameEvents;
import snownee.snow.WrappedSoundType;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.mixin.BlockAccess;

@NotNullByDefault
public interface SnowVariant extends IKiwiBlock {
	IntegerProperty OPTIONAL_LAYERS = IntegerProperty.create("layers", 0, 8);

	default BlockState srm$getRaw(BlockState state, BlockGetter level, BlockPos pos) {
		if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof SnowBlockEntity be) {
			return be.getContainedState();
		}
		return Blocks.AIR.defaultBlockState();
	}

	default BlockState srm$decreaseLayer(BlockState state, Level level, BlockPos pos, boolean byPlayer) {
		return srm$getRaw(state, level, pos);
	}

	default double srm$getYOffset() {
		return 0;
	}

	@Override
	default ItemStack getCloneItemStack(BlockState state, HitResult result, LevelReader level, BlockPos pos, Player player) {
		return srm$getRaw(state, level, pos).getCloneItemStack(result, level, pos, player);
	}

	@Override
	default SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
		if (state.hasBlockEntity() && !(state.getBlock() instanceof SnowLayerBlock)) {
			return WrappedSoundType.get(srm$getRaw(state, level, pos).getSoundType(level, pos, entity));
		}
		return IKiwiBlock.super.getSoundType(state, level, pos, entity);
	}

	@Override
	default BlockState getAppearance(
			BlockState state,
			BlockAndTintGetter level,
			BlockPos pos,
			Direction side,
			@Nullable BlockState queryState,
			@Nullable BlockPos sourcePos) {
		if (srm$layers(state, level, pos) > 0 && queryState != null && queryState.is(BlockTags.SNOW)) {
			return srm$getSnowState(state, level, pos);
		}
		return srm$getRaw(state, level, pos);
	}

	default int srm$layers(BlockState state, BlockGetter level, BlockPos pos) {
		return 0;
	}

	default int srm$maxLayers(BlockState state, Level level, BlockPos pos2) {
		return 0;
	}

	default BlockState srm$getSnowState(BlockState state, BlockGetter level, BlockPos pos) {
		int layers = srm$layers(state, level, pos);
		return layers == 0 ? Blocks.AIR.defaultBlockState() : Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, layers);
	}

	@Override
	default boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		if (willHarvest) {
			state.getBlock().playerWillDestroy(level, pos, state, player);
		} else {
			((BlockAccess) state.getBlock()).callSpawnDestroyParticles(level, player, pos, state);
		}
		if (GameEvents.onDestroyedByPlayer(level, player, pos, state, level.getBlockEntity(pos))) {
			return IKiwiBlock.super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
		}
		return true;
	}

}
