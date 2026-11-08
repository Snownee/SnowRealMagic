package snownee.snow.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowCommonConfig;
import snownee.snow.WorldTickHandler;
import snownee.snow.util.CommonProxy;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements WorldTickHandler.ProbeHolder, SnowAccumulation.PendingHolder {
	@Unique
	private final List<WorldTickHandler.Probe> srm$probes = new ArrayList<>();
	@Unique
	private final Long2ObjectMap<SnowAccumulation.Pending> srm$pendingSnow = new Long2ObjectOpenHashMap<>();

	@Override
	public List<WorldTickHandler.Probe> srm$probes() {
		return srm$probes;
	}

	@Override
	public Long2ObjectMap<SnowAccumulation.Pending> srm$pendingSnow() {
		return srm$pendingSnow;
	}

	@SuppressWarnings("UnreachableCode")
	@WrapMethod(method = "tickPrecipitation")
	private void srm_tickPrecipitation(BlockPos pos, Operation<Void> original) {
		ServerLevel level = (ServerLevel) (Object) this;
		if (SnowCommonConfig.forceVanillaIceSnowLogic || !CommonProxy.weatherTick(level, () -> WorldTickHandler.tick(level, pos))) {
			original.call(pos);
		}
	}

	@Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
	private void srm_tickProbes(BooleanSupplier haveTime, CallbackInfo ci) {
		ServerLevel level = (ServerLevel) (Object) this;
		WorldTickHandler.tickProbes(level);
		SnowAccumulation.flushPending(level);
	}

	@Inject(method = "tickChunk", at = @At("HEAD"))
	private void srm_tickChunk(LevelChunk chunk, int tickSpeed, CallbackInfo ci) {
		WorldTickHandler.tickMelt((ServerLevel) (Object) this, chunk, tickSpeed);
	}
}
