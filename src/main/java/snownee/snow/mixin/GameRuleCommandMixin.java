package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {

	@Inject(method = "setRule", at = @At("RETURN"))
	private static <T extends GameRules.Value<T>> void srm_setRule(CommandContext<CommandSourceStack> context, GameRules.Key<T> key, CallbackInfoReturnable<Integer> ci) {
		if (key == GameRules.RULE_SNOW_ACCUMULATION_HEIGHT) {
			context.getSource().sendFailure(Component.translatable("commands.gamerule.snowrealmagic.hint"));
		}
	}

	@Inject(method = "queryRule", at = @At("RETURN"))
	private static <T extends GameRules.Value<T>> void srm_queryRule(CommandSourceStack context, GameRules.Key<T> key, CallbackInfoReturnable<Integer> ci) {
		if (key == GameRules.RULE_SNOW_ACCUMULATION_HEIGHT) {
			context.sendFailure(Component.translatable("commands.gamerule.snowrealmagic.hint"));
		}
	}

}
