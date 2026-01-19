package snownee.snow.client.model;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.RenderData;
import snownee.snow.client.SnowClientConfig;

public class SnowVariantModel extends WrapperBlockStateModel {

	public SnowVariantModel(BlockStateModel model) {
		super(model);
	}

	@Override
	public void emitQuads(
			QuadEmitter emitter,
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState state,
			RandomSource random,
			Predicate<@Nullable Direction> cullTest) {
		if (SnowClientConfig.snowVariants) {
			if (((FabricBlockGetter) level).getBlockEntityRenderData(pos) instanceof RenderData) {
				emitter = new SnowyQuadEmitter(emitter);
			} else if (state.hasProperty(DoublePlantBlock.HALF) &&
					CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(level.getBlockState(pos.below()))) {
				emitter = new SnowyQuadEmitter(emitter);
			}
		}
		super.emitQuads(emitter, level, pos, state, random, cullTest);
	}
}
