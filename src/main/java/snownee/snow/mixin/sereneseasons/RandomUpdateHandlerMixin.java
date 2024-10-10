package snownee.snow.mixin.sereneseasons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import glitchcore.event.TickEvent;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.season.RandomUpdateHandler;

@Mixin(value = RandomUpdateHandler.class, remap = false)
public abstract class RandomUpdateHandlerMixin {

	@Inject(at = @At("HEAD"), method = "onWorldTick", cancellable = true)
	private static void srm_onWorldTick(TickEvent.Level event, CallbackInfo ci) {
		Level level = event.getLevel();
		if (event.getPhase() == TickEvent.Phase.END && !level.isClientSide()) {
			Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
			adjustWeatherFrequency(level, subSeason);
		}
		ci.cancel();
	}

	@Shadow
	private static void adjustWeatherFrequency(Level world, Season.SubSeason subSeason) {
		throw new AssertionError();
	}

}
