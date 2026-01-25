package snownee.snow.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExtraCollisionSnowLayerBlock extends SRMSnowLayerBlock {
	public ExtraCollisionSnowLayerBlock(Properties properties) {
		super(properties.dynamicShape());
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ShapeCaches.get(
				ShapeCaches.COLLIDER, state, level, pos, () -> {
					VoxelShape shape = super.getCollisionShape(state, level, pos, context);
					return Shapes.or(shape, srm$getRaw(state, level, pos).getCollisionShape(level, pos, context));
				});
	}
}
