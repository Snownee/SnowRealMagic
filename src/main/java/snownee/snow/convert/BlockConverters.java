package snownee.snow.convert;

import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireflyBushBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ShortDryGrassBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TallDryGrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;
import snownee.snow.util.KeyedList;

public class BlockConverters {
	public final KeyedList<Identifier, BlockConverter> converters = new KeyedList<>();
	public final KeyedList<Identifier, BlockConverter> airConverters = new KeyedList<>(1);
	private final Object2ObjectLinkedOpenHashMap<BlockState, @Nullable BlockConverter> cache = new Object2ObjectLinkedOpenHashMap<>();

	public void add(Identifier id, BlockConverter converter) {
		if (converter.acceptAir()) {
			airConverters.putLast(id, converter);
		} else {
			converters.putLast(id, converter);
		}
	}

	@Nullable
	public BlockConverter of(BlockState blockState) {
		BlockConverter converter = cache.computeIfAbsent(blockState, this::ofInternal);
		if (cache.size() > 1024) {
			cache.removeFirst();
		}
		return converter;
	}

	@Nullable
	private BlockConverter ofInternal(BlockState blockState) {
		for (BlockConverter converter : blockState.isAir() ? airConverters : converters) {
			if (converter.takeIn(blockState)) {
				return converter.accept(blockState) ? converter : null;
			}
		}
		return null;
	}

	public void initSnow() {
		add(
				SnowRealMagic.id("air"), new BlockConverter() {
					@Override
					public boolean takeIn(BlockState blockState) {
						return true;
					}

					@Override
					public boolean acceptAir() {
						return true;
					}

					@Override
					public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
						Block block = Blocks.SNOW;
						if (SnowCommonConfig.fancySnowOnUpperSlab && level.getBlockState(pos.below()).getBlock() instanceof SlabBlock) {
							block = CoreModule.SNOW_BLOCK.get();
						}
						return block.defaultBlockState();
					}
				});
		add(
				SnowRealMagic.id("double_plant"), new BlockConverter() {
					@Override
					public boolean takeIn(BlockState blockState) {
						return blockState.getBlock() instanceof DoublePlantBlock;
					}

					@Override
					public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
						if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
							return CoreModule.SNOWY_DOUBLE_PLANT_LOWER.defaultBlockState();
						} else {
							return CoreModule.SNOWY_DOUBLE_PLANT_UPPER.defaultBlockState();
						}
					}
				});
		add(
				SnowRealMagic.id("slab"), new CoveredBlockConverter(SlabBlock.class, CoreModule.SLAB.defaultBlockState()) {
					@Override
					public boolean accept(BlockState blockState) {
						return blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
					}

					@Override
					public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
						return result;
					}
				});
		add(
				SnowRealMagic.id("stairs"), new CoveredBlockConverter(StairBlock.class, CoreModule.STAIRS.defaultBlockState()) {
					@Override
					public boolean accept(BlockState blockState) {
						return super.accept(blockState) && blockState.getValue(StairBlock.HALF) == Half.BOTTOM;
					}
				});
		add(SnowRealMagic.id("wall"), new CoveredBlockConverter(WallBlock.class, CoreModule.WALL.defaultBlockState()));
		add(
				SnowRealMagic.id("fence"), new CoveredBlockConverter(FenceBlock.class, CoreModule.FENCE.defaultBlockState()) {
					@Override
					public BlockState result(BlockState blockState) {
						return (
								blockState.getSoundType() == SoundType.WOOD || blockState.is(BlockTags.WOODEN_FENCES) ?
										CoreModule.FENCE :
										CoreModule.FENCE2).defaultBlockState();
					}
				});
		add(SnowRealMagic.id("fence_gate"), new CoveredBlockConverter(FenceGateBlock.class, CoreModule.FENCE_GATE.defaultBlockState()));
		add(
				SnowRealMagic.id("plant"), new BlockConverter() {
					@Override
					public boolean takeIn(BlockState blockState) {
						return blockState.is(CoreModule.PLANTS);
					}

					@Override
					public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
						return CoreModule.SNOWY_PLANT.defaultBlockState();
					}
				});
		add(
				SnowRealMagic.id("fallback"), new BlockConverter() {
					private final Object2BooleanOpenHashMap<Block> cache = new Object2BooleanOpenHashMap<>();

					@Override
					public boolean takeIn(BlockState blockState) {
						return isTakeInPlant(blockState) || blockState.is(CoreModule.CONTAINABLES);
					}

					@Override
					public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
						if (!blockState.getCollisionShape(level, pos).isEmpty()) {
							return CoreModule.SNOW_EXTRA_COLLISION_BLOCK.defaultBlockState();
						}
						return isTakeInPlant(blockState) ?
								CoreModule.SNOWY_PLANT.defaultBlockState() :
								CoreModule.SNOW_BLOCK.defaultBlockState();
					}

					private boolean isTakeInPlant(BlockState blockState) {
						return cache.computeIfAbsent(
								blockState.getBlock(),
								block -> block instanceof TallGrassBlock || block instanceof TallDryGrassBlock ||
										block instanceof ShortDryGrassBlock || block instanceof BushBlock ||
										block instanceof FireflyBushBlock || block instanceof FlowerBlock ||
										block instanceof SaplingBlock || block instanceof MushroomBlock ||
										block instanceof SweetBerryBushBlock || block instanceof FlowerBedBlock);
					}
				});
	}
}
