package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractBlockRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractBlockRenderContext.class)
public interface AbstractBlockRenderContextAccess {
	@Accessor
	BlockRenderInfo getBlockInfo();
}
