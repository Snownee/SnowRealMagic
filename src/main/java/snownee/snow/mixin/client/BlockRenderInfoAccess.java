package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.client.renderer.RenderType;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BlockRenderInfo.class)
public interface BlockRenderInfoAccess {
	@Accessor(remap = false)
	void setDefaultLayer(RenderType defaultLayer);
}
