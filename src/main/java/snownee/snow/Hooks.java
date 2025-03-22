package snownee.snow;

import java.util.Map;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapDecoder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.KiwiGO;
import snownee.snow.block.SRMSnowLayerBlock;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.mixin.BlockBehaviourAccess;
import snownee.snow.network.SSnowLandEffectPacket;
import snownee.snow.util.CommonProxy;

public final class Hooks {
	private static final MapDecoder<ResourceLocation> BLOCK_ENTITY_ID = ResourceLocation.CODEC.fieldOf("id");

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
			if (convert(level, pos, blockstate, 1, Block.UPDATE_CLIENTS, true)) {
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

	public static boolean convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers, int flags, boolean canConvert) {
		BlockState newState = getSnowBlockFor(level, pos, blockState, layers, canConvert);
		if (newState == null) {
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
		if (blockState.isAir()) {
			BlockPos posDown = pos.below();
			BlockState stateDown = level.getBlockState(posDown);
			Block block = SnowCommonConfig.fancySnowOnUpperSlab && stateDown.getBlock() instanceof SlabBlock ?
					CoreModule.SNOW_BLOCK.get() :
					Blocks.SNOW;
			return block.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
		}
		if (!canConvert) {
			return null;
		}
		if (!SnowCommonConfig.canPlaceSnowInBlock() || blockState.hasBlockEntity()) {
			return null;
		}
		Block block = blockState.getBlock();
		if (block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock ||
				block instanceof SaplingBlock ||
				block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			KiwiGO<SRMSnowLayerBlock> newBlock;
			if (block instanceof DoublePlantBlock) {
				if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
					newBlock = CoreModule.SNOWY_DOUBLE_PLANT_LOWER;
				} else {
					newBlock = CoreModule.SNOWY_DOUBLE_PLANT_UPPER;
				}
			} else if (blockState.is(CoreModule.PLANTS)) {
				newBlock = CoreModule.SNOWY_PLANT;
			} else if (blockState.getCollisionShape(level, pos).isEmpty()) {
				newBlock = CoreModule.SNOW_BLOCK;
			} else {
				newBlock = CoreModule.SNOW_EXTRA_COLLISION_BLOCK;
			}

			return newBlock.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
		}

		BlockPos posDown = pos.below();
		BlockState stateDown = level.getBlockState(posDown);
		Block newBlock;
		switch (block) {
			case StairBlock ignored when !CoreModule.STAIRS.is(blockState) -> newBlock = CoreModule.STAIRS.get();
			case SlabBlock ignored when !CoreModule.SLAB.is(blockState) && blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM -> {
				// can't copy properties as this doesn't extend vanilla slabs
				return CoreModule.SLAB.defaultBlockState();
			}
			case FenceBlock ignored when block.getClass() != SnowFenceBlock.class ->
					newBlock = blockState.is(BlockTags.WOODEN_FENCES) || blockState.getSoundType() == SoundType.WOOD ?
							CoreModule.FENCE.get() :
							CoreModule.FENCE2.get();
			case FenceGateBlock ignored when !CoreModule.FENCE_GATE.is(blockState) -> newBlock = CoreModule.FENCE_GATE.get();
			case WallBlock ignored when !CoreModule.WALL.is(blockState) -> newBlock = CoreModule.WALL.get();
			default -> {
				return null;
			}
		}
		BlockState newState = copyProperties(blockState, newBlock.defaultBlockState()).setValue(SnowVariant.OPTIONAL_LAYERS, layers);
		if (block instanceof FenceBlock || block instanceof FenceGateBlock) {
			newState = newState.updateShape(Direction.DOWN, stateDown, level, pos, posDown);
		}
		return newState;
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
			convert(level, pos, state, layers, Block.UPDATE_ALL, true);
		} else if (canSnowSurvive(state, level, pos) && state.canBeReplaced(useContext)) {
			convert(level, pos, state, layers, Block.UPDATE_ALL, false);
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
			if (canSnowSurvive(Blocks.SNOW.defaultBlockState(), level, pos) && newState.canBeReplaced(useContext)) {
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
				meltByBrightness = level.getBrightness(LightLayer.BLOCK, above) >= SnowCommonConfig.snowPersistMaxLightLevel;
			} else {
				meltByBrightness = level.getBrightness(LightLayer.BLOCK, pos) > SnowCommonConfig.snowPersistMaxLightLevel;
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
				biome) && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() == pos.getY()) {
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
								w.getBrightness(LightLayer.BLOCK, p) <= SnowCommonConfig.snowSpawnMaxLightLevel,
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

			if (!canSnowSurvive(state, level, pos)) {
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

	public static BlockState getStateForPlacement(Block block, BlockPlaceContext context) {
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
		if (blockEntityData != null) {
			ResourceLocation id = blockEntityData.read(BLOCK_ENTITY_ID).result().orElse(null);
			if (CoreModule.TILE.key().equals(id) || CoreModule.TEXTURE_TILE.key().equals(id)) {
				//noinspection deprecation
				BlockState blockState = SnowBlockEntity.parseContainedState(blockEntityData.getUnsafe());
				if (blockState.canSurvive(level, pos)) {
					return getSnowBlockFor(level, pos, blockState, 1, true);
				}
			}
		}
		return null;
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
			level.setBlock(
					pos,
					Hooks.copyProperties(blockState, CoreModule.SNOW_BLOCK.defaultBlockState()),
					Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
		}
		var blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof SnowBlockEntity snowTile)) {
			return false;
		}
		if (blockState.is(CoreModule.SNOW_TAG) && snowTile.getContainedState().isAir()) {
			level.setBlock(
					pos,
					Hooks.copyProperties(blockState, Blocks.SNOW.defaultBlockState()),
					Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
		} else {
			snowTile.options.renderOverlay = !snowTile.options.renderOverlay;
			if (level.isClientSide) {
				level.sendBlockUpdated(pos, blockState, blockState, Block.UPDATE_ALL_IMMEDIATE);
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
			int i = blockState.getValue(SnowLayerBlock.LAYERS);
			boolean hasOverlay = false;
			if (i != 0 && level.getBlockEntity(pos) instanceof SnowBlockEntity be) {
				hasOverlay = be.options.renderOverlay;
			}
			level.setBlock(pos, state2, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
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
}
