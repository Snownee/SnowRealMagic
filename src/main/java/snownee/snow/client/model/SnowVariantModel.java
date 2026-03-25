package snownee.snow.client.model;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.RenderData;
import snownee.snow.client.SnowClientConfig;

public class SnowVariantModel extends WrapperBlockStateModel {

	public SnowVariantModel(BlockStateModel wrapped) {
		super(wrapped);
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
