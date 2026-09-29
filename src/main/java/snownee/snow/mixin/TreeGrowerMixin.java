package snownee.snow.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import snownee.snow.Hooks;
import snownee.snow.block.SnowVariant;

@Mixin(value = TreeGrower.class, priority = 200)
public class TreeGrowerMixin {
    @ModifyReturnValue(method = "getSurroundingBlockStates", at = @At("RETURN"))
    private static List<Pair<BlockState, BlockPos>> srm_unwrapSaplings(
            List<Pair<BlockState, BlockPos>> original, ServerLevel level) {
        return original.stream().map(pair -> {
            BlockState state = pair.getFirst();
            if (state.getBlock() instanceof SnowVariant snow) {
                return Pair.of(snow.srm$getRaw(state, level, pair.getSecond()), pair.getSecond());
            }
            return pair;
        }).toList();
    }

    @WrapMethod(method = "growTree")
    private boolean srm_preserveSnowOnFailedGrowth(
            ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state,
            RandomSource random, Operation<Boolean> original) {
        // Any of the four 2x2 arrangements can be selected, so snapshot the surrounding 3x3.
        List<SnowSnapshot> snapshots = new ArrayList<>();
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
            BlockState candidateState = level.getBlockState(candidate);
            if (candidateState.getBlock() instanceof SnowVariant snow) {
                snapshots.add(new SnowSnapshot(candidate.immutable(), snow.srm$getRaw(candidateState, level, candidate),
                        snow.srm$layers(candidateState, level, candidate)));
            }
        }
        boolean grown = original.call(level, generator, pos, state, random);
        if (!grown) {
            for (SnowSnapshot snapshot : snapshots) {
                BlockState current = level.getBlockState(snapshot.pos());
                if (current.is(snapshot.state().getBlock()) || current.getBlock() instanceof SnowVariant) {
                    Hooks.convert(level, snapshot.pos(), snapshot.state(), snapshot.layers(), Block.UPDATE_INVISIBLE, true, false);
                }
            }
        }
        return grown;
    }

    public record SnowSnapshot(BlockPos pos, BlockState state, int layers) {}
}
