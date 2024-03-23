package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;

@Mixin(SnowyDirtBlock.class)
public class SnowyDirtBlockMixin {

	@WrapOperation(
			method = "isSnowySetting",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
	private static boolean srm_getStateForPlacementProxy(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original) {
		return original.call(instance, CoreModule.SNOWY_SETTING);
	}

}
