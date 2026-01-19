package snownee.snow.client.model;

import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class SnowyBlockModelPart implements BlockModelPart {
	private final BlockModelPart normal;
	private final BlockModelPart snowy;

	public SnowyBlockModelPart(BlockModelPart normal, BlockModelPart snowy) {
		this.normal = normal;
		this.snowy = snowy;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable Direction direction) {
		return normal.getQuads(direction);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return normal.useAmbientOcclusion();
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return normal.particleIcon();
	}

	@Override
	public void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
		if (emitter instanceof SnowyQuadEmitter) {
			snowy.emitQuads(emitter, cullTest);
		} else {
			normal.emitQuads(emitter, cullTest);
		}
	}
}
