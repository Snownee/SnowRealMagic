package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractTerrainRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractTerrainRenderContext.class)
public interface AbstractTerrainRenderContextAccess {
	@Accessor
	BlockRenderInfo getBlockInfo();
}
