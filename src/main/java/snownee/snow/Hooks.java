package snownee.snow;

import java.util.Map;
import java.util.function.BiPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.lighting.LightEngine;
import snownee.kiwi.KiwiGO;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.network.SSnowLandEffectPacket;

public final class Hooks {
	private Hooks() {
	}

	public static boolean canGrassSurvive(BlockState blockState, LevelReader viewableWorld, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = viewableWorld.getBlockState(blockPos2);
		if (blockState2.is(CoreModule.BOTTOM_SNOW)) {
			if (blockState2.is(Blocks.SNOW)) {
				return SnowCommonConfig.sustainGrassIfLayerMoreThanOne || blockState2.getValue(SnowLayerBlock.LAYERS) == 1;
			}
			return true;
		} else {
			int i = LightEngine.getLightBlockInto(viewableWorld, blockState, blockPos, blockState2, blockPos2, Direction.UP, blockState2.getLightBlock(viewableWorld, blockPos2));
			return i < viewableWorld.getMaxLightLevel();
		}
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
				Biome biome = worldgenlevel.getBiome(pos).value();
				if (biome.shouldFreeze(worldgenlevel, belowPos, false)) {
					worldgenlevel.setBlock(belowPos, Blocks.ICE.defaultBlockState(), 2);
				}

				if (biome.shouldSnow(worldgenlevel, pos)) {
					worldgenlevel.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
					BlockState blockstate = worldgenlevel.getBlockState(belowPos);
					if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
						worldgenlevel.setBlock(belowPos, blockstate.setValue(SnowyDirtBlock.SNOWY, true), 2);
					}
				} else if (SnowCommonConfig.replaceWorldFeature && SnowCommonConfig.placeSnowOnBlockNaturally && SnowCommonConfig.canPlaceSnowInBlock()) {
					if (biome.warmEnoughToRain(pos) || worldgenlevel.getBrightness(LightLayer.BLOCK, pos) >= 10 || !Blocks.SNOW.defaultBlockState().canSurvive(worldgenlevel, pos)) {
						continue;
					}
					BlockState blockstate = worldgenlevel.getBlockState(pos);
					if (convert(worldgenlevel, pos, blockstate, 1, 2, true)) {
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

	public static boolean canSnowSurvive(BlockState state, BlockGetter worldIn, BlockPos pos) {
		BlockState blockstate = worldIn.getBlockState(pos.below());
		if (blockstate.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
			return false;
		} else if (blockstate.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
			return true;
		} else {
			return Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP) || blockstate.getBlock() instanceof SnowLayerBlock && blockstate.getValue(SnowLayerBlock.LAYERS) == 8;
		}
	}

	public static boolean canContainState(BlockState state) {
		if (!SnowCommonConfig.canPlaceSnowInBlock() || state.hasBlockEntity() || !state.getFluidState().isEmpty()) {
			return false;
		}
		Block block = state.getBlock();
		if (state.is(CoreModule.NOT_CONTAINABLES)) {
			return false;
		}
		if (state.is(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			return true;
		}
		if (block instanceof FenceBlock && state.is(BlockTags.FENCES)) {
			return hasAllProperties(state, CoreModule.FENCE.defaultBlockState());
		}
		if (block instanceof FenceGateBlock && state.is(BlockTags.FENCE_GATES)) {
			return hasAllProperties(state, CoreModule.FENCE_GATE.defaultBlockState());
		}
		if (block instanceof WallBlock && state.is(BlockTags.WALLS)) {
			return hasAllProperties(state, CoreModule.WALL.defaultBlockState());
		}
		if (block instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && state.is(BlockTags.SLABS)) {
			return true;
		}
		if (block instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM && state.is(BlockTags.STAIRS)) {
			return hasAllProperties(state, CoreModule.STAIRS.defaultBlockState());
		}
		return false;
	}

	public static boolean convert(LevelAccessor world, BlockPos pos, BlockState state, int layers, int flags, boolean canConvert) {
		if (state.isAir()) {
			world.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers), flags);
			return true;
		}
		if (!SnowCommonConfig.canPlaceSnowInBlock() || state.hasBlockEntity()) {
			return false;
		}
		if (!canConvert) {
			return false;
		}
		Block block = state.getBlock();
		if (state.is(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			world.setBlock(pos, CoreModule.TILE_BLOCK.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers), flags);
			BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof SnowBlockEntity) {
				((SnowBlockEntity) tile).setState(state);
			}
			return true;
		}

