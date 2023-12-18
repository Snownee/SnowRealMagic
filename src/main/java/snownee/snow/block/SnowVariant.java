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
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.extensions.IForgeBlock;
import snownee.snow.WrappedSoundType;
import snownee.snow.block.entity.SnowBlockEntity;

public interface SnowVariant extends IForgeBlock {
	IntegerProperty OPTIONAL_LAYERS = IntegerProperty.create("layers", 0, 8);

	default BlockState getRaw(BlockState state, BlockGetter level, BlockPos pos) {
		if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof SnowBlockEntity be) {
			return be.getContainedState();
		}
		return Blocks.AIR.defaultBlockState();
	}

	default BlockState decreaseLayer(BlockState state, Level level, BlockPos pos, boolean byPlayer) {
		return getRaw(state, level, pos);
	}

	default double getYOffset() {
		return 0;
	}

	@Override
	default ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return getRaw(state, level, pos).getCloneItemStack(target, level, pos, player);
	}

	@Override
	default SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
		if (state.hasBlockEntity() && !(state.getBlock() instanceof SnowLayerBlock)) {
			return WrappedSoundType.get(getRaw(state, level, pos).getSoundType(level, pos, entity));
		}
		return IForgeBlock.super.getSoundType(state, level, pos, entity);
	}

	@Override
	default BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
		if (layers(state, level, pos) > 0 && queryState != null && queryState.is(BlockTags.SNOW)) {
			return getSnowState(state, level, pos);
		}
		return getRaw(state, level, pos);
	}

	default int layers(BlockState state, BlockGetter level, BlockPos pos) {
		return 0;
	}

	default int maxLayers(BlockState state, Level level, BlockPos pos2) {
		return 0;
	}

	default BlockState getSnowState(BlockState state, BlockGetter level, BlockPos pos) {
		int layers = layers(state, level, pos);
		return layers == 0 ? Blocks.AIR.defaultBlockState() : Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, layers);
	}
}
