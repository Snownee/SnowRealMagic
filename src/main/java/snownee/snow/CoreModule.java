package snownee.snow;

import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.MapColor;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.block.entity.InheritanceBlockEntityType;
import snownee.snow.block.ExtraCollisionSnowLayerBlock;
import snownee.snow.block.SRMSnowLayerBlock;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.block.SnowSlabBlock;
import snownee.snow.block.SnowStairsBlock;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.block.WaterLoggableSnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.convert.BlockConverters;
import snownee.snow.loot.NormalizeLoot;
import snownee.snow.mixin.BlockBehaviourAccess;

@KiwiModule(modId = SnowRealMagic.ID)
public class CoreModule extends AbstractModule {
	public static final TagKey<Block> SNOW_TAG = blockTag("snow");

	public static final TagKey<Block> SNOWY_SETTING = blockTag("snowy_setting");

	public static final TagKey<Block> CONTAINABLES = blockTag("containables");

	public static final TagKey<Block> PLANTS = blockTag("plants");

	public static final TagKey<Block> NOT_CONTAINABLES = blockTag("not_containables");

	public static final TagKey<Block> ENTITY_INSIDE = blockTag("entity_inside");

	public static final TagKey<Block> ANIMATE_TICK = blockTag("animate_tick");

	public static final TagKey<Block> OFFSET_Y = blockTag("offset_y");

	public static final TagKey<Block> EXPAND_MODEL = blockTag("expand_model");

	public static final TagKey<Block> CANNOT_ACCUMULATE_ON = blockTag("cannot_accumulate_on");

	@NoItem
	@Name("snow_extra_collision")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_EXTRA_COLLISION_BLOCK = snowLayer(ExtraCollisionSnowLayerBlock::new);

	@NoItem
	@Name("snow")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_BLOCK = snowLayer(SRMSnowLayerBlock::new);

	@NoItem
	public static final KiwiGO<SRMSnowLayerBlock> SNOWY_PLANT = snowLayer(SRMSnowLayerBlock::new);

	@NoItem
	public static final KiwiGO<SRMSnowLayerBlock> SNOWY_DOUBLE_PLANT_LOWER = snowLayer(SRMSnowLayerBlock::new);

	@NoItem
	public static final KiwiGO<SRMSnowLayerBlock> SNOWY_DOUBLE_PLANT_UPPER = snowLayer(SRMSnowLayerBlock::new);

	@NoItem
	public static final KiwiGO<Block> FENCE = block(
			$ -> new SnowFenceBlock($.mapColor(MapColor.SNOW).sound(SoundType.SNOW).randomTicks()),
			() -> Blocks.OAK_FENCE);

	@NoItem
	public static final KiwiGO<Block> FENCE2 = block(
			$ -> new SnowFenceBlock($.mapColor(MapColor.SNOW)
					.sound(SoundType.SNOW)
					.randomTicks()
					.overrideDescription(FENCE.get().getDescriptionId())), () -> Blocks.NETHER_BRICK_FENCE);

	@NoItem
	public static final KiwiGO<Block> STAIRS = block(
			$ -> new SnowStairsBlock($.mapColor(MapColor.SNOW)
					.sound(SoundType.SNOW)
					.randomTicks()), () -> Blocks.OAK_STAIRS);

	@NoItem
	public static final KiwiGO<Block> SLAB = block(
			$ -> new SnowSlabBlock($.mapColor(MapColor.SNOW).sound(SoundType.SNOW).randomTicks()),
			() -> Blocks.OAK_SLAB);

	@NoItem
	public static final KiwiGO<Block> FENCE_GATE = block(
			$ -> new SnowFenceGateBlock($.mapColor(MapColor.SNOW)
					.sound(SoundType.SNOW)
					.randomTicks()), () -> Blocks.OAK_FENCE_GATE);

	@NoItem
	public static final KiwiGO<Block> WALL = block(
			$ -> new SnowWallBlock($.mapColor(MapColor.SNOW)
					.sound(SoundType.SNOW)
					.randomTicks()), () -> Blocks.COBBLESTONE_WALL);

	@Name("snow")
	public static final KiwiGO<BlockEntityType<SnowBlockEntity>> TILE = blockEntity(SnowBlockEntity::new, SRMSnowLayerBlock.class);

	public static final KiwiGO<BlockEntityType<SnowCoveredBlockEntity>> TEXTURE_TILE = go(
			() -> new InheritanceBlockEntityType<>(SnowCoveredBlockEntity::new, WaterLoggableSnowVariant.class, false),
			Registries.BLOCK_ENTITY_TYPE);

	public static final KiwiGO<MapCodec<NormalizeLoot>> NORMALIZE = go(() -> NormalizeLoot.CODEC, Registries.LOOT_POOL_ENTRY_TYPE);

	public static final GameRule<Integer> BLIZZARD_STRENGTH = GameRules.registerInteger(
			SnowRealMagic.ID + ":blizzard_strength",
			GameRuleCategory.MISC,
			0,
			0);

	public static final GameRule<Integer> BLIZZARD_FREQUENCY = GameRules.registerInteger(
			SnowRealMagic.ID + ":blizzard_frequency",
			GameRuleCategory.MISC,
			10000,
			0);

	public static final BlockConverters CONVERTERS = new BlockConverters();

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			CONVERTERS.initSnow();
			BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate = (blockState, blockGetter, blockPos, entityType) -> {
				final var below = blockPos.below();
				return blockState.getValue(SnowLayerBlock.LAYERS) <= SnowCommonConfig.mobSpawningMaxLayers &&
						blockGetter.getBlockState(below).isValidSpawn(blockGetter, below, entityType);
			};
			Stream.of(
					SNOW_EXTRA_COLLISION_BLOCK,
					SNOWY_DOUBLE_PLANT_LOWER,
					SNOWY_DOUBLE_PLANT_UPPER,
					SNOW_BLOCK,
					SNOWY_PLANT).map(KiwiGO::get).forEach(block -> {
				Item.BY_BLOCK.put(block, Items.SNOW);
				((BlockBehaviourAccess) block).getProperties().isValidSpawn(predicate);
			});
			((BlockBehaviourAccess) Blocks.SNOW).getProperties().isValidSpawn(predicate);
		});
	}

	private static KiwiGO<SRMSnowLayerBlock> snowLayer(Function<BlockBehaviour.Properties, SRMSnowLayerBlock> factory) {
		return block(
				$ -> {
					$.replaceable = false;
					$.overrideDescription(Blocks.SNOW.getDescriptionId());
					return factory.apply($);
				}, () -> Blocks.SNOW);
	}
}
