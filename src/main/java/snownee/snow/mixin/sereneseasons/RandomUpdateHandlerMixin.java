package snownee.snow.mixin.sereneseasons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.handler.season.RandomUpdateHandler;

@Mixin(value = RandomUpdateHandler.class, remap = false)
public abstract class RandomUpdateHandlerMixin {

	@Inject(at = @At("HEAD"), method = "onWorldTick", cancellable = true)
	private void srm_onWorldTick(TickEvent.WorldTickEvent event, CallbackInfo ci) {
		if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
		      Season.SubSeason subSeason = SeasonHelper.getSeasonState(event.world).getSubSeason();
		      Season season = subSeason.getSeason();
		      adjustWeatherFrequency(event.world, season);
		}
		ci.cancel();
	}

	@Shadow
	abstract void adjustWeatherFrequency(Level world, Season season);

}
