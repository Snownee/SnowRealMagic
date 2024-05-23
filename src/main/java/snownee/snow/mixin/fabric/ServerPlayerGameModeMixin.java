package snownee.snow.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.WaterLoggableSnowVariant;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@WrapOperation(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean srm_destroyBlock(
			ServerPlayer player,
			BlockState blockState,
			Operation<Boolean> original,
			@Local(argsOnly = true) BlockPos pos) {
		if (blockState.getBlock() instanceof WaterLoggableSnowVariant snowVariant) {
			blockState = snowVariant.srm$getRaw(blockState, player.level(), pos);
		}
		return original.call(player, blockState);
	}
}
