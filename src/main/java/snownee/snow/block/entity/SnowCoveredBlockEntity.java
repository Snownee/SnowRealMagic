package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.KUtil;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;

public class SnowCoveredBlockEntity extends SnowBlockEntity {

	public SnowCoveredBlockEntity(BlockPos pos, BlockState state) {
		super(CoreModule.TEXTURE_TILE.get(), pos, state);
		options.renderOverlay = true;
	}

	@Override
	public void loadState(CompoundTag data, boolean network) {
		boolean changed = false;
		if (!network && data.contains("Items")) {
			String idStr = data.getCompound("Items").getString("0");
			ResourceLocation id = KUtil.RL(idStr);
			if (id != null) {
				Item item = BuiltInRegistries.ITEM.get(id);
				if (item instanceof BlockItem) {
					Block block = ((BlockItem) item).getBlock();
					changed |= setContainedState(Hooks.copyProperties(getBlockState(), block.defaultBlockState()), network);
				}
			}
		} else if (data.contains("Block")) {
			ResourceLocation id = KUtil.RL(data.getString("Block"));
			Block block = BuiltInRegistries.BLOCK.get(id);
			if (block != null && block != Blocks.AIR) {
				changed |= setContainedState(Hooks.copyProperties(getBlockState(), block.defaultBlockState()), network);
			}
		} else {
			changed |= setContainedState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound("State")), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	@Override
	public void saveState(CompoundTag data, boolean network) {
		data.putString("Block", BuiltInRegistries.BLOCK.getKey(getContainedState().getBlock()).toString());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlockState(BlockState blockState) {
		super.setBlockState(blockState);
		setContainedState(Hooks.copyProperties(getBlockState(), state), false);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (hasLevel() && level.isClientSide) {
			setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 11);
		}
	}

}
