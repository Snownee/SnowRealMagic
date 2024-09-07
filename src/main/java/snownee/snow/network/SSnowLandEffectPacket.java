package snownee.snow.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SoundType;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.snow.SnowRealMagic;

@KiwiPacket
public record SSnowLandEffectPacket(BlockPos pos, byte originLayers, byte layers) implements CustomPacketPayload {
	public static final Type<SSnowLandEffectPacket> TYPE = new CustomPacketPayload.Type<>(SnowRealMagic.id("land_effect"));

	private static final RandomSource RANDOM = RandomSource.create();

	public void sendToAround(ServerLevel level) {
		KPacketSender.sendToAround(this, level, null, pos, 16);
	}

	@Override
	public Type<SSnowLandEffectPacket> type() {
		return TYPE;
	}


	public static class Handler implements PlayPacketHandler<SSnowLandEffectPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SSnowLandEffectPacket> STREAM_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC,
				SSnowLandEffectPacket::pos,
				ByteBufCodecs.BYTE,
				SSnowLandEffectPacket::originLayers,
				ByteBufCodecs.BYTE,
				SSnowLandEffectPacket::layers,
				SSnowLandEffectPacket::new
		);

		@Override
		public void handle(SSnowLandEffectPacket packet, PayloadContext payloadContext) {
			payloadContext.execute(() -> {
				ClientLevel level = Minecraft.getInstance().level;
				double offsetY = packet.originLayers / 8D;
				int times = packet.layers * 10;
				for (int i = 0; i < times; ++i) {
					double d0 = RANDOM.nextGaussian() * 0.1D;
					double d1 = RANDOM.nextGaussian() * 0.02D;
					double d2 = RANDOM.nextGaussian() * 0.1D;
					level.addParticle(
							ParticleTypes.SNOWFLAKE,
							packet.pos.getX() + RANDOM.nextFloat(),
							packet.pos.getY() + offsetY,
							packet.pos.getZ() + RANDOM.nextFloat(),
							d0,
							d1,
							d2);
				}
				level.playLocalSound(
						packet.pos,
						SoundType.SNOW.getPlaceSound(),
						SoundSource.BLOCKS,
						(SoundType.SNOW.getVolume() + 1) / 2F,
						SoundType.SNOW.getPitch() * 0.8F,
						false);
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SSnowLandEffectPacket> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
