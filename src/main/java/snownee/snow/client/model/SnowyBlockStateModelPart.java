package snownee.snow.client.model;

import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

public class SnowyBlockStateModelPart implements BlockStateModelPart {
	private final BlockStateModelPart normal;
	private final BlockStateModelPart snowy;

	public SnowyBlockStateModelPart(BlockStateModelPart normal, BlockStateModelPart snowy) {
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
	public Material.Baked particleMaterial() {
		return normal.particleMaterial();
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags() {
		return normal.materialFlags();
	}

	@Override
	public void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
		if (emitter instanceof SRMQuadEmitter) {
			snowy.emitQuads(emitter, cullTest);
		} else {
			normal.emitQuads(emitter, cullTest);
		}
	}
}
