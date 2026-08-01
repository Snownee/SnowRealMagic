package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayerGameMode;
import snownee.snow.block.WaterLoggableSnowVariant;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@WrapOperation(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;canHarvestBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"))
	private boolean srm_destroyBlock(
			BlockState blockState,
			BlockGetter level,
			BlockPos pos,
			Player player,
			Operation<Boolean> original) {
		if (blockState.getBlock() instanceof WaterLoggableSnowVariant snowVariant) {
			blockState = snowVariant.srm$getRaw(blockState, level, pos);
		}
		return original.call(blockState, level, pos, player);
	}
}
