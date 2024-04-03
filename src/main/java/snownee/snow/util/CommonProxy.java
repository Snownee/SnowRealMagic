package snownee.snow.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.material.FluidState;
import snownee.kiwi.Mod;
import snownee.kiwi.loader.Platform;
import snownee.snow.GameEvents;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;

@Mod(SnowRealMagic.MODID)
public class CommonProxy implements ModInitializer {
	public static boolean terraforged;
	public static boolean fabricSeasons = Platform.isModLoaded("seasons");

	public static boolean isHot(FluidState fluidState, Level level, BlockPos pos) {
		return fluidState.getType().getPickupSound().orElse(null) == SoundEvents.BUCKET_FILL_LAVA || fluidState.is(FluidTags.LAVA);
	}

	public static Packet<ClientGamePacketListener> getAddEntityPacket(Entity entity) {
		return new ClientboundAddEntityPacket(entity);
	}

	public static void weatherTick(ServerLevel level, Runnable action) {
		if (level.random.nextInt(SnowCommonConfig.weatherTickSlowness) == 0) {
			action.run();
		}
	}

	public static boolean snowAccumulationNow(Level level) {
		if (!level.isRaining()) {
			return false;
		}
		if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
			return true;
		}
		// there is no thundering in winter in Serene Seasons
		if (SnowCommonConfig.snowAccumulationDuringSnowstorm && level.isThundering()) {
			return true;
		}
		return false;
	}

	public static boolean shouldMelt(Level level, BlockPos pos) {
		return shouldMelt(level, pos, level.getBiome(pos), 1);
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome, int layers) {
		if (SnowCommonConfig.snowNeverMelt) {
			return false;
		}
		if (snowAndIceMeltInWarmBiomes(level.dimension(), biome) && biome.value().warmEnoughToRain(pos) && skyLightEnoughToMelt(
				level,
				pos,
				layers)) {
			return true;
		}
		if (layers <= 1) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9) {
				return false;
			}
			if (!(level.getBlockState(pos.below()).getBlock() instanceof SnowLayerBlock)) {
				return false;
			}
		}
		return SnowCommonConfig.snowNaturalMelt && skyLightEnoughToMelt(level, pos, layers);
	}

	public static boolean snowAndIceMeltInWarmBiomes(ResourceKey<Level> dimension, Holder<Biome> biome) {
		return fabricSeasons || SnowCommonConfig.snowAndIceMeltInWarmBiomes;
	}

	public static boolean skyLightEnoughToMelt(Level level, BlockPos pos, int layers) {
		return level.getBrightness(LightLayer.SKY, layers == 8 ? pos.above() : pos) > 2;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		return false;
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (SnowCommonConfig.debugSpawningCommand) {
				DebugMobSpawningCommand.register(dispatcher);
			}
		});
		UseBlockCallback.EVENT.register(GameEvents::onItemUse);
		PlayerBlockBreakEvents.BEFORE.register(GameEvents::onDestroyedByPlayer);
	}
}
