package snownee.snow;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.lighting.LayerLightEngine;
import snownee.kiwi.KiwiGO;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.network.SSnowLandEffectPacket;

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
				} else if (SnowCommonConfig.replaceWorldFeature && SnowCommonConfig.canPlaceSnowInBlock()) {
					if (biome.warmEnoughToRain(pos) || worldgenlevel.getBrightness(LightLayer.BLOCK, pos) >= 10 || !Blocks.SNOW.defaultBlockState().canSurvive(worldgenlevel, pos)) {
						continue;
					}
					BlockState blockstate = worldgenlevel.getBlockState(pos);
					if (convert(worldgenlevel, pos, blockstate, 1, 2)) {
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

	public static boolean canSurvive(BlockState state, BlockGetter worldIn, BlockPos pos, boolean ignoreSelf) {
		BlockState blockstate = worldIn.getBlockState(pos.below());
		Block block = blockstate.getBlock();
		if (block instanceof SnowLayerBlock && blockstate.getValue(SnowLayerBlock.LAYERS) == 8) {
			return true;
		} else if ((SnowCommonConfig.snowOnIce && (blockstate.is(Blocks.ICE) || blockstate.is(Blocks.PACKED_ICE))) || !blockstate.is(CoreModule.INVALID_SUPPORTERS)) {
			if (ignoreSelf || state.getMaterial().isReplaceable() || canContainState(state)) {
				if (blockstate.is(BlockTags.LEAVES) || Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP)) {
					return true;
				}
			}
		}
		return false;
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

	public static boolean convert(LevelAccessor world, BlockPos pos, BlockState state, int layers, int flags) {
		if (!SnowCommonConfig.canPlaceSnowInBlock() || state.hasBlockEntity()) {
			return false;
		}
		Block block = state.getBlock();
		if (state.isAir()) {
			world.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers), flags);
			return true;
		}
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
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (!newState.hasProperty(property))
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

	@SuppressWarnings("deprecation")
	public static boolean placeLayersOn(Level world, BlockPos pos, int layers, boolean fallingEffect, BlockPlaceContext useContext, boolean playSound) {
		layers = Mth.clamp(layers, 1, 8);
		BlockState state = world.getBlockState(pos);
		int originLayers = 0;
		if (state.getBlock() instanceof SnowLayerBlock) {
			originLayers = state.getValue(SnowLayerBlock.LAYERS);
			world.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, Mth.clamp(originLayers + layers, 1, 8)));
		} else if (Hooks.canContainState(state) && state.canSurvive(world, pos)) {
			Hooks.convert(world, pos, state, Mth.clamp(layers, 1, 8), 3);
		} else if (Blocks.SNOW.canSurvive(state, world, pos)) {
			world.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Mth.clamp(layers, 1, 8)));
		} else {
			return false;
		}
		if (fallingEffect) {
			SSnowLandEffectPacket.send(world, pos, originLayers, layers);
		} else if (playSound) {
			SoundType soundtype = Blocks.SNOW.getSoundType(Blocks.SNOW.defaultBlockState());
			world.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		}
		if (originLayers + layers > 8) {
			pos = pos.above();
			if (Blocks.SNOW.canSurvive(Blocks.SNOW.defaultBlockState(), world, pos) && world.getBlockState(pos).canBeReplaced(useContext)) {
				placeLayersOn(world, pos, layers - (8 - originLayers), fallingEffect, useContext, playSound);
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

}
