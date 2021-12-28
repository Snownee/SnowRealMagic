package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.lighting.LayerLightEngine;
import snownee.snow.block.ModSnowLayerBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowCoveredBlockEntity;

public final class Hooks {
	private Hooks() {
	}

	public static boolean canSurvive(BlockState blockState, LevelReader viewableWorld, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = viewableWorld.getBlockState(blockPos2);
		if (blockState2.is(CoreModule.BOTTOM_SNOW)) {
			if (blockState2.getBlock() == Blocks.SNOW) {
				return SnowCommonConfig.sustainGrassIfLayerMoreThanOne || blockState2.getValue(SnowLayerBlock.LAYERS) == 1;
			}
			return true;
		} else {
			int i = LayerLightEngine.getLightBlockInto(viewableWorld, blockState, blockPos, blockState2, blockPos2, Direction.UP, blockState2.getLightBlock(viewableWorld, blockPos2));
			return i < viewableWorld.getMaxLightLevel();
		}
	}

	public static InteractionResult shouldRenderFaceSnow(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockPos relativePos) {
		if (!state.canOcclude()) {
			return InteractionResult.PASS;
		}
		pos = pos.relative(direction);
		state = level.getBlockState(pos);
		if (!state.hasBlockEntity() || !(state.getBlock() instanceof SnowVariant)) {
			return InteractionResult.PASS;
		}
		if (direction == Direction.UP) {
			return InteractionResult.CONSUME;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof SnowCoveredBlockEntity) {
			if (!((SnowCoveredBlockEntity) blockEntity).getState().canOcclude())
				return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel worldgenlevel = ctx.level();
		BlockPos blockpos = ctx.origin();
		MutableBlockPos pos = new MutableBlockPos();
		MutableBlockPos belowPos = new MutableBlockPos();

		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				int k = blockpos.getX() + i;
				int l = blockpos.getZ() + j;
				int i1 = worldgenlevel.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
				pos.set(k, i1, l);
				belowPos.set(pos).move(Direction.DOWN, 1);
				Biome biome = worldgenlevel.getBiome(pos);
				if (biome.shouldFreeze(worldgenlevel, belowPos, false)) {
					worldgenlevel.setBlock(belowPos, Blocks.ICE.defaultBlockState(), 2);
				}

				if (biome.shouldSnow(worldgenlevel, pos)) {
					worldgenlevel.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
					BlockState blockstate = worldgenlevel.getBlockState(belowPos);
					if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
						worldgenlevel.setBlock(belowPos, blockstate.setValue(SnowyDirtBlock.SNOWY, true), 2);
					}
				} else if (!SnowCommonConfig.retainOriginalBlocks && SnowCommonConfig.replaceWorldFeature) {
					if (biome.warmEnoughToRain(pos) || worldgenlevel.getBrightness(LightLayer.BLOCK, pos) >= 10 || !CoreModule.BLOCK.defaultBlockState().canSurvive(worldgenlevel, pos)) {
						continue;
					}
					BlockState blockstate = worldgenlevel.getBlockState(pos);
					if (ModSnowLayerBlock.convert(worldgenlevel, pos, blockstate, 1, 2)) {
						blockstate = worldgenlevel.getBlockState(belowPos);
						if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
							worldgenlevel.setBlock(belowPos, blockstate.setValue(SnowyDirtBlock.SNOWY, true), 2);
						}
					}
				}
			}
		}

		return true;
	}

}
