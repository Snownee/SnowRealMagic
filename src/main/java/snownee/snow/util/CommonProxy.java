package snownee.snow.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;
import snownee.kiwi.loader.Platform;
import snownee.snow.GameEvents;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;
import snownee.snow.compat.sereneseasons.SereneSeasonsCompat;
import snownee.snow.datagen.SnowBlockTagsProvider;

@Mod(SnowRealMagic.MODID)
public class CommonProxy {
	public static boolean terraforged;
	public static boolean sereneseasons;

	public CommonProxy() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
			if (SnowCommonConfig.debugSpawningCommand)
				DebugMobSpawningCommand.register(event.getDispatcher());
		});
		MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			InteractionResult result = GameEvents.onItemUse(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
			if (result.consumesAction()) {
				event.setCanceled(true);
				event.setCancellationResult(result);
			}
		});
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener((GatherDataEvent event) -> {
			DataGenerator generator = event.getGenerator();
			SnowBlockTagsProvider blockTagsProvider = new SnowBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
			generator.addProvider(event.includeServer(), blockTagsProvider);
		});
		sereneseasons = Platform.isModLoaded("sereneseasons");
		if (sereneseasons) {
			SnowRealMagic.LOGGER.info("SereneSeasons detected. Overriding weather behavior.");
		}

		if (Platform.isPhysicalClient())
			ClientProxy.init();
	}

	public static boolean isHot(FluidState fluidState, Level level, BlockPos pos) {
		return fluidState.getType().getFluidType().getTemperature(fluidState, level, pos) > 380 || fluidState.is(FluidTags.LAVA);
	}

	public static Packet<ClientGamePacketListener> getAddEntityPacket(Entity entity) {
		return NetworkHooks.getEntitySpawningPacket(entity);
	}

	public static void weatherTick(ServerLevel level, Runnable action) {
		if (sereneseasons) {
			SereneSeasonsCompat.weatherTick(level, action);
			return;
		}

		if (level.random.nextFloat() < SnowCommonConfig.weatherTickSlowness) {
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
		if (SnowCommonConfig.snowAccumulationDuringSnowstorm && (level.isThundering() || sereneseasons)) {
			return true;
		}
		return false;
	}

	public static boolean shouldMelt(Level level, BlockPos pos) {
		return shouldMelt(level, pos, level.getBiome(pos), 1);
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome, int layers) {
		if (SnowCommonConfig.snowNeverMelt)
			return false;
		if (sereneseasons)
			return SereneSeasonsCompat.shouldMelt(level, pos, biome);
		if (snowAndIceMeltInWarmBiomes(level.dimension(), biome) && biome.value().warmEnoughToRain(pos) && skyLightEnoughToMelt(level, pos, layers))
			return true;
		if (layers <= 1) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9)
				return false;
			if (!(level.getBlockState(pos.below()).getBlock() instanceof SnowLayerBlock))
				return false;
		}
		return SnowCommonConfig.snowNaturalMelt && skyLightEnoughToMelt(level, pos, layers);
	}

	public static boolean snowAndIceMeltInWarmBiomes(ResourceKey<Level> dimension, Holder<Biome> biome) {
		if (sereneseasons) {
			return false; // handled by serene seasons instead
		}
		if (SnowCommonConfig.snowAndIceMeltInWarmBiomes) {
			return true;
		}
		return false;
	}

	public static boolean skyLightEnoughToMelt(Level level, BlockPos pos, int layers) {
		return level.getBrightness(LightLayer.SKY, layers == 8 ? pos.above() : pos) > 2;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneseasons) {
			return SereneSeasonsCompat.coldEnoughToSnow(level, pos, biome);
		}
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneseasons) {
			return SereneSeasonsCompat.isWinter(level, pos, biome);
		}
		return false;
	}
}
