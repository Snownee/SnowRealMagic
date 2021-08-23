package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.BufferBuilder;
import snownee.snow.BufferBuilderDuck;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder implements BufferBuilderDuck {
	@Shadow
	private double yOffset;

	@Override
	public void translateY(double y) {
		yOffset += y;
	}

}
