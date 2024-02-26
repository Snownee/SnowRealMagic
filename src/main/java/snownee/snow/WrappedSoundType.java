package snownee.snow;

import java.util.Map;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public final class WrappedSoundType extends SoundType {

	private final SoundType type;

	@SuppressWarnings("deprecation")
	private WrappedSoundType(SoundType type) {
		super(type.getVolume(), type.getPitch(), null, null, null, null, null);
		this.type = type;
	}

	@Nonnull
	@Override
	public SoundEvent getStepSound() {
		return SoundType.SNOW.getStepSound();
	}

	@Nonnull
	@Override
	public SoundEvent getFallSound() {
		return SoundType.SNOW.getFallSound();
	}

	@Nonnull
	@Override
	public SoundEvent getBreakSound() {
		return type.getBreakSound();
	}

	@Nonnull
	@Override
	public SoundEvent getPlaceSound() {
		return SoundType.SNOW.getStepSound();
	}

	@Nonnull
	@Override
	public SoundEvent getHitSound() {
		return type.getHitSound();
	}

	private static final Map<SoundType, SoundType> wrappedSounds = Maps.newConcurrentMap();

	public static SoundType get(SoundType soundType) {
		if (soundType == SoundType.SNOW || soundType instanceof WrappedSoundType) {
			return soundType;
		}
		return wrappedSounds.computeIfAbsent(soundType, WrappedSoundType::new);
	}
}
