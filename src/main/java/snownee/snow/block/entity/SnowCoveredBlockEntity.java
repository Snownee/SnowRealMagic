package snownee.snow.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.util.Util;
import snownee.snow.CoreModule;
import snownee.snow.block.ModSnowLayerBlock;

public class SnowCoveredBlockEntity extends SnowBlockEntity {

	public SnowCoveredBlockEntity(BlockPos pos, BlockState state) {
		super(CoreModule.TEXTURE_TILE.get(), pos, state);
		options.renderOverlay = true; // stairs does not implement WatcherSnowVariant
	}

	@Override
	public void loadState(CompoundTag data, boolean network) {
		boolean changed = false;
		if (!network && data.contains("Items")) {
			String idStr = data.getCompound("Items").getString("0");
			ResourceLocation id = Util.RL(idStr);
			if (id != null) {
				Item item = ForgeRegistries.ITEMS.getValue(id);
				if (item instanceof BlockItem) {
					Block block = ((BlockItem) item).getBlock();
					changed |= setState(ModSnowLayerBlock.copyProperties(getBlockState(), block.defaultBlockState()), network);
				}
			}
		} else if (data.contains("Block")) {
			ResourceLocation id = Util.RL(data.getString("Block"));
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block != null && block != Blocks.AIR) {
				changed |= setState(ModSnowLayerBlock.copyProperties(getBlockState(), block.defaultBlockState()), network);
			}
		} else {
			changed |= setState(NbtUtils.readBlockState(data.getCompound("State")), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	@Override
	public void saveState(CompoundTag data, boolean network) {
		data.putString("Block", getState().getBlock().getRegistryName().toString());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlockState(BlockState blockState) {
		super.setBlockState(blockState);
		setState(ModSnowLayerBlock.copyProperties(getBlockState(), state), false);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (hasLevel() && level.isClientSide) {
			BlockState state = getBlockState();
			level.markAndNotifyBlock(worldPosition, level.getChunkAt(worldPosition), state, state, 11, 512);
		}
	}

}
