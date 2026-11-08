package snownee.snow;

import java.util.stream.LongStream;

import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMaps;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Immutable per-chunk snow accumulation data. The key is {@link net.minecraft.core.BlockPos#asLong()}, the value is a
 * snow strength from 0 to {@link #MAX_STATE}. Missing entries mean no snow.
 */
public final class SnowAccumulationData {
	public static final int MAX_STATE = 8;
	public static final SnowAccumulationData EMPTY = new SnowAccumulationData(new Long2ByteOpenHashMap());
	public static final Codec<SnowAccumulationData> CODEC = Codec.LONG_STREAM.xmap(
			SnowAccumulationData::fromPacked,
			SnowAccumulationData::toPacked);
	public static final StreamCodec<RegistryFriendlyByteBuf, SnowAccumulationData> STREAM_CODEC = StreamCodec.ofMember(
			SnowAccumulationData::write,
			SnowAccumulationData::read);

	private final Long2ByteMap states;

	private SnowAccumulationData(Long2ByteMap states) {
		this.states = Long2ByteMaps.unmodifiable(states);
	}

	/**
	 * Wraps {@code states} in an unmodifiable view without copying it. The caller must hand over ownership: the map
	 * must never be modified after this call.
	 */
	static SnowAccumulationData wrap(Long2ByteMap states) {
		return new SnowAccumulationData(states);
	}

	public int get(long pos) {
		return states.get(pos);
	}

	public boolean isEmpty() {
		return states.isEmpty();
	}

	public Long2ByteMap states() {
		return states;
	}

	private static SnowAccumulationData fromPacked(LongStream stream) {
		long[] packed = stream.toArray();
		Long2ByteOpenHashMap states = new Long2ByteOpenHashMap(packed.length / 2);
		for (int i = 0; i + 1 < packed.length; i += 2) {
			int state = (int) packed[i + 1];
			if (state > 0) {
				states.put(packed[i], (byte) Math.min(state, MAX_STATE));
			}
		}
		return states.isEmpty() ? EMPTY : wrap(states);
	}

	private LongStream toPacked() {
		LongStream.Builder builder = LongStream.builder();
		for (Long2ByteMap.Entry entry : states.long2ByteEntrySet()) {
			builder.add(entry.getLongKey());
			builder.add(entry.getByteValue());
		}
		return builder.build();
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(states.size());
		for (Long2ByteMap.Entry entry : states.long2ByteEntrySet()) {
			buf.writeLong(entry.getLongKey());
			buf.writeByte(entry.getByteValue());
		}
	}

	private static SnowAccumulationData read(RegistryFriendlyByteBuf buf) {
		int size = buf.readVarInt();
		Long2ByteOpenHashMap states = new Long2ByteOpenHashMap(size);
		for (int i = 0; i < size; i++) {
			long pos = buf.readLong();
			int state = buf.readByte();
			if (state > 0) {
				states.put(pos, (byte) Math.min(state, MAX_STATE));
			}
		}
		return states.isEmpty() ? EMPTY : wrap(states);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SnowAccumulationData other && states.equals(other.states);
	}

	@Override
	public int hashCode() {
		return states.hashCode();
	}
}
