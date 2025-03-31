package snownee.snow.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.level.block.SoundType;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public final class WrappedSoundType extends SoundType {

	private WrappedSoundType(SoundType type) {
		//noinspection deprecation
		super(
				type.getVolume(),
				type.getPitch(),
				type.getBreakSound(),
				SoundType.SNOW.getStepSound(),
				SoundType.SNOW.getFallSound(),
				SoundType.SNOW.getPlaceSound(),
				type.getHitSound());
	}

	private static final Map<SoundType, SoundType> wrappedSounds = Maps.newConcurrentMap();

	public static SoundType get(SoundType soundType) {
		if (soundType == SoundType.SNOW || soundType instanceof WrappedSoundType) {
			return soundType;
		}
		return wrappedSounds.computeIfAbsent(soundType, WrappedSoundType::new);
	}
}
