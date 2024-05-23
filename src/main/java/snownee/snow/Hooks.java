package snownee.snow;

import java.util.Map;
import java.util.function.BiPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.KiwiGO;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.network.SSnowLandEffectPacket;
import snownee.snow.util.CommonProxy;

public final class Hooks {
	private Hooks() {
	}

	public static void placeFeatureExtra(Biome biome, WorldGenLevel level, BlockPos pos, BlockPos belowPos) {
		if (SnowCommonConfig.replaceWorldFeature && SnowCommonConfig.placeSnowOnBlockNaturally &&
				SnowCommonConfig.canPlaceSnowInBlock()) {
			if (biome.warmEnoughToRain(pos) || level.getBrightness(LightLayer.BLOCK, pos) >= 10 ||
					!Blocks.SNOW.defaultBlockState().canSurvive(level, pos)) {
				return;
			}
			BlockState blockstate = level.getBlockState(pos);
			if (convert(level, pos, blockstate, 1, 2, true)) {
				blockstate = level.getBlockState(belowPos);
				if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
					level.setBlock(belowPos, blockstate.setValue(SnowyDirtBlock.SNOWY, true), 2);
				}
			}
		}
	}

	public static boolean canSnowSurvive(BlockState state, BlockGetter level, BlockPos pos) {
		pos = pos.below();
		BlockState blockstate = level.getBlockState(pos);
		if (blockstate.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
			return false;
		} else if (blockstate.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
			return true;
		} else {
			return Block.isFaceFull(blockstate.getCollisionShape(level, pos), Direction.UP);
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
		if (state.is(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock ||
				block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock ||
				block instanceof SweetBerryBushBlock) {
			return true;
		}
		return switch (block) {
			case FenceBlock ignored -> hasAllProperties(state, CoreModule.FENCE.defaultBlockState());
			case FenceGateBlock ignored -> hasAllProperties(state, CoreModule.FENCE_GATE.defaultBlockState());
			case WallBlock ignored -> hasAllProperties(state, CoreModule.WALL.defaultBlockState());
			case SlabBlock ignored when state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM -> true;
			case StairBlock ignored when state.getValue(StairBlock.HALF) == Half.BOTTOM -> hasAllProperties(
					state,
					CoreModule.STAIRS.defaultBlockState());
			default -> false;
		};
	}

	public static boolean convert(LevelAccessor level, BlockPos pos, BlockState state, int layers, int flags, boolean canConvert) {
		if (state.isAir()) {
			placeNormalSnow(level, pos, layers, flags);
			return true;
		}
		if (!SnowCommonConfig.canPlaceSnowInBlock() || state.hasBlockEntity()) {
			return false;
		}
		if (!canConvert) {
			return false;
		}
		Block block = state.getBlock();
		if (state.is(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock ||
				block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock ||
				block instanceof SweetBerryBushBlock) {
			var shape = state.getCollisionShape(level, pos);

			BlockState result;

			if (state.is(CoreModule.PLANTS)) {
				if (state.hasProperty(DoublePlantBlock.HALF)) {
					if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
						result = CoreModule.SNOWY_DOUBLE_PLANT_LOWER.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
					} else {
						result = CoreModule.SNOWY_DOUBLE_PLANT_UPPER.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
					}
				} else {
					result = CoreModule.SNOWY_PLANT.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
				}
			} else if (shape.isEmpty()) {
				result = CoreModule.SNOW_BLOCK.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
			} else {
				result = CoreModule.SNOW_EXTRA_COLLISION_BLOCK.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
			}

			level.setBlock(pos, result, flags);
			if (level.getBlockEntity(pos) instanceof SnowBlockEntity snowBlockEntity) {
				snowBlockEntity.setContainedState(state);
			}
			return true;
		}

		BlockPos posDown = pos.below();
		BlockState stateDown = level.getBlockState(posDown);
		switch (block) {
			case StairBlock ignored when !CoreModule.STAIRS.is(state) -> {
				BlockState newState = CoreModule.STAIRS.defaultBlockState();
				newState = copyProperties(state, newState);
				level.setBlock(pos, newState, flags);
			}
			case SlabBlock ignored when !CoreModule.SLAB.is(state) && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM ->
				// can't copy properties as this doesn't extend vanilla slabs
					level.setBlock(pos, CoreModule.SLAB.defaultBlockState(), flags);
			case FenceBlock ignored when block.getClass() != SnowFenceBlock.class -> {
				KiwiGO<Block> newBlock =
						state.is(BlockTags.WOODEN_FENCES) || state.getSoundType() == SoundType.WOOD ? CoreModule.FENCE : CoreModule.FENCE2;
				BlockState newState = newBlock.defaultBlockState();
				newState = copyProperties(state, newState).setValue(SnowVariant.OPTIONAL_LAYERS, layers);
				newState = newState.updateShape(Direction.DOWN, stateDown, level, pos, posDown);
				level.setBlock(pos, newState, flags);
			}
			case FenceGateBlock ignored when !CoreModule.FENCE_GATE.is(state) -> {
				BlockState newState = CoreModule.FENCE_GATE.defaultBlockState();
				newState = copyProperties(state, newState).setValue(SnowVariant.OPTIONAL_LAYERS, layers);
				newState = newState.updateShape(Direction.DOWN, stateDown, level, pos, posDown);
				level.setBlock(pos, newState, flags);
			}
			case WallBlock ignored when !CoreModule.WALL.is(state) -> {
				BlockState newState = CoreModule.WALL.defaultBlockState();
				newState = copyProperties(state, newState).setValue(SnowVariant.OPTIONAL_LAYERS, layers);
				newState = newState.updateShape(Direction.DOWN, stateDown, level, pos, posDown);
				level.setBlock(pos, newState, flags);
			}
			default -> {
				return false;
			}
		}

		BlockEntity tile = level.getBlockEntity(pos);
		if (tile instanceof SnowBlockEntity) {
			((SnowBlockEntity) tile).setContainedState(state);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> boolean hasAllProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : newState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (property == SnowVariant.OPTIONAL_LAYERS) {
				continue;
			}
			if (!oldState.hasProperty(property)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> BlockState copyProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (!newState.hasProperty(property)) {
				continue;
			}
			newState = newState.setValue(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	public static boolean placeLayersOn(
			Level level,
			BlockPos pos,
			int layers,
			boolean fallingEffect,
			BlockPlaceContext useContext,
			boolean playSound,
			boolean canConvert) {
		layers = Mth.clamp(layers, 1, 8);
		BlockState state = level.getBlockState(pos);
		int originLayers = 0;
		if (state.hasProperty(SnowLayerBlock.LAYERS)) {
			originLayers = state.getValue(SnowLayerBlock.LAYERS);
			level.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, Mth.clamp(originLayers + layers, 1, 8)));
		} else if (state.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			originLayers = state.getValue(SnowVariant.OPTIONAL_LAYERS);
			if (originLayers == 0 && !canSnowSurvive(state, level, pos)) {
				return false;
			}
			level.setBlockAndUpdate(pos, state.setValue(SnowVariant.OPTIONAL_LAYERS, Mth.clamp(originLayers + layers, 1, 8)));
		} else if (canConvert && canContainState(state) && state.canSurvive(level, pos)) {
			convert(level, pos, state, Mth.clamp(layers, 1, 8), 3, canConvert);
		} else if (canSnowSurvive(state, level, pos) && state.canBeReplaced(useContext)) {
			placeNormalSnow(level, pos, layers, 3);
		} else {
			return false;
		}
		BlockState newState = level.getBlockState(pos);
		Block.pushEntitiesUp(state, newState, level, pos);
		if (fallingEffect) {
			//todo: check if it's available
			new SSnowLandEffectPacket(pos, (byte) originLayers, (byte) layers).sendToAround((ServerLevel) level);
		} else if (playSound) {
			SoundType soundtype = Blocks.SNOW.getSoundType(Blocks.SNOW.defaultBlockState());
			level.playSound(
					null,
					pos,
					soundtype.getPlaceSound(),
					SoundSource.BLOCKS,
					(soundtype.getVolume() + 1) / 2F,
					soundtype.getPitch() * 0.8F);
		}
		if (originLayers + layers > 8) {
			pos = pos.above();
			newState = level.getBlockState(pos);
			useContext = BlockPlaceContext.at(useContext, pos, Direction.UP);
			if (canSnowSurvive(Blocks.SNOW.defaultBlockState(), level, pos) && newState.canBeReplaced(useContext)) {
				placeLayersOn(level, pos, layers - (8 - originLayers), fallingEffect, useContext, playSound, canConvert);
			}
		}
		return true;
	}

	public static void placeNormalSnow(LevelAccessor level, BlockPos pos, int layers, int flags) {
		BlockState stateBelow = level.getBlockState(pos.below());
		Block block = SnowCommonConfig.fancySnowOnUpperSlab && stateBelow.getBlock() instanceof SlabBlock ?
				CoreModule.SNOW_EXTRA_COLLISION_BLOCK.get() :
				Blocks.SNOW;
		stateBelow = block.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Mth.clamp(layers, 1, 8));
		level.setBlock(pos, stateBelow, flags);
		if (stateBelow.is(CoreModule.SNOW_TAG)) {
			setPlacedBy(level, pos, stateBelow);
		}
	}

	public static void setPlacedBy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level.getBlockEntity(pos) instanceof SnowBlockEntity blockEntity && blockEntity.getContainedState().isAir()) {
			blockEntity.options.update(true);
		}
	}

	public static boolean canFallThrough(BlockState state, Level level, BlockPos pos) {
		if (state.getBlock() instanceof SnowVariant snow) {
			if (snow.srm$maxLayers(state, level, pos) == 8 && snow.srm$layers(state, level, pos) < 8) {
				return true;
			}
		}
		return FallingBlock.isFree(state) && state.getCollisionShape(level, pos).isEmpty();
	}

	public static void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		randomTick(state, level, pos, random, 0.125f);
	}

	public static void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, float chance) {
		if (CommonProxy.terraforged) {
			return;
		}
		if (chance != 1 && random.nextFloat() > chance) {
			return;
		}
		Holder<Biome> biome = level.getBiome(pos);
		SnowVariant snow = (SnowVariant) state.getBlock();
		int layers = snow.srm$layers(state, level, pos);
		boolean meltByTemperature = false;
		boolean meltByBrightness = false;
		if (!SnowCommonConfig.snowNeverMelt) {
			if (layers == 8) {
				BlockPos above = pos.above();
				BlockState upState = level.getBlockState(above);
				if (upState.getBlock() instanceof SnowVariant s && s.srm$layers(upState, level, above) > 0) {
					return;
				}
				meltByBrightness = level.getBrightness(LightLayer.BLOCK, above) > 10;
			} else {
				meltByBrightness = level.getBrightness(LightLayer.BLOCK, pos) > 11;
			}
			meltByTemperature = CommonProxy.shouldMelt(level, pos, biome, layers);
		}
		boolean melt = meltByTemperature || meltByBrightness;
		if (!melt && SnowCommonConfig.accumulationWinterOnly && !CommonProxy.isWinter(level, pos, biome)) {
			return;
		}

		boolean accumulate = false;
		if (layers < SnowCommonConfig.snowAccumulationMaxLayers && !meltByBrightness && level.isRaining() && CommonProxy.coldEnoughToSnow(
				level,
				pos,
				biome)) {
			accumulate = CommonProxy.snowAccumulationNow(level);
		}

		if (accumulate) {
			if (!level.getBlockState(pos.below()).is(CoreModule.CANNOT_ACCUMULATE_ON)) {
				accumulate(
						level,
						pos,
						state,
						(w, p) -> (
								SnowCommonConfig.snowAccumulationMaxLayers > 8 ||
										!(w.getBlockState(p.below()).getBlock() instanceof SnowLayerBlock)) &&
								w.getBrightness(LightLayer.BLOCK, p) <= 10,
						true);
			}
		} else if (melt) {
			accumulate(level, pos, state, (w, p) -> !(w.getBlockState(p.above()).getBlock() instanceof SnowLayerBlock), false);
		}
	}

	private static void accumulate(
			ServerLevel level,
			BlockPos pos,
			BlockState centerState,
			BiPredicate<LevelAccessor, BlockPos> filter,
			boolean accumulate) {
		SnowVariant centerSnowVariant = (SnowVariant) centerState.getBlock();
		int i = centerSnowVariant.srm$layers(centerState, level, pos);
		MutableBlockPos pos2 = pos.mutable();
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			Direction direction = Direction.from2DDataValue(k);
			pos2.setWithOffset(pos, direction);
			if (j % 2 == 1) {
				pos2.move(direction);
			}
			if (!level.isLoaded(pos2) || !filter.test(level, pos2)) {
				continue;
			}
			BlockState state = level.getBlockState(pos2);
			BlockPos height = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos2);
			if (height.getY() != pos2.getY()) {
				if (height.getY() != pos2.getY() + 1 || !(state.getBlock() instanceof SnowVariant)) {
					continue;
				}
			}

			if (!canSnowSurvive(state, level, pos2)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof SnowVariant snowVariant) {
				l = snowVariant.srm$layers(state, level, pos2);
				if (accumulate) {
					if (l >= snowVariant.srm$maxLayers(state, level, pos2)) {
						continue;
					}
					if (level.getBlockState(pos2.move(Direction.DOWN)).is(CoreModule.CANNOT_ACCUMULATE_ON)) {
						continue;
					}
					pos2.move(Direction.UP);
				}
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				if (accumulate) {
					placeLayersOn(
							level,
							pos2,
							1,
							false,
							new DirectionalPlaceContext(level, pos2, Direction.UP, ItemStack.EMPTY, Direction.DOWN),
							false,
							SnowCommonConfig.placeSnowOnBlockNaturally);
				} else {
					SnowVariant snowVariant = (SnowVariant) state.getBlock();
					level.setBlockAndUpdate(pos2, snowVariant.srm$decreaseLayer(state, level, pos2, false));
				}
				return;
			}
		}
		if (accumulate) {
			placeLayersOn(
					level,
					pos,
					1,
					false,
					new DirectionalPlaceContext(level, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN),
					false,
					SnowCommonConfig.placeSnowOnBlockNaturally);
		} else {
			level.setBlockAndUpdate(pos, centerSnowVariant.srm$decreaseLayer(centerState, level, pos, false));
		}
	}

	public static boolean isSnowySetting(BlockState state) {
		if (state.is(CoreModule.SNOWY_SETTING)) {
			return !state.hasProperty(SnowVariant.OPTIONAL_LAYERS) || state.getValue(SnowVariant.OPTIONAL_LAYERS) != 0;
		}
		return false;
	}

	public static boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (!context.getItemInHand().is(Items.SNOW)) {
			return false;
		}
		int i = state.getValue(SnowVariant.OPTIONAL_LAYERS);
		if (i == 8) {
			return false;
		}
		return i > 0 || canSnowSurvive(state, context.getLevel(), context.getClickedPos());
	}

	public static BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (state.hasProperty(SnowLayerBlock.LAYERS)) {
			int i = state.getValue(SnowLayerBlock.LAYERS);
			return state.setValue(SnowLayerBlock.LAYERS, Math.min(8, i + 1));
		} else if (state.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			int i = state.getValue(SnowVariant.OPTIONAL_LAYERS);
			return state.setValue(SnowVariant.OPTIONAL_LAYERS, Math.min(8, i + 1));
		}
		ItemStack stack = context.getItemInHand();
		var blockEntityData = stack.getComponentsPatch().get(DataComponents.BLOCK_ENTITY_DATA);
		if (blockEntityData != null && blockEntityData.isPresent()
				&& "snowrealmagic:snow".equals(blockEntityData.get().getUnsafe().getString("id"))) {
			return CoreModule.SNOW_EXTRA_COLLISION_BLOCK.defaultBlockState();
		}
		if (SnowCommonConfig.fancySnowOnUpperSlab) {
			BlockState stateBelow = context.getLevel().getBlockState(context.getClickedPos().below());
			if (stateBelow.getBlock() instanceof SlabBlock) {
				return CoreModule.SNOW_EXTRA_COLLISION_BLOCK.defaultBlockState();
			}
		}
		return Blocks.SNOW.defaultBlockState();
	}

	public static boolean canPlaceAt(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (canContainState(state)) {
			Block block = state.getBlock();
			if (block instanceof StairBlock || block instanceof SlabBlock || block instanceof FenceBlock ||
					block instanceof FenceGateBlock || block instanceof WallBlock) {
				return true;
			}
			return canSnowSurvive(state, level, pos);
		}
		return false;
	}

	public static boolean useSnowWithEmptyHand(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
			return false;
		}
		var stateBelow = level.getBlockState(pos.below());
		if (stateBelow.getBlock() instanceof SnowLayerBlock || stateBelow.hasProperty(BlockStateProperties.SNOWY)) {
			return false;
		}
		if (blockState.is(Blocks.SNOW)) {
			level.setBlock(pos, Hooks.copyProperties(blockState, CoreModule.SNOW_EXTRA_COLLISION_BLOCK.defaultBlockState()), 16 | 32);
		}
		var blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof SnowBlockEntity snowTile)) {
			return false;
		}
		if (blockState.is(CoreModule.SNOW_TAG) && snowTile.getContainedState().isAir()) {
			level.setBlock(pos, Hooks.copyProperties(blockState, Blocks.SNOW.defaultBlockState()), 16 | 32);
		} else {
			snowTile.options.renderOverlay = !snowTile.options.renderOverlay;
			if (level.isClientSide) {
				level.sendBlockUpdated(pos, blockState, blockState, 11);
			}
		}
		return true;
	}

	public static boolean useSnowWithItem(
			ItemStack itemStack,
			BlockState blockState,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand interactionHand, BlockHitResult hitResult,
			SnowVariant snowVariant) {
		Block block = Block.byItem(itemStack.getItem());
		if (block == Blocks.AIR || !snowVariant.srm$getRaw(blockState, level, pos).isAir()) {
			return false;
		}
		BlockPlaceContext context = new BlockPlaceContext(player, interactionHand, itemStack, hitResult);
		if (!context.replacingClickedOnBlock()) {
			return false;
		}
		BlockState state2 = block.getStateForPlacement(context);
		if (state2 == null || !Hooks.canContainState(state2) || !state2.canSurvive(level, pos)) {
			return false;
		}
		if (!level.isClientSide) {
			level.setBlock(pos, state2, 16 | 32);
			block.setPlacedBy(level, pos, blockState, player, context.getItemInHand());
			int i = blockState.getValue(SnowLayerBlock.LAYERS);
			if (Hooks.placeLayersOn(level, pos, i, false, context, true, true) && !player.isCreative()) {
				context.getItemInHand().shrink(1);
			}
		}
		return true;
	}
}
