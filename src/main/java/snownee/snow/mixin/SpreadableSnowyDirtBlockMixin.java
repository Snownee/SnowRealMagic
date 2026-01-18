package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadableSnowyDirtBlockMixin {

	@WrapOperation(
			method = "randomTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
	private boolean srm_isSnowySetting(BlockState blockState, Object block, Operation<Boolean> original) {
		return Hooks.isSnowySetting(blockState);
	}

	@Inject(
			method = "canBeGrass",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"),
			cancellable = true)
	private static void srm_checkSnowFirst(
			final BlockState blockState,
			final LevelReader levelReader,
			final BlockPos blockPos,
			final CallbackInfoReturnable<Boolean> cir,
			@Local(ordinal = 1) BlockPos abovePos,
			@Local(ordinal = 1) BlockState aboveBlock) {
		if (aboveBlock.is(CoreModule.SNOWY_SETTING)) {
			if (aboveBlock.getBlock() instanceof SnowVariant snowVariant) {
				cir.setReturnValue(
						SnowCommonConfig.sustainGrassIfLayerMoreThanOne || snowVariant.srm$layers(aboveBlock, levelReader, abovePos) <= 1);
			}
			cir.setReturnValue(true);
		}
	}
}