		BlockPos posDown = pos.below();
		BlockState stateDown = world.getBlockState(posDown);
		if (block instanceof StairBlock && !CoreModule.STAIRS.is(state) && state.is(BlockTags.STAIRS)) {
			BlockState newState = CoreModule.STAIRS.defaultBlockState();
			newState = copyProperties(state, newState);
			world.setBlock(pos, newState, flags);
		} else if (block instanceof SlabBlock && !CoreModule.SLAB.is(state) && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && state.is(BlockTags.SLABS)) {
			// can't copy properties as this doesn't extend vanilla slabs
			world.setBlock(pos, CoreModule.SLAB.defaultBlockState(), flags);
		} else if (block instanceof FenceBlock && block.getClass() != SnowFenceBlock.class && state.is(BlockTags.FENCES)) {
			KiwiGO<Block> newBlock = state.is(BlockTags.WOODEN_FENCES) ? CoreModule.FENCE : CoreModule.FENCE2;
			BlockState newState = newBlock.defaultBlockState();
			newState = copyProperties(state, newState);
			newState = newState.updateShape(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlock(pos, newState, flags);
		} else if (block instanceof FenceGateBlock && !CoreModule.FENCE_GATE.is(state) && state.is(BlockTags.FENCE_GATES)) {
			BlockState newState = CoreModule.FENCE_GATE.defaultBlockState();
			newState = copyProperties(state, newState);
			newState = newState.updateShape(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlock(pos, newState, flags);
		} else if (block instanceof WallBlock && !CoreModule.WALL.is(state) && state.is(BlockTags.WALLS)) {
			BlockState newState = CoreModule.WALL.defaultBlockState();
			newState = copyProperties(state, newState);
			newState = newState.updateShape(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlock(pos, newState, flags);
		} else {
			return false;
		}

		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof SnowBlockEntity) {
			((SnowBlockEntity) tile).setState(state);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> boolean hasAllProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : newState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (!oldState.hasProperty(property))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> BlockState copyProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (!newState.hasProperty(property))
				continue;
			newState = newState.setValue(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	public static boolean placeLayersOn(Level world, BlockPos pos, int layers, boolean fallingEffect, BlockPlaceContext useContext, boolean playSound, boolean canConvert) {
		layers = Mth.clamp(layers, 1, 8);
		BlockState state = world.getBlockState(pos);
		int originLayers = 0;
		if (state.getBlock() instanceof SnowLayerBlock) {
			originLayers = state.getValue(SnowLayerBlock.LAYERS);
			world.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, Mth.clamp(originLayers + layers, 1, 8)));
		} else if (canConvert && canContainState(state) && state.canSurvive(world, pos)) {
			convert(world, pos, state, Mth.clamp(layers, 1, 8), 3, canConvert);
		} else if (canSnowSurvive(state, world, pos) && world.getBlockState(pos).canBeReplaced(useContext)) {
			world.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Mth.clamp(layers, 1, 8)));
		} else {
			return false;
		}
		Block.pushEntitiesUp(state, world.getBlockState(pos), world, pos);
		if (fallingEffect) {
			//todo: check if it's available
			SSnowLandEffectPacket.send(world, pos, originLayers, layers);
		} else if (playSound) {
			SoundType soundtype = Blocks.SNOW.getSoundType(Blocks.SNOW.defaultBlockState());
			world.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		}
		if (originLayers + layers > 8) {
			pos = pos.above();
			if (canSnowSurvive(Blocks.SNOW.defaultBlockState(), world, pos) && world.getBlockState(pos).canBeReplaced(useContext)) {
				placeLayersOn(world, pos, layers - (8 - originLayers), fallingEffect, useContext, playSound, canConvert);
			}
		}
		return true;
	}

