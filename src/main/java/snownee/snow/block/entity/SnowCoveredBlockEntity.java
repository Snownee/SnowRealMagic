package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.block.SnowSlabBlock;
import snownee.snow.block.SnowVariant;

public class SnowCoveredBlockEntity extends SnowBlockEntity {

	public SnowCoveredBlockEntity(BlockPos pos, BlockState blockState) {
		super(CoreModule.TEXTURE_TILE.get(), pos, blockState);
		options.renderOverlay = blockState.getBlock().getClass() == SnowSlabBlock.class;
	}

	@Override
	public boolean setContainedState(BlockState state, boolean update) {
		return super.setContainedState(Hooks.copyProperties(getBlockState(), state), update);
	}

	@Override
	public void saveContainedState(ValueOutput output, boolean network) {
		output.putString("Block", BuiltInRegistries.BLOCK.getKey(getContainedState().getBlock()).toString());
		if (options.renderOverlay) {
			output.putBoolean("RO", true);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlockState(BlockState blockState) {
		super.setBlockState(blockState);
		setContainedState(containedState, false);
		if (options.renderOverlay && level != null && blockState.getBlock() instanceof SnowVariant snowVariant &&
				!snowVariant.srm$canRenderOverlay(blockState)) {
			options.renderOverlay = false;
		}
	}

	@Override
	public void refresh() {
		super.refresh();
		if (level != null && level.isClientSide()) {
			setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 11);
		}
	}

}
