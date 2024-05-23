package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadableSnowyDirtBlockMixin {

	/**
	 * Use {@link Redirect} for fail fast if there is conflicting
	 */
	@Redirect(
			method = "randomTick",
			at = @At(
					value = "INVOKE",
					ordinal = 1,
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
	private boolean srm_useSrmSnow(final BlockState instance, final Block block) {
		return instance.is(CoreModule.SNOWY_SETTING);
	}

	@Inject(
			method = "canBeGrass",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true)
	private static void srm_checkSnowFirst(
			final BlockState blockState,
			final LevelReader levelReader,
			final BlockPos blockPos,
			final CallbackInfoReturnable<Boolean> cir,
			BlockPos abovePos,
			BlockState aboveBlock) {
		if (aboveBlock.is(CoreModule.SNOWY_SETTING)) {
			if (aboveBlock.getBlock() instanceof SnowVariant snowVariant) {
				cir.setReturnValue(
						SnowCommonConfig.sustainGrassIfLayerMoreThanOne || snowVariant.srm$layers(aboveBlock, levelReader, abovePos) <= 1);
			}
			cir.setReturnValue(true);
		}
	}
}
