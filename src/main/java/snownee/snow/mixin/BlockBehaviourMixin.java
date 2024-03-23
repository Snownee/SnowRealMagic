package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.WaterLoggableSnowVariant;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
	private void srm_getDestroyProgress(
			BlockState blockState,
			Player player,
			BlockGetter level,
			BlockPos pos,
			CallbackInfoReturnable<Float> cir) {
		if (blockState.getBlock() instanceof WaterLoggableSnowVariant snowVariant) {
			cir.setReturnValue(snowVariant.getRaw(blockState, level, pos).getDestroyProgress(player, level, pos));
		}
	}
}
