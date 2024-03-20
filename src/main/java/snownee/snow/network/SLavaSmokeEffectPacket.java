package snownee.snow.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import snownee.kiwi.network.KPacketTarget;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.KiwiPacket.Direction;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "lava_smoke", dir = Direction.PLAY_TO_CLIENT)
public class SLavaSmokeEffectPacket extends PacketHandler {

	private static final RandomSource RANDOM = RandomSource.create();
	public static SLavaSmokeEffectPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor,
			FriendlyByteBuf buf,
			ServerPlayer sender) {
		BlockPos pos = buf.readBlockPos();
		return executor.apply(() -> {
			ClientLevel level = Minecraft.getInstance().level;
			for (int i = 0; i < 10; ++i) {
				double d0 = RANDOM.nextGaussian() * 0.02D;
				double d1 = RANDOM.nextGaussian() * 0.02D;
				double d2 = RANDOM.nextGaussian() * 0.02D;
				level.addParticle(
						ParticleTypes.SMOKE,
						pos.getX() + RANDOM.nextFloat(),
						pos.getY(),
						pos.getZ() + RANDOM.nextFloat(),
						d0,
						d1,
						d2);
			}
			level.playLocalSound(pos, SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.8F, 0.8F, false);
		});
	}

	public static void send(ServerLevel serverLevel, BlockPos pos) {
		I.send(KPacketTarget.around(serverLevel, pos, 16), buf -> {
			buf.writeBlockPos(pos);
		});
	}

}
