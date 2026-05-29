package snownee.snow;

import java.util.List;
import java.util.function.BiPredicate;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.loader.Platform;
import snownee.snow.block.SRMSnowLayerBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.convert.BlockConverter;
import snownee.snow.mixin.BlockBehaviourAccess;
import snownee.snow.network.SSnowLandEffectPacket;
import snownee.snow.util.CommonProxy;

public final class Hooks {
	private static final MapCodec<Identifier> BLOCK_ENTITY_ID = Identifier.CODEC.fieldOf("id");

	private Hooks() {
	}

	public static void placeFeatureExtra(Biome biome, WorldGenLevel level, BlockPos pos, BlockPos belowPos) {
		if (!SnowCommonConfig.replaceWorldFeature || !SnowCommonConfig.placeSnowOnBlockNaturally ||
				!SnowCommonConfig.canPlaceSnowInBlock()) {
			return;
		}
		if (biome.warmEnoughToRain(pos, level.getSeaLevel()) || level.getBrightness(LightLayer.BLOCK, pos) >= 10 || !Hooks.canSnowSurvive(
				level,
				pos)) {
			return;
		}
		BlockState blockstate = level.getBlockState(pos);
		if (convert(level, pos, blockstate, 1, Block.UPDATE_CLIENTS, true)) {
//			SnowRealMagic.LOGGER.info("Place {} @ {}", level.getBlockState(pos).getBlock(), pos);
			blockstate = level.getBlockState(belowPos);
			if (blockstate.hasProperty(BlockStateProperties.SNOWY)) {
				level.setBlock(belowPos, blockstate.setValue(BlockStateProperties.SNOWY, true), Block.UPDATE_CLIENTS);
			}
		}
	}

	public static boolean canSnowSurvive(LevelReader level, BlockPos pos) {
		return Blocks.SNOW.defaultBlockState().canSurvive(level, pos);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean canContainState(BlockState blockState) {
		return CoreModule.CONVERTERS.of(blockState) != null;
	}

	public static boolean convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers, int flags, boolean canConvert) {
		return convert(level, pos, blockState, layers, flags, canConvert, true);
	}

	public static boolean convert(
			LevelAccessor level,
			BlockPos pos,
			BlockState blockState,
			int layers,
			int flags,
			boolean canConvert,
			boolean checkSurvive) {
		BlockState newState = getSnowBlockFor(level, pos, blockState, layers, canConvert);
		if (newState == null) {
			return false;
		}
		if (checkSurvive && !newState.canSurvive(level, pos)) {
			return false;
		}
		level.setBlock(pos, newState, flags);
		SRMSnowLayerBlock.setContainedState(level, pos, blockState);
		processFancyOverlay(level, pos, ItemStack.EMPTY);
		return true;
	}

	public static void processFancyOverlay(LevelAccessor level, BlockPos pos, ItemStack itemStack) {
		if (itemStack.has(DataComponents.BLOCK_ENTITY_DATA) || !(level.getBlockEntity(pos) instanceof SnowBlockEntity be) ||
				be.options.renderOverlay) {
			return;
		}
		if (SnowCommonConfig.fancySnowOnUpperSlab && level.getBlockState(pos.below()).getBlock() instanceof SlabBlock) {
			be.options.renderOverlay = true;
			be.setChanged();
		}
	}

	@Nullable
	public static BlockState getSnowBlockFor(LevelAccessor level, BlockPos pos, BlockState blockState, int layers, boolean canConvert) {
		if (SnowCommonConfig.restoreOriginalBlocks || blockState.hasBlockEntity() || !blockState.getFluidState().isEmpty() || blockState.is(
				CoreModule.NOT_CONTAINABLES)) {
			return null;
		}
		if (!blockState.isAir() && (!canConvert || !SnowCommonConfig.canPlaceSnowInBlock())) {
			return null;
		}
		BlockConverter converter = CoreModule.CONVERTERS.of(blockState);
		if (converter == null) {
			return null;
		}
		blockState = converter.convert(level, pos, blockState, layers);
		if (blockState.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			blockState = blockState.setValue(SnowVariant.OPTIONAL_LAYERS, layers);
			BlockPos posDown = pos.below();
			BlockState stateDown = level.getBlockState(posDown);
			blockState = blockState.updateShape(level, level, pos, Direction.DOWN, posDown, stateDown, level.getRandom());
		} else if (blockState.hasProperty(SnowLayerBlock.LAYERS)) {
			blockState = blockState.setValue(SnowLayerBlock.LAYERS, layers);
		}
		return blockState;
	}

