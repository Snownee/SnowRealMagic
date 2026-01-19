package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	private Level level;

	@WrapOperation(
			method = "getFluidInCamera", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
	private boolean srm_getFluidInCamera(
			BlockState blockState,
			Object powderSnow,
			Operation<Boolean> original,
			@Local(name = "checkPos") BlockPos checkPos,
			@Local(name = "offsetPos") Vec3 offsetPos) {
		boolean originalValue = original.call(blockState, powderSnow);
		if (originalValue || !SnowCommonConfig.thinnerBoundingBox) {
			return originalValue;
		}
		if (!blockState.is(CoreModule.SNOWY_SETTING) || !(blockState.getBlock() instanceof SnowVariant snowVariant) ||
				snowVariant.srm$layers(blockState, level, checkPos) < 2) {
			return false;
		}
		return snowVariant.srm$getSnowState(blockState, level, checkPos)
				.getOcclusionShape()
				.bounds()
				.contains(offsetPos.subtract(checkPos.getX(), checkPos.getY(), checkPos.getZ()));
	}
}
