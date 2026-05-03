package snownee.snow.compat.diagonalblocks;

import fuzs.diagonalblocks.common.api.v2.block.StarCollisionBlock;
import fuzs.diagonalblocks.common.impl.world.phys.shapes.NoneVoxelShape;
import fuzs.diagonalblocks.common.impl.world.phys.shapes.VoxelCollection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.block.SnowVariant;
import snownee.snow.mixin.diagonalblocks.VoxelCollectionAccess;

public interface SnowStarCollisionBlock extends StarCollisionBlock {
	@Override
	default void _createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		StarCollisionBlock.super._createBlockStateDefinition(builder);
		builder.add(SnowVariant.OPTIONAL_LAYERS);
	}

	static VoxelShape mergeShape(VoxelShape base, VoxelShape snow) {
		if (base instanceof VoxelCollectionAccess baseCollection) {
			VoxelCollection collection = new VoxelCollection(snow);
			collection.addVoxelShape(baseCollection.getOutlineShape(), Shapes.empty());
			for (NoneVoxelShape noneVoxel : baseCollection.getNoneVoxels()) {
				collection.addVoxelShape(noneVoxel, Shapes.empty());
			}
			return collection.optimize();
		} else {
			return Shapes.or(base, snow);
		}
	}
}
