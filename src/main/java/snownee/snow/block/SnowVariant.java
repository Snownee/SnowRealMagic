package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeBlock;
import snownee.snow.WrappedSoundType;

public interface SnowVariant extends IForgeBlock {
	default BlockState getRaw(BlockState state, IBlockReader world, BlockPos pos) {
		if (state.hasTileEntity()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof SnowTile) {
				return ((SnowTile) tile).getState();
			}
		}
		return Blocks.AIR.getDefaultState();
	}

	default BlockState onShovel(BlockState state, World world, BlockPos pos) {
		return getRaw(state, world, pos);
	}

	default double getYOffset() {
		return 0;
	}

	@Override
	default ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return getRaw(state, world, pos).getPickBlock(target, world, pos, player);
	}

	@Override
	default SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		if (hasTileEntity(state) && !(state.getBlock() instanceof SnowBlock)) {
			return WrappedSoundType.get(getRaw(state, world, pos).getSoundType(world, pos, entity));
		}
		return IForgeBlock.super.getSoundType(state, world, pos, entity);
	}

}
