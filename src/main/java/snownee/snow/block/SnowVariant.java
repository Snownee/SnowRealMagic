package snownee.snow.block;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.block.IKiwiBlock;
import snownee.snow.block.entity.SnowBlockEntity;

public interface SnowVariant extends IKiwiBlock {
	default BlockState getRaw(BlockState state, BlockGetter world, BlockPos pos) {
		if (state.hasBlockEntity()) {
			BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof SnowBlockEntity) {
				return ((SnowBlockEntity) tile).getState();
			}
		}
		return Blocks.AIR.defaultBlockState();
	}

	default BlockState onShovel(BlockState state, Level world, BlockPos pos) {
		return getRaw(state, world, pos);
	}

	default double getYOffset() {
		return 0;
	}

	@Override
	default ItemStack getPickedStack(BlockState state, BlockGetter world, BlockPos pos, @Nullable Player player, @Nullable HitResult result) {
		BlockState raw = getRaw(state, world, pos);
		if (raw.getBlock() instanceof BlockPickInteractionAware) {
			return (((BlockPickInteractionAware) raw.getBlock()).getPickedStack(raw, world, pos, player, result));
		}
		return raw.getBlock().getCloneItemStack(world, pos, raw);
	}

	//	@Override
	//	default SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
	//		if (state.hasBlockEntity() && !(state.getBlock() instanceof SnowLayerBlock)) {
	//			return WrappedSoundType.get(getRaw(state, world, pos).getSoundType(world, pos, entity));
	//		}
	//		return IForgeBlock.super.getSoundType(state, world, pos, entity);
	//	}

}