	public static <T extends Comparable<T>> boolean hasAllProperties(BlockState oldState, BlockState newState) {
		for (Property<?> property : newState.getProperties()) {
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
		for (Property.Value<?> value : oldState.getValues().toList()) {
			Property<T> property = (Property<T>) value.property();
			newState = newState.trySetValue(property, property.getValueClass().cast(value.value()));
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
			if (originLayers == 0 && !canSnowSurvive(level, pos)) {
				return false;
			}
			level.setBlockAndUpdate(pos, state.setValue(SnowVariant.OPTIONAL_LAYERS, Mth.clamp(originLayers + layers, 1, 8)));
		} else if (state.canSurvive(level, pos) && (canConvert || state.canBeReplaced(useContext))) {
			if (!convert(level, pos, state, layers, Block.UPDATE_ALL, canConvert)) {
				return false;
			}
		} else {
			return false;
		}
		BlockState newState = level.getBlockState(pos);
		Block.pushEntitiesUp(state, newState, level, pos);
		if (fallingEffect) {
			//todo: check if it's available
			new SSnowLandEffectPacket(pos, (byte) originLayers, (byte) layers).sendToAround((ServerLevel) level);
		} else if (playSound) {
			SoundType soundtype = ((BlockBehaviourAccess) Blocks.SNOW).callGetSoundType(Blocks.SNOW.defaultBlockState());
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
			if (canSnowSurvive(level, pos) && newState.canBeReplaced(useContext)) {
				placeLayersOn(level, pos, layers - (8 - originLayers), fallingEffect, useContext, playSound, canConvert);
			}
		}
		return true;
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
		if (chance != 1 && random.nextFloat() > chance) {
			return;
		}
//		level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
		Holder<Biome> biome = level.getBiome(pos);
		SnowVariant snow = (SnowVariant) state.getBlock();
		int layers = snow.srm$layers(state, level, pos);
		boolean meltByTemperature = false;
		boolean meltByBrightness = false;
		if (!SnowCommonConfig.snowNeverMelt) {
			if (layers == 8) {
				BlockPos above = pos.above();
				BlockState aboveState = level.getBlockState(above);
				boolean hasSnowLayerAbove = aboveState.getBlock() instanceof SnowVariant s && s.srm$layers(aboveState, level, above) > 0;
				if (hasSnowLayerAbove) {
					return;
				}
				meltByBrightness = CommonProxy.blockLightEnoughToMelt(level, above);
			} else {
				meltByBrightness = CommonProxy.blockLightEnoughToMelt(level, pos);
			}
			meltByTemperature = CommonProxy.shouldMeltByTemperature(level, pos, biome, layers);
		}
		boolean melt = meltByTemperature || meltByBrightness;
		if (!melt && SnowCommonConfig.accumulationWinterOnly && !CommonProxy.isWinter(level, pos, biome)) {
			return;
		}

		boolean accumulate = false;
		if (layers < SnowCommonConfig.snowAccumulationMaxLayers && !meltByBrightness && level.isRaining() && CommonProxy.coldEnoughToSnow(
				level,
				pos,
				biome) && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY()) {
			accumulate = CommonProxy.snowAccumulationNow(level);
		}

		if (accumulate) {
			if (!level.getBlockState(pos.below()).is(CoreModule.CANNOT_ACCUMULATE_ON)) {
				accumulate(
						level,
						pos,
						state,
						(w, p) -> {
							if (SnowCommonConfig.snowAccumulationMaxLayers < 9 &&
									w.getBlockState(p.below()).getBlock() instanceof SnowLayerBlock) {
								return false;
							}
							if (SnowCommonConfig.snowSpawnMaxLightLevel < 15 && w.getBrightness(LightLayer.BLOCK, p) >
									SnowCommonConfig.snowSpawnMaxLightLevel) {
								return false;
							}
							return CommonProxy.coldEnoughToSnow(w, p, w.getBiome(p));
						},
						true);
			}
		} else if (melt) {
			accumulate(level, pos, state, (w, p) -> !(w.getBlockState(p.above()).getBlock() instanceof SnowLayerBlock), false);
		}
	}

	private static void accumulate(
			ServerLevel level,
			BlockPos centerPos,
			BlockState centerState,
			BiPredicate<LevelAccessor, BlockPos> filter,
			boolean accumulate) {
		if (!SnowCommonConfig.smoothAccumulation) {
			accumulateSingle(level, centerPos, centerState, accumulate);
			return;
		}
		SnowVariant centerSnowVariant = (SnowVariant) centerState.getBlock();
		int i = centerSnowVariant.srm$layers(centerState, level, centerPos);
		MutableBlockPos pos = centerPos.mutable();
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			Direction direction = Direction.from2DDataValue(k);
			pos.setWithOffset(centerPos, direction);
			if (j % 2 == 1) {
				pos.move(direction);
			}
			if (!level.isLoaded(pos) || !filter.test(level, pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			BlockPos height = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
			if (height.getY() != pos.getY()) {
				if (height.getY() != pos.getY() + 1 || !(state.getBlock() instanceof SnowVariant)) {
					continue;
				}
			}

			if (!canSnowSurvive(level, pos)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof SnowVariant snowVariant) {
				l = snowVariant.srm$layers(state, level, pos);
				if (accumulate) {
					if (l >= snowVariant.srm$maxLayers(state, level, pos)) {
						continue;
					}
					if (level.getBlockState(pos.move(Direction.DOWN)).is(CoreModule.CANNOT_ACCUMULATE_ON)) {
						continue;
					}
					pos.move(Direction.UP);
				}
			} else if (accumulate && !SnowCommonConfig.canPlaceSnowInBlock() && !state.isAir()) {
				continue;
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				accumulateSingle(level, pos, state, accumulate);
				return;
			}
		}
		accumulateSingle(level, centerPos, centerState, accumulate);
	}

	private static void accumulateSingle(ServerLevel level, BlockPos pos, BlockState state, boolean accumulate) {
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
			SnowVariant snowVariant = (SnowVariant) state.getBlock();
			level.setBlockAndUpdate(pos, snowVariant.srm$decreaseLayer(state, level, pos, false));
		}
	}

	public static boolean isSnowySetting(BlockState blockState) {
		if (!blockState.is(CoreModule.SNOWY_SETTING)) {
			return false;
		}
		if (blockState.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			return blockState.getValue(SnowVariant.OPTIONAL_LAYERS) != 0;
		}
		return true;
	}

	public static boolean canBeReplaced(BlockState blockState, BlockPlaceContext context) {
		int i;
		boolean opt = false;
		if (blockState.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			i = blockState.getValue(SnowVariant.OPTIONAL_LAYERS);
			opt = true;
		} else if (blockState.hasProperty(SnowLayerBlock.LAYERS)) {
			i = blockState.getValue(SnowLayerBlock.LAYERS);
		} else {
			throw new IllegalStateException("Invalid block state: " + blockState);
		}
		if (i == 8) {
			return false;
		}
		if (!context.getItemInHand().is(Items.SNOW)) {
			if (!((SnowVariant) blockState.getBlock()).srm$getRaw(blockState, context.getLevel(), context.getClickedPos()).isAir()) {
				return false;
			}
			return SnowCommonConfig.snowAlwaysReplaceable || i == 1;
		}
		if (i == 0) {
			return canSnowSurvive(context.getLevel(), context.getClickedPos());
		}
		return opt || context.replacingClickedOnBlock() || context.getClickedFace() == Direction.UP;
	}

	@Nullable
	public static BlockState getStateForPlacement(Block block, BlockPlaceContext context, @Nullable BlockState originalState) {
		if (SnowCommonConfig.restoreOriginalBlocks) {
			return originalState;
		}
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (block == Blocks.SNOW) {
			BlockState state = level.getBlockState(pos);
			if (state.hasProperty(SnowLayerBlock.LAYERS)) {
				int i = state.getValue(SnowLayerBlock.LAYERS);
				return state.setValue(SnowLayerBlock.LAYERS, Math.min(8, i + 1));
			} else if (state.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
				int i = state.getValue(SnowVariant.OPTIONAL_LAYERS);
				return state.setValue(SnowVariant.OPTIONAL_LAYERS, Math.min(8, i + 1));
			}
			if (SnowCommonConfig.fancySnowOnUpperSlab) {
				BlockState stateBelow = level.getBlockState(pos.below());
				if (stateBelow.getBlock() instanceof SlabBlock) {
					return CoreModule.SNOW_BLOCK.defaultBlockState();
				}
			}
		}
		var blockEntityData = context.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
		if (originalState != null && blockEntityData != null) {
			Identifier id = blockEntityData.copyTagWithoutId().read(BLOCK_ENTITY_ID).orElse(null);
			if (originalState.canSurvive(level, pos) && (CoreModule.TILE.key().equals(id) || CoreModule.TEXTURE_TILE.key().equals(id))) {
				return getSnowBlockFor(level, pos, originalState.trySetValue(BlockStateProperties.WATERLOGGED, false), 1, true);
			}
		}
		return originalState;
	}

	public static boolean canPlaceAt(Level level, BlockPos pos) {
		BlockState snowBlock = getSnowBlockFor(level, pos, level.getBlockState(pos), 1, true);
		return snowBlock != null && snowBlock.canSurvive(level, pos);
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
		if (!level.isClientSide()) {
			int i = blockState.getValue(SnowLayerBlock.LAYERS);
			boolean hasOverlay = false;
			if (i != 0 && level.getBlockEntity(pos) instanceof SnowBlockEntity be) {
				hasOverlay = be.options.renderOverlay;
			}
			level.setBlock(pos, state2, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
			block.setPlacedBy(level, pos, state2, player, context.getItemInHand());
			if (Hooks.placeLayersOn(level, pos, i, false, context, true, true)) {
				if (!player.isCreative()) {
					context.getItemInHand().shrink(1);
				}
				if (hasOverlay && level.getBlockEntity(pos) instanceof SnowBlockEntity be) {
					be.options.renderOverlay = true;
				}
			}
		}
		return true;
	}

	public static boolean isFallable(BlockState blockState) {
		return SnowCommonConfig.snowGravity && (blockState.is(Blocks.SNOW) || blockState.is(CoreModule.SNOW_TAG));
	}

	public static void restoreOriginalBlocks(LevelChunk chunk) {
		Level level = chunk.getLevel();
		if (!SnowCommonConfig.restoreOriginalBlocks || level.isClientSide() || level.getServer() == null ||
				chunk.getBlockEntities().isEmpty()) {
			return;
		}
		List<BlockEntity> blockEntities = Lists.newArrayList();
		for (BlockEntity be : chunk.getBlockEntities().values()) {
			BlockState blockState = be.getBlockState();
			if (blockState.getBlock() instanceof SnowVariant) {
				blockEntities.add(be);
			}
		}
		if (blockEntities.isEmpty()) {
			return;
		}
		level.getServer().execute(new TickTask(
				0, () -> {
			for (BlockEntity be : blockEntities) {
				Level level1 = be.getLevel();
				if (be.isRemoved() || level1 == null) {
					continue;
				}
				BlockState blockState = be.getBlockState();
				BlockPos pos = be.getBlockPos();
				BlockState raw = ((SnowVariant) blockState.getBlock()).srm$getRaw(blockState, level1, pos);
				if (raw.isAir()) {
					raw = ((SnowVariant) blockState.getBlock()).srm$getSnowState(blockState, level1, pos);
				}
				level1.setBlock(pos, raw, Block.UPDATE_NONE | Block.UPDATE_KNOWN_SHAPE);
				BlockPos below = pos.below();
				BlockState belowState = level1.getBlockState(below);
				if (belowState.hasProperty(BlockStateProperties.SNOWY) && belowState.getValue(BlockStateProperties.SNOWY)) {
					level1.setBlock(
							below,
							belowState.setValue(BlockStateProperties.SNOWY, false),
							Block.UPDATE_NONE | Block.UPDATE_KNOWN_SHAPE);
				}
			}
		}));
	}

	public static void logError(Throwable e, String template, Object... args) {
		if (SnowCommonConfig.logBlockError || Platform.isProduction()) {
			SnowRealMagic.LOGGER.error(template.formatted(args), e);
		}
	}
}
