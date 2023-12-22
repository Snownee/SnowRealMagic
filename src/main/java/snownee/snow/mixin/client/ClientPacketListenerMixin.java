package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import snownee.snow.GameEvents;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(at = @At("TAIL"), method = "handleLogin")
	private void srm_handleLogin(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
		GameEvents.onPlayerJoin();
	}

}
