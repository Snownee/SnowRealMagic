package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import snownee.snow.SnowCommonConfig;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {

	@Inject(method = "setRule", at = @At("RETURN"))
	private static <T> void srm_setRule(
			CommandContext<CommandSourceStack> context,
			GameRule<T> gameRule,
			CallbackInfoReturnable<Integer> ci) {
		if (gameRule == GameRules.MAX_SNOW_ACCUMULATION_HEIGHT && !SnowCommonConfig.forceVanillaIceSnowLogic) {
			context.getSource().sendFailure(Component.translatable("commands.gamerule.snowrealmagic.hint"));
		}
	}

	@Inject(method = "queryRule", at = @At("RETURN"))
	private static <T> void srm_queryRule(
			CommandSourceStack source,
			GameRule<T> gameRule,
			CallbackInfoReturnable<Integer> ci) {
		if (gameRule == GameRules.MAX_SNOW_ACCUMULATION_HEIGHT && !SnowCommonConfig.forceVanillaIceSnowLogic) {
			source.sendFailure(Component.translatable("commands.gamerule.snowrealmagic.hint"));
		}
	}

}
