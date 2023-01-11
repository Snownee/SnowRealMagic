package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.FenceGateBlock;

@Mixin(FenceGateBlock.class)
public interface FenceGateBlockAccess {

	@Accessor
	@Mutable
	void setCloseSound(SoundEvent soundEvent);

	@Accessor
	@Mutable
	void setOpenSound(SoundEvent soundEvent);

	@Accessor
	SoundEvent getCloseSound();

	@Accessor
	SoundEvent getOpenSound();

}
