package snownee.snow.block;


import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class NoCollisionSnowLayerBlock extends SRMSnowLayerBlock {
	public NoCollisionSnowLayerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return switch (pathComputationType) {
			case AIR -> true;
			case LAND, WATER -> false;
		};
	}
}
