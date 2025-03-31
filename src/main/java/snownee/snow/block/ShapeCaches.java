package snownee.snow.block;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;

@NotNullByDefault
public class ShapeCaches {

	public record Key(BlockState state, int layers) {
	}

	public static final Cache<Object, VoxelShape> VISUAL = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	public static final Cache<Object, VoxelShape> COLLIDER = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	public static final Cache<Object, VoxelShape> OUTLINE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

	public static VoxelShape get(Cache<Object, VoxelShape> cache, BlockState blockState, Function<BlockState, VoxelShape> shapeFunc) {
		return get(cache, blockState, shapeFunc, Shapes::or);
	}

	public static VoxelShape get(
			Cache<Object, VoxelShape> cache,
			BlockState blockState,
			Function<BlockState, VoxelShape> shapeFunc,
			BinaryOperator<VoxelShape> shapeMerger) {
		blockState = blockState.trySetValue(BlockStateProperties.WATERLOGGED, false);
		blockState = blockState.trySetValue(StairBlock.HALF, Half.BOTTOM);
		VoxelShape shape = cache.getIfPresent(blockState);
		if (shape == null) {
			SnowVariant snowVariant = (SnowVariant) blockState.getBlock();
			int layers = snowVariant.srm$layers(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
			BlockState key = blockState;
			if (layers != 1 && blockState.getBlock() instanceof SnowWallBlock) {
				blockState = blockState.setValue(SnowVariant.OPTIONAL_LAYERS, 1);
			}
			shape = shapeFunc.apply(blockState);
			if (layers != 0 && blockState.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
				BlockState snowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
				VoxelShape snowShape;
				if (cache == VISUAL) {
					snowShape = snowState.getOcclusionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
				} else if (cache == COLLIDER) {
					snowShape = snowState.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
				} else {
					snowShape = snowState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
				}
				shape = shapeMerger.apply(shape, snowShape);
			}
			cache.put(key, shape);
		}
		return shape;
	}

	public static VoxelShape get(
			Cache<Object, VoxelShape> cache,
			BlockState blockState,
			BlockGetter level,
			BlockPos pos,
			Callable<? extends VoxelShape> loader) {
		blockState = blockState.trySetValue(BlockStateProperties.WATERLOGGED, false);
		try {
			SnowVariant snowVariant = (SnowVariant) blockState.getBlock();
			int layers = snowVariant.srm$layers(blockState, level, pos);
			Key key;
			if (blockState.is(CoreModule.SNOW_TAG)) { // block like flowers has offset, so we can't cache it
				BlockState raw = snowVariant.srm$getRaw(blockState, level, pos);
				Class<?> clazz = raw.getBlock().getClass();
				if (!(clazz == TallGrassBlock.class || clazz == TallFlowerBlock.class)) {
					return loader.call();
				}
				key = new Key(raw, layers);
			} else {
				key = new Key(blockState, layers);
			}
			return cache.get(key, loader);
		} catch (Exception e) {
			SnowRealMagic.LOGGER.error("", e);
			throw new RuntimeException(e);
		}
	}

	public static void invalidateAll() {
		VISUAL.invalidateAll();
		COLLIDER.invalidateAll();
		OUTLINE.invalidateAll();
	}

}