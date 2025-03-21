package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import snownee.snow.WorldTickHandler;
import snownee.snow.util.CommonProxy;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@SuppressWarnings("UnreachableCode")
	@WrapMethod(method = "tickPrecipitation")
	private void srm_tickPrecipitation(BlockPos pos, Operation<Void> original) {
		ServerLevel level = (ServerLevel) (Object) this;
		if (!CommonProxy.weatherTick(level, () -> WorldTickHandler.tick(level, pos))) {
			original.call(pos);
		}
	}
}
