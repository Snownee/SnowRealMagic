package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.SnowVariant;
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
			cir.setReturnValue(snowVariant.srm$getRaw(blockState, level, pos).getDestroyProgress(player, level, pos));
		}
	}

	@Inject(method = "getCloneItemStack", at = @At("HEAD"), cancellable = true)
	private void srm_getCloneItemStack(
			LevelReader level,
			BlockPos pos,
			BlockState blockState,
			boolean includeData,
			CallbackInfoReturnable<ItemStack> cir) {
		if (blockState.getBlock() instanceof SnowVariant snowVariant) {
			BlockState raw = snowVariant.srm$getRaw(blockState, level, pos);
			if (raw.isAir()) {
				cir.setReturnValue(Items.SNOW.getDefaultInstance());
			}
			cir.setReturnValue(raw.getCloneItemStack(level, pos, false));
		}
	}
}
