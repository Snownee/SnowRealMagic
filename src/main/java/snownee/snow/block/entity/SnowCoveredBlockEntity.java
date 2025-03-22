package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;

@NotNullByDefault
public class SnowCoveredBlockEntity extends SnowBlockEntity {

	public SnowCoveredBlockEntity(BlockPos pos, BlockState state) {
		super(CoreModule.TEXTURE_TILE.get(), pos, state);
		options.renderOverlay = true;
	}

	@Override
	public void loadContainedState(CompoundTag data, boolean network) {
		boolean changed = false;
		BlockState blockState = parseContainedState(data);
		if (!blockState.isAir()) {
			changed = setContainedState(blockState, network);
		}
		if (changed && network) {
			refresh();
		}
	}

	@Override
	public boolean setContainedState(BlockState state, boolean update) {
		return super.setContainedState(Hooks.copyProperties(getBlockState(), state), update);
	}

	@Override
	public void saveContainedState(CompoundTag data, boolean network) {
		data.putString("Block", BuiltInRegistries.BLOCK.getKey(getContainedState().getBlock()).toString());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlockState(BlockState blockState) {
		super.setBlockState(blockState);
		setContainedState(state, false);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (level != null && level.isClientSide) {
			setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 11);
		}
	}

}
