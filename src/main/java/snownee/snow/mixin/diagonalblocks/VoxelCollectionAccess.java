package snownee.snow.mixin.diagonalblocks;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fuzs.diagonalblocks.world.phys.shapes.NoneVoxelShape;
import fuzs.diagonalblocks.world.phys.shapes.VoxelCollection;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(value = VoxelCollection.class, remap = false)
public interface VoxelCollectionAccess {
	@Accessor
	VoxelShape getOutlineShape();

	@Accessor
	List<NoneVoxelShape> getNoneVoxels();
}
