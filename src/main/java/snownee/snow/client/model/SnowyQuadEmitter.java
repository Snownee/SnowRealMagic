package snownee.snow.client.model;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class SnowyQuadEmitter implements QuadEmitter {
	private final QuadEmitter wrapped;

	public SnowyQuadEmitter(QuadEmitter wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public QuadEmitter pos(int vertexIndex, float x, float y, float z) {
		return wrapped.pos(vertexIndex, x, y, z);
	}

	@Override
	public QuadEmitter color(int vertexIndex, int color) {
		return wrapped.color(vertexIndex, color);
	}

	@Override
	public QuadEmitter uv(int vertexIndex, float u, float v) {
		return wrapped.uv(vertexIndex, u, v);
	}

	@Override
	public QuadEmitter lightmap(int vertexIndex, int lightmap) {
		return wrapped.lightmap(vertexIndex, lightmap);
	}

	@Override
	public QuadEmitter normal(int vertexIndex, float x, float y, float z) {
		return wrapped.normal(vertexIndex, x, y, z);
	}

	@Override
	public QuadEmitter nominalFace(@Nullable Direction face) {
		return wrapped.nominalFace(face);
	}

	@Override
	public QuadEmitter cullFace(@Nullable Direction face) {
		return wrapped.cullFace(face);
	}

	@Override
	public QuadEmitter chunkLayer(@Nullable ChunkSectionLayer layer) {
		return wrapped.chunkLayer(layer);
	}

	@Override
	public QuadEmitter emissive(boolean emissive) {
		return wrapped.emissive(emissive);
	}

	@Override
	public QuadEmitter diffuseShade(boolean shade) {
		return wrapped.diffuseShade(shade);
	}

	@Override
	public QuadEmitter ambientOcclusion(TriState ao) {
		return wrapped.ambientOcclusion(ao);
	}

	@Override
	public QuadEmitter foilType(ItemStackRenderState.@Nullable FoilType foilType) {
		return wrapped.foilType(foilType);
	}

	@Override
	public QuadEmitter shadeMode(ShadeMode mode) {
		return wrapped.shadeMode(mode);
	}

	@Override
	public QuadEmitter atlas(QuadAtlas quadAtlas) {
		return wrapped.atlas(quadAtlas);
	}

	@Override
	public QuadEmitter tintIndex(int tintIndex) {
		return wrapped.tintIndex(tintIndex);
	}

	@Override
	public QuadEmitter tag(int tag) {
		return wrapped.tag(tag);
	}

	@Override
	public QuadEmitter copyFrom(QuadView quad) {
		return wrapped.copyFrom(quad);
	}

	@Override
	public QuadEmitter fromBakedQuad(BakedQuad quad) {
		return wrapped.fromBakedQuad(quad);
	}

	@Override
	public void pushTransform(QuadTransform transform) {
		wrapped.pushTransform(transform);
	}

	@Override
	public void popTransform() {
		wrapped.popTransform();
	}

	@Override
	public QuadEmitter emit() {
		return wrapped.emit();
	}

	@Override
	public float x(int vertexIndex) {
		return wrapped.x(vertexIndex);
	}

	@Override
	public float y(int vertexIndex) {
		return wrapped.y(vertexIndex);
	}

	@Override
	public float z(int vertexIndex) {
		return wrapped.z(vertexIndex);
	}

	@Override
	public float posByIndex(int vertexIndex, int coordinateIndex) {
		return wrapped.posByIndex(vertexIndex, coordinateIndex);
	}

	@Override
	public Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
		return wrapped.copyPos(vertexIndex, target);
	}

	@Override
	public int color(int vertexIndex) {
		return wrapped.color(vertexIndex);
	}

	@Override
	public float u(int vertexIndex) {
		return wrapped.u(vertexIndex);
	}

	@Override
	public float v(int vertexIndex) {
		return wrapped.v(vertexIndex);
	}

	@Override
	public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
		return wrapped.copyUv(vertexIndex, target);
	}

	@Override
	public int lightmap(int vertexIndex) {
		return wrapped.lightmap(vertexIndex);
	}

	@Override
	public boolean hasNormal(int vertexIndex) {
		return wrapped.hasNormal(vertexIndex);
	}

	@Override
	public float normalX(int vertexIndex) {
		return wrapped.normalX(vertexIndex);
	}

	@Override
	public float normalY(int vertexIndex) {
		return wrapped.normalY(vertexIndex);
	}

	@Override
	public float normalZ(int vertexIndex) {
		return wrapped.normalZ(vertexIndex);
	}

	@Override
	public @Nullable Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
		return wrapped.copyNormal(vertexIndex, target);
	}

	@Override
	public Vector3fc faceNormal() {
		return wrapped.faceNormal();
	}

	@Override
	public Direction lightFace() {
		return wrapped.lightFace();
	}

	@Override
	public @Nullable Direction nominalFace() {
		return wrapped.nominalFace();
	}

	@Override
	public @Nullable Direction cullFace() {
		return wrapped.cullFace();
	}

	@Override
	public @Nullable ChunkSectionLayer chunkLayer() {
		return wrapped.chunkLayer();
	}

	@Override
	public boolean emissive() {
		return wrapped.emissive();
	}

	@Override
	public boolean diffuseShade() {
		return wrapped.diffuseShade();
	}

	@Override
	public TriState ambientOcclusion() {
		return wrapped.ambientOcclusion();
	}

	@Override
	public ItemStackRenderState.@Nullable FoilType foilType() {
		return wrapped.foilType();
	}

	@Override
	public ShadeMode shadeMode() {
		return wrapped.shadeMode();
	}

	@Override
	public QuadAtlas atlas() {
		return wrapped.atlas();
	}

	@Override
	public int tintIndex() {
		return wrapped.tintIndex();
	}

	@Override
	public int tag() {
		return wrapped.tag();
	}
}