package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.client.SnowyLeaves;

@Mixin(TerrainParticle.class)
public class TerrainParticleMixin {

	@Shadow
	@Final
	@Mutable
	private SingleQuadParticle.Layer layer;

	@Inject(
			method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
			at = @At("RETURN"))
	private void srm_useSnowyLeafSprite(
			ClientLevel level,
			double x,
			double y,
			double z,
			double xa,
			double ya,
			double za,
			BlockState state,
			BlockPos pos,
			CallbackInfo ci) {
		TextureAtlasSprite sprite = SnowyLeaves.particleSprite(pos, state);
		if (sprite == null) {
			return;
		}
		SingleQuadParticle particle = (SingleQuadParticle) (Object) this;
		particle.sprite = sprite;
		particle.rCol = 0.6F;
		particle.gCol = 0.6F;
		particle.bCol = 0.6F;
		this.layer = SingleQuadParticle.Layer.bySprite(sprite);
	}
}
