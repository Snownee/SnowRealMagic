package snownee.snow;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.util.EnumUtil;
import snownee.snow.block.EntitySnowLayerBlock;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.block.SnowSlabBlock;
import snownee.snow.block.SnowStairsBlock;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.client.FallingSnowRenderer;
import snownee.snow.client.SnowClient;
import snownee.snow.datagen.SnowBlockTagsProvider;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.loot.NormalLootEntry;
import snownee.snow.mixin.IntegerValueAccess;

@KiwiModule
@KiwiModule.Subscriber(modBus = true)
public class CoreModule extends AbstractModule {

	public static final TagKey<Block> BOTTOM_SNOW = blockTag(SnowRealMagic.MODID, "bottom_snow");

	public static final TagKey<Block> CONTAINABLES = blockTag(SnowRealMagic.MODID, "containables");

	public static final TagKey<Block> NOT_CONTAINABLES = blockTag(SnowRealMagic.MODID, "not_containables");

	public static final TagKey<Block> OFFSET_Y = blockTag(SnowRealMagic.MODID, "offset_y");

	public static final TagKey<Block> CANNOT_ACCUMULATE_ON = blockTag(SnowRealMagic.MODID, "cannot_accumulate_on");

	@NoItem
	@Name("snow")
	public static final KiwiGO<EntitySnowLayerBlock> TILE_BLOCK = go(() -> new EntitySnowLayerBlock(blockProp(Blocks.SNOW)));

	@NoItem
	public static final KiwiGO<Block> FENCE = go(() -> new SnowFenceBlock(blockProp(Blocks.OAK_FENCE).randomTicks()));

	@NoItem
	public static final KiwiGO<Block> FENCE2 = go(() -> new SnowFenceBlock(blockProp(Blocks.NETHER_BRICK_FENCE).randomTicks()));

	@NoItem
	public static final KiwiGO<Block> STAIRS = go(() -> new SnowStairsBlock(blockProp(Blocks.OAK_STAIRS).randomTicks()));

	@NoItem
	public static final KiwiGO<Block> SLAB = go(() -> new SnowSlabBlock(blockProp(Blocks.OAK_SLAB).randomTicks()));

	@NoItem
	public static final KiwiGO<Block> FENCE_GATE = go(() -> new SnowFenceGateBlock(blockProp(Blocks.OAK_FENCE_GATE).randomTicks()));

	@NoItem
	public static final KiwiGO<Block> WALL = go(() -> new SnowWallBlock(blockProp(Blocks.COBBLESTONE_WALL).randomTicks()));

	@Name("snow")
	public static final KiwiGO<BlockEntityType<SnowBlockEntity>> TILE = blockEntity(SnowBlockEntity::new, null, TILE_BLOCK);

	public static final KiwiGO<BlockEntityType<SnowCoveredBlockEntity>> TEXTURE_TILE = blockEntity(SnowCoveredBlockEntity::new, null, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL);

	@Name("snow")
	public static final KiwiGO<EntityType<FallingSnowEntity>> ENTITY = go(() -> EntityType.Builder.<FallingSnowEntity>of(FallingSnowEntity::new, MobCategory.MISC).setCustomClientFactory((spawnEntity, world) -> new FallingSnowEntity(world)).sized(0.98F, 0.001F).build(SnowRealMagic.MODID + ".snow"));

	public static final KiwiGO<LootPoolEntryType> NORMAL = go(() -> new LootPoolEntryType(new NormalLootEntry.Serializer()));

	public static final GameRules.Key<IntegerValue> BLIZZARD_STRENGTH = GameRules.register(SnowRealMagic.MODID + ":blizzardStrength", GameRules.Category.MISC, IntegerValueAccess.callCreate(0));

	public static final GameRules.Key<IntegerValue> BLIZZARD_FREQUENCY = GameRules.register(SnowRealMagic.MODID + ":blizzardFrequency", GameRules.Category.MISC, IntegerValueAccess.callCreate(10000));

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(ENTITY.get(), FallingSnowRenderer::new);
	}

	@Override
	protected void init(InitEvent event) {
		ModUtil.init();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		Predicate<RenderType> blockRenderTypes = EnumUtil.BLOCK_RENDER_TYPES::contains;
		for (Supplier<? extends Block> block : List.of(TILE_BLOCK, FENCE, FENCE2, FENCE_GATE, SLAB, STAIRS, WALL))
			ItemBlockRenderTypes.setRenderLayer(block.get(), blockRenderTypes);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerExtraModel(ModelEvent.RegisterAdditional event) {
		event.register(SnowClient.OVERLAY_MODEL);
	}

	@Override
	protected void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		SnowBlockTagsProvider blockTagsProvider = new SnowBlockTagsProvider(generator, event.getExistingFileHelper());
		generator.addProvider(event.includeServer(), blockTagsProvider);
	}

	//	@SubscribeEvent
	//	@OnlyIn(Dist.CLIENT)
	//	public void onBlockTint(ColorHandlerEvent.Block event) {
	//		if (!SnowClientConfig.colorTint)
	//			return;
	//		BlockColors blockColors = event.getBlockColors();
	//		blockColors.register((state, world, pos, index) -> {
	//			if (world == null || pos == null) {
	//				return -1;
	//			}
	//			Block block = state.getBlock();
	//			if (block instanceof ISnowVariant) {
	//				BlockState raw = ((ISnowVariant) block).getRaw(state, world, pos);
	//				return blockColors.getColor(raw, world, pos, index); // getColor
	//			}
	//			return -1;
	//		}, SLAB, STAIRS, WALL, FENCE, FENCE2, FENCE_GATE);
	//	}

}
