package snownee.snow.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import snownee.kiwi.Mod;
import snownee.snow.GameEvents;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;

@Mod(SnowRealMagic.MODID)
public class CommonProxy implements ModInitializer {
	public static boolean isHot(FluidState fluidState, Level level, BlockPos pos) {
		return fluidState.getType().getPickupSound().orElse(null) == SoundEvents.BUCKET_FILL_LAVA || fluidState.is(FluidTags.LAVA);
	}

	public static Packet<ClientGamePacketListener> getAddEntityPacket(Entity entity) {
		return new ClientboundAddEntityPacket(entity);
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (SnowCommonConfig.debugSpawningCommand)
				DebugMobSpawningCommand.register(dispatcher);
		});
		UseBlockCallback.EVENT.register(GameEvents::onItemUse);
		PlayerBlockBreakEvents.BEFORE.register(GameEvents::onDestroyedByPlayer);
	}
}
