package snownee.snow.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "land_effect", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SSnowLandEffectPacket extends PacketHandler {

	private static RandomSource RANDOM = RandomSource.create();
	public static SSnowLandEffectPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		BlockPos pos = buf.readBlockPos();
		byte originLayers = buf.readByte();
		byte layers = buf.readByte();
		return executor.apply(() -> {
			ClientLevel level = Minecraft.getInstance().level;
			double offsetY = originLayers / 8D;
			int times = layers * 10;
			for (int i = 0; i < times; ++i) {
				double d0 = RANDOM.nextGaussian() * 0.1D;
				double d1 = RANDOM.nextGaussian() * 0.02D;
				double d2 = RANDOM.nextGaussian() * 0.1D;
				level.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + RANDOM.nextFloat(), pos.getY() + offsetY, pos.getZ() + RANDOM.nextFloat(), d0, d1, d2);
			}
			level.playLocalSound(pos, SoundType.SNOW.getPlaceSound(), SoundSource.BLOCKS, (SoundType.SNOW.getVolume() + 1) / 2F, SoundType.SNOW.getPitch() * 0.8F, false);
		});
	}

	public static void send(Level level, BlockPos pos, int originLayers, int layers) {
		if (!(level instanceof ServerLevel serverLevel)) return;
		I.send(PlayerLookup.around(serverLevel, pos, 16), buf -> {
			buf.writeBlockPos(pos);
			buf.writeByte(originLayers);
			buf.writeByte(layers);
		});
	}

}
