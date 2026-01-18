package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BlockRenderInfo.class)
public interface BlockRenderInfoAccess {
	@Accessor
	void setDefaultLayer(ChunkSectionLayer defaultLayer);
}
