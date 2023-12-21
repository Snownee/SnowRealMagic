package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import snownee.snow.Hooks;
import snownee.snow.block.SnowVariant;

@Mixin(value = AbstractMegaTreeGrower.class, priority = 200)
public class AbstractMegaTreeGrowerMixin {

	@Inject(method = "isTwoByTwoSapling", at = @At("HEAD"), cancellable = true)
	private static void srm_isTwoByTwoSapling(BlockState state, BlockGetter level, BlockPos origin, int xOffset, int yOffset, CallbackInfoReturnable<Boolean> ci) {
		Block block = state.getBlock();
		BlockPos.MutableBlockPos pos = origin.mutable();
		if (srm_test(level, pos.setWithOffset(origin, xOffset, 0, yOffset), block)
				&& srm_test(level, pos.setWithOffset(origin, xOffset + 1, 0, yOffset), block)
				&& srm_test(level, pos.setWithOffset(origin, xOffset, 0, yOffset + 1), block)
				&& srm_test(level, pos.setWithOffset(origin, xOffset + 1, 0, yOffset + 1), block)
		) {
			ci.setReturnValue(true);
		}
	}

	@Unique
	private static boolean srm_test(BlockGetter level, BlockPos pos, Block block) {
		BlockState state = level.getBlockState(pos);
		if (state.is(block)) {
			return true;
		}
		return state.getBlock() instanceof SnowVariant snow && snow.getRaw(state, level, pos).is(block);
	}

	@Inject(method = "placeMega", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
	private void srm_placeMega(ServerLevel level, ChunkGenerator chunkGenerator, BlockPos origin, BlockState state, RandomSource random, int offsetX, int offsetY, CallbackInfoReturnable<Boolean> ci, ResourceKey<ConfiguredFeature<?, ?>> resourcekey, Holder<ConfiguredFeature<?, ?>> holder, ConfiguredFeature<?, ?> configuredfeature) {
		BlockPos[] positions = new BlockPos[4];
		positions[0] = origin.offset(offsetX, 0, offsetY);
		positions[1] = origin.offset(offsetX + 1, 0, offsetY);
		positions[2] = origin.offset(offsetX, 0, offsetY + 1);
		positions[3] = origin.offset(offsetX + 1, 0, offsetY + 1);
		int[] layers = new int[4];
		for (int i = 0; i < 4; i++) {
			BlockPos pos = positions[i];
			BlockState state2 = level.getBlockState(pos);
			if (state2.getBlock() instanceof SnowVariant snow) {
				layers[i] = snow.layers(state2, level, pos);
			} else {
				layers[i] = 0;
			}
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
		}
		if (configuredfeature.place(level, chunkGenerator, random, positions[0])) {
			ci.setReturnValue(true);
		} else {
			for (int i = 0; i < 4; i++) {
				BlockPos pos = positions[i];
				level.setBlock(pos, state, 4);
				if (layers[i] > 0) {
					Hooks.convert(level, pos, state, layers[i], 4, true);
				}
			}
			ci.setReturnValue(false);
		}
	}
}
