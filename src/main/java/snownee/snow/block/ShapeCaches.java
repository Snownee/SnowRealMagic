package snownee.snow.block;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;

public class ShapeCaches {

	public record Key(BlockState state, int layers) {
	}

	public static final Cache<Key, VoxelShape> VISUAL = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
	public static final Cache<Key, VoxelShape> COLLIDER = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
	public static final Cache<Key, VoxelShape> OUTLINE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

	public static VoxelShape get(Cache<Key, VoxelShape> cache, BlockState state, BlockGetter level, BlockPos pos, Callable<? extends VoxelShape> loader) {
		try {
			SnowVariant snowVariant = (SnowVariant) state.getBlock();
			int layers = snowVariant.layers(state, level, pos);
			Key key;
			if (CoreModule.TILE_BLOCK.is(state)) { // block like flowers has offset so we can't cache it
				BlockState raw = snowVariant.getRaw(state, level, pos);
				Class<?> clazz = raw.getBlock().getClass();
				if (!(clazz == TallGrassBlock.class || clazz == TallFlowerBlock.class)) {
					return loader.call();
				}
				key = new Key(raw, layers);
			}else {
				key = new Key(state, layers);
			}
			return cache.get(key, loader);
		} catch (Exception e) {
			SnowRealMagic.LOGGER.error("", e);
			throw new RuntimeException(e);
		}
	}

}