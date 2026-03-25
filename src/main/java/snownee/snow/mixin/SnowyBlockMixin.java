package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;

@Mixin(SnowyBlock.class)
public class SnowyBlockMixin {

	@WrapMethod(method = "isSnowySetting")
	private static boolean srm_isSnowySetting(BlockState aboveState, Operation<Boolean> original) {
		return Hooks.isSnowySetting(aboveState);
	}

}