	public static boolean canFallThrough(BlockState state, Level worldIn, BlockPos pos) {
		if (state.getCollisionShape(worldIn, pos).isEmpty()) {
			if (FallingBlock.isFree(state) && state.getCollisionShape(worldIn, pos).isEmpty()) {
				return true;
			}
		}
		if (state.getBlock() instanceof SnowLayerBlock && state.getValue(SnowLayerBlock.LAYERS) < 8) {
			return true;
		}
		return false;
	}

	public static void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (ModUtil.terraforged) {
			return;
		}
		if (random.nextInt(8) > 0) {
			return;
		}
		Holder<Biome> biome = worldIn.getBiome(pos);
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		boolean meltByTemperature = false;
		boolean meltByBrightness = false;
		if (!SnowCommonConfig.snowNeverMelt) {
			if (layers == 8) {
				BlockState upState = worldIn.getBlockState(pos.above());
				if (upState.getBlock() instanceof SnowLayerBlock) {
					return;
				}
			}
			meltByTemperature = ModUtil.shouldMelt(worldIn, pos, biome, layers);
			meltByBrightness = worldIn.getBrightness(LightLayer.BLOCK, pos) > 11;
		}
		boolean melt = meltByTemperature || meltByBrightness;
		if (!melt) {
			if (!SnowCommonConfig.snowAccumulationDuringSnowfall && !SnowCommonConfig.snowAccumulationDuringSnowstorm) {
				return;
			}
			if (SnowCommonConfig.accumulationWinterOnly && !ModUtil.isWinter(worldIn, pos, biome)) {
				return;
			}
		}
		if (!meltByBrightness) {
			BlockPos height = worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
			if (height.getY() > pos.getY()) {
				return;
			}
		}

		boolean accumulate = false;
		if (!meltByBrightness && worldIn.isRaining() && ModUtil.coldEnoughToSnow(worldIn, pos, biome)) {
			if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
				accumulate = true;
			} else if (SnowCommonConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering()) {
				accumulate = true;
			}
		}

		if (accumulate) {
			if (layers < SnowCommonConfig.snowAccumulationMaxLayers) {
				accumulate(worldIn, pos, state, (w, p) -> (SnowCommonConfig.snowAccumulationMaxLayers > 8 || !(w.getBlockState(p.below()).getBlock() instanceof SnowLayerBlock)) && w.getBrightness(LightLayer.BLOCK, p) <= 10, true);
			}
		} else if (melt) {
			if (layers == 1) {
				SnowVariant snow = (SnowVariant) state.getBlock();
				worldIn.setBlockAndUpdate(pos, snow.getRaw(state, worldIn, pos));
			} else {
				accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.above()).getBlock() instanceof SnowLayerBlock), false);
			}
		}
	}

	private static void accumulate(Level world, BlockPos pos, BlockState centerState, BiPredicate<LevelAccessor, BlockPos> filter, boolean accumulate) {
		int i = centerState.getValue(SnowLayerBlock.LAYERS);
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			Direction direction = Direction.from2DDataValue(k);
			BlockPos pos2 = pos.relative(direction);
			if (j % 2 == 1) {
				pos2 = pos2.relative(Direction.from2DDataValue(k + 1));
			}
			if (!world.isLoaded(pos2) || !filter.test(world, pos2)) {
				continue;
			}
			BlockState state = world.getBlockState(pos2);
			BlockPos height = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos2);
			if (height.getY() != pos2.getY()) {
				continue;
			}

			if (!canSnowSurvive(state, world, pos2)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof SnowLayerBlock) {
				l = state.getValue(SnowLayerBlock.LAYERS);
				if (accumulate && state.is(CoreModule.CANNOT_ACCUMULATE_ON)) {
					continue;
				}
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				if (accumulate) {
					placeLayersOn(world, pos2, 1, false, new DirectionalPlaceContext(world, pos2, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false, SnowCommonConfig.placeSnowOnBlockNaturally);
				} else {
					world.setBlockAndUpdate(pos2, state.setValue(SnowLayerBlock.LAYERS, l - 1));
				}
				return;
			}
		}
		if (accumulate) {
			placeLayersOn(world, pos, 1, false, new DirectionalPlaceContext(world, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false, SnowCommonConfig.placeSnowOnBlockNaturally);
		} else {
			world.setBlockAndUpdate(pos, centerState.setValue(SnowLayerBlock.LAYERS, i - 1));
		}
	}
}
