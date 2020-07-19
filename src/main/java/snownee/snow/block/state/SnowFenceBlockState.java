package snownee.snow.block.state;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.snow.block.SnowFenceBlock;

public class SnowFenceBlockState extends BlockState {

    private static Map<PositionKey, Material> cachedMaterials = new HashMap<>();

    public SnowFenceBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> properties, MapCodec<BlockState> mapCodec) {
        super(block, properties, mapCodec);
    }

    @Override
    public boolean isNormalCube(IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isSolidSide(IBlockReader world, BlockPos pos, @Nonnull Direction side) {
        BlockPos neighborPos = pos.offset(side);
        BlockState neighborState = world.getBlockState(neighborPos);
        //If the block requesting our "solid" status is a fence we want to fake it if our source material is the same as theirs
        if (neighborState.isIn(BlockTags.FENCES)) {
            return SnowFenceBlock.isWooden(world, pos, this) == SnowFenceBlock.isWooden(world, neighborPos, neighborState);
        }
        return super.isSolidSide(world, pos, side);
    }

    @Override
    public Material getMaterial() {
        //Fallback to a custom material to ensure it does not match so that other fences only connect
        // when our custom blockstate says that we are solid
        return SnowFenceBlock.NO_MATCH;
    }

    public static Material getMaterial(BlockState blockState, IBlockReader world, BlockPos pos) {
        Block block = blockState.getBlock();
        Material material;
        if (block instanceof SnowFenceBlock) {
            if (world instanceof World) {
                //If we have world information and are a snow fence block, check if we have a cached material type that we are supposed to use
                //Note: This cache is invalidated after the blocks finish changing as by then we are able to get the proper
                // value from the world and don't have to deal with all the different edge cases for when the cache needs to be invalidated
                PositionKey cacheKey = new PositionKey((World) world, pos);
                if (cachedMaterials.containsKey(cacheKey)) {
                    return cachedMaterials.get(cacheKey);
                }
            }
            material = ((SnowFenceBlock) block).getRaw(blockState, world, pos).getMaterial();
        } else {
            //If the block is not one of our blocks just grab the material the normal way.
            //This is likely to be the case for when we are getting the neighboring material.
            material = blockState.getMaterial();
        }
        return material;
    }

    public static void setCachedMaterial(World world, BlockPos pos, Material material) {
        cachedMaterials.put(new PositionKey(world, pos), material);
    }

    public static void clearCachedMaterial(World world, BlockPos pos) {
        cachedMaterials.remove(new PositionKey(world, pos));
    }

    private static class PositionKey {

        private final RegistryKey<World> dim;
        private final boolean remote;
        private final BlockPos pos;

        PositionKey(World world, BlockPos pos) {
            //Keep track of the dimension to make sure that if a spot is edited in multiple worlds at once we don't break.
            this.dim = world.func_234923_W_();
            //Ensure we have an immutable position
            this.pos = pos.toImmutable();
            //We need to keep track of remote so that in single player we don't clear our cache too early by both sides sharing a cache
            this.remote = world.isRemote();
        }

        @Override
        public int hashCode() {
            int code = remote ? 1 : 31;
            code = 31 * code + dim.hashCode();
            code = 31 * code + pos.hashCode();
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof PositionKey) {
                PositionKey other = (PositionKey) obj;
                return remote == other.remote && dim.equals(other.dim) && pos.equals(other.pos);
            }
            return false;
        }
    }
}
