package snownee.snow.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
	private static final ThreadLocal<Map<BlockPos, Integer>> srm$layers = ThreadLocal.withInitial(HashMap::new);

	@WrapOperation(
			method = "getSurroundingBlockStates",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private static BlockState srm_getSurroundingBlockState(ServerLevel level, BlockPos pos, Operation<BlockState> original) {
		BlockState state = original.call(level, pos);
		if (state.getBlock() instanceof SnowVariant snow) {
			BlockState raw = snow.srm$getRaw(state, level, pos);
			if (!raw.isAir()) {
				return raw;
			}
		}
		return state;
	}

	@WrapMethod(method = "removeSaplings")
	private static void srm_removeSaplings(
			ServerLevel level,
			List<Pair<BlockState, BlockPos>> saplings,
			Operation<Void> original) {
		Map<BlockPos, Integer> layers = srm$layers.get();
		layers.clear();
		for (Pair<BlockState, BlockPos> sapling : saplings) {
			BlockPos pos = sapling.getSecond();
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof SnowVariant snow) {
				int count = snow.srm$layers(state, level, pos);
				if (count > 0) {
					layers.put(pos.immutable(), count);
				}
			}
		}
		original.call(level, saplings);
	}

	@Inject(method = "growTree", at = @At("RETURN"))
	private void srm_restoreLayers(
			ServerLevel level,
			ChunkGenerator generator,
			BlockPos pos,
			BlockState state,
			RandomSource random,
			CallbackInfoReturnable<Boolean> cir) {
		Map<BlockPos, Integer> layers = srm$layers.get();
		if (layers.isEmpty()) {
			return;
		}
		layers.forEach((blockPos, count) -> Hooks.convert(
				level,
				blockPos,
				level.getBlockState(blockPos),
				count,
				Block.UPDATE_INVISIBLE,
				true,
				false));
		layers.clear();
	}
}