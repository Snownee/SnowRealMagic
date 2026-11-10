package snownee.snow.client.model;

import java.util.Arrays;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.client.SnowyLeaves;

public class SnowyLeavesModel extends WrapperBlockStateModel {

	private final TextureAtlasSprite base;
	private final TextureAtlasSprite[] tops;
	private final TextureAtlasSprite[] sides;
	private final Material.Baked[] topMaterials;

	public SnowyLeavesModel(
			BlockStateModel wrapped,
			TextureAtlasSprite base,
			TextureAtlasSprite[] tops,
			TextureAtlasSprite[] sides) {
		super(wrapped);
		this.base = base;
		this.tops = tops;
		this.sides = sides;
		this.topMaterials = new Material.Baked[tops.length];
		for (int i = 0; i < tops.length; i++) {
			this.topMaterials[i] = new Material.Baked(tops[i], false);
		}
	}

	@Override
	public void emitQuads(
			QuadEmitter emitter,
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState state,
			RandomSource random,
			Predicate<@Nullable Direction> cullTest) {
		int snowLevel = SnowyLeaves.snowLevel(level, pos, state);
		TextureAtlasSprite top = SnowyLeaves.intensityForLevel(snowLevel) > 0 ? tops[snowLevel] : null;
		TextureAtlasSprite side = top == null ? null : sides[snowLevel];
		super.emitQuads(new SnowyLeavesEmitter(emitter, base, top, side), level, pos, state, random, cullTest);
	}

	public TextureAtlasSprite particleSprite(int snowLevel) {
		return tops[Mth.clamp(snowLevel, 0, tops.length - 1)];
	}

	@Override
	public Material.Baked particleMaterial() {
		return topMaterials[0];
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return topMaterials[SnowyLeaves.snowLevel(level, pos, state)];
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SnowyLeavesModel other)) {
			return false;
		}
		return wrapped == other.wrapped && base == other.base &&
				Arrays.equals(tops, other.tops) && Arrays.equals(sides, other.sides);
	}

	@Override
	public int hashCode() {
		int result = System.identityHashCode(wrapped);
		result = 31 * result + System.identityHashCode(base);
		result = 31 * result + Arrays.hashCode(tops);
		result = 31 * result + Arrays.hashCode(sides);
		return result;
	}

	private static final class SnowyLeavesEmitter extends SRMQuadEmitter {

		private final TextureAtlasSprite base;
		private final @Nullable TextureAtlasSprite top;
		private final @Nullable TextureAtlasSprite side;

		private SnowyLeavesEmitter(
				QuadEmitter wrapped,
				TextureAtlasSprite base,
				@Nullable TextureAtlasSprite top,
				@Nullable TextureAtlasSprite side) {
			super(wrapped);
			this.base = base;
			this.top = top;
			this.side = side;
		}

		@Override
		public QuadEmitter fromBakedQuad(BakedQuad quad) {
			if (quad.materialInfo().sprite() == base) {
				Direction direction = quad.direction();
				TextureAtlasSprite target = direction == Direction.UP ? top : direction == Direction.DOWN ? null : side;
				if (target != null) {
					return super.fromBakedQuad(remap(quad, base, target));
				}
			}
			return super.fromBakedQuad(quad);
		}
	}

	private static BakedQuad remap(BakedQuad quad, TextureAtlasSprite from, TextureAtlasSprite to) {
		long uv0 = remapUv(quad.packedUV0(), from, to);
		long uv1 = remapUv(quad.packedUV1(), from, to);
		long uv2 = remapUv(quad.packedUV2(), from, to);
		long uv3 = remapUv(quad.packedUV3(), from, to);
		BakedQuad.MaterialInfo old = quad.materialInfo();
		BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(
				to,
				old.layer(),
				old.itemRenderType(),
				old.itemGlintRenderType(),
				old.itemGlintSpecialRenderType(),
				-1,
				old.shadeDirectionOverride(),
				old.lightEmission());
		return new BakedQuad(
				quad.position0(),
				quad.position1(),
				quad.position2(),
				quad.position3(),
				uv0,
				uv1,
				uv2,
				uv3,
				quad.direction(),
				materialInfo);
	}

	private static long remapUv(long packed, TextureAtlasSprite from, TextureAtlasSprite to) {
		float u = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
		float v = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
		return UVPair.pack(to.getU(u), to.getV(v));
	}
}
