package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadableSnowyDirtBlockMixin {

	@Inject(
			method = "canBeGrass",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"),
			cancellable = true)
	private static void srm_checkSnowFirst(
			final BlockState state,
			final LevelReader level,
			final BlockPos pos,
			final CallbackInfoReturnable<Boolean> cir,
			@Local(name = "above") BlockPos above,
			@Local(name = "aboveState") BlockState aboveState) {
		if (aboveState.is(CoreModule.SNOWY_SETTING)) {
			if (aboveState.getBlock() instanceof SnowVariant snowVariant) {
				cir.setReturnValue(
						SnowCommonConfig.sustainGrassIfLayerMoreThanOne || snowVariant.srm$layers(aboveState, level, above) <= 1);
			}
			cir.setReturnValue(true);
		}
	}
}
