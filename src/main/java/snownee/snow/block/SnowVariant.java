package snownee.snow.block;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import snownee.kiwi.block.IKiwiBlock;
import snownee.snow.block.entity.SnowBlockEntity;

public interface SnowVariant extends IKiwiBlock, FabricBlock {
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
		return layers == 0 ? Blocks.AIR.defaultBlockState() : Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
	}

	default boolean srm$canRenderDecoration(BlockState blockState) {
		return false;
	}

	default double srm$renderDecorationOffset(BlockState blockState) {
		return 0.125;
	}

	default boolean srm$canRenderOverlay(BlockState blockState) {
		return true;
	}

	default double srm$renderLayerOffset(BlockState blockState) {
		return 0;
	}
}
