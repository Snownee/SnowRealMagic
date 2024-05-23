package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import snownee.snow.Hooks;
import snownee.snow.block.SnowVariant;

@Mixin(value = TreeGrower.class, priority = 200)
public class TreeGrowerMixin {
	@WrapOperation(
			method = "isTwoByTwoSapling",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private static BlockState srm_isTwoByTwoSapling(
			final BlockGetter level,
			final BlockPos blockPos,
			final Operation<BlockState> original) {

		var blockState = original.call(level, blockPos);

		if (blockState.getBlock() instanceof SnowVariant snow) {
			return snow.srm$getRaw(blockState, level, blockPos);
		}

		return blockState;
	}

	@Inject(
			method = "growTree",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
	private void srm_initLayers(
			final ServerLevel serverLevel,
			final ChunkGenerator chunkGenerator,
			final BlockPos blockPos,
			final BlockState blockState,
			final RandomSource randomSource,
			final CallbackInfoReturnable<Boolean> cir,
			@Share("layers") LocalRef<IntList> layers) {
		layers.set(new IntArrayList(4));
	}

	@ModifyExpressionValue(
			method = "growTree",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"),
			slice = @Slice(
					to = @At(
							value = "INVOKE",
							ordinal = 3,
							target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")))
	private BlockPos srm_recordLayers(
			final BlockPos blockPos,
			ServerLevel serverLevel,
			@Share("layers") LocalRef<IntList> layers) {
		var blockState = serverLevel.getBlockState(blockPos);
		if (blockState.getBlock() instanceof SnowVariant snow) {
			layers.get().add(snow.srm$layers(blockState, serverLevel, blockPos));
		} else {
			layers.get().add(0);
		}
		return blockPos;
	}

	@WrapOperation(
			method = "growTree",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/level/levelgen/feature/ConfiguredFeature;place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z")))
	private boolean srm_recoverLayers(
			final ServerLevel serverLevel,
			final BlockPos blockPos,
			final BlockState blockState,
			final int i,
			final Operation<Boolean> original,
			@Share("layers") LocalRef<IntList> layers,
			@Share("index") LocalIntRef index) {
		var result = original.call(serverLevel, blockPos, blockState, i);

		var layer = layers.get().getInt(index.get());
		if (layer > 0) {
			Hooks.convert(serverLevel, blockPos, blockState, layer, 4, true);
		}
		index.set(index.get() + 1);
		return result;
	}
}
