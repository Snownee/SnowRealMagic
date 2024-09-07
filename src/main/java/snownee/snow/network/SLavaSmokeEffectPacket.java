package snownee.snow.network;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.snow.SnowRealMagic;

@KiwiPacket
public record SLavaSmokeEffectPacket(BlockPos pos) implements CustomPacketPayload {
	public static final Type<SLavaSmokeEffectPacket> TYPE = new CustomPacketPayload.Type<>(SnowRealMagic.id("lava_smoke"));

	private static final RandomSource RANDOM = RandomSource.create();

	public void sendToAround(ServerLevel level) {
		KPacketSender.sendToAround(this, level, null, pos, 16);
	}


	@Override
	public @NotNull Type<SLavaSmokeEffectPacket> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<SLavaSmokeEffectPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SLavaSmokeEffectPacket> STREAM_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC,
				SLavaSmokeEffectPacket::pos,
				SLavaSmokeEffectPacket::new
		);

		@Override
		public void handle(SLavaSmokeEffectPacket packet, PayloadContext payloadContext) {
			payloadContext.execute(() -> {
				var level = Minecraft.getInstance().level;
				for (var i = 0; i < 10; ++i) {
					var d0 = RANDOM.nextGaussian() * 0.02D;
					var d1 = RANDOM.nextGaussian() * 0.02D;
					var d2 = RANDOM.nextGaussian() * 0.02D;
					level.addParticle(
							ParticleTypes.SMOKE,
							packet.pos.getX() + RANDOM.nextFloat(),
							packet.pos.getY(),
							packet.pos.getZ() + RANDOM.nextFloat(),
							d0,
							d1,
							d2);
				}
				level.playLocalSound(packet.pos, SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.8F, 0.8F, false);
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SLavaSmokeEffectPacket> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
