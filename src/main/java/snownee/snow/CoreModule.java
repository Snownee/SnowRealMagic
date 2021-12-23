package snownee.snow;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.KiwiModule.RenderLayer.Layer;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.snow.block.EntitySnowLayerBlock;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.block.SnowSlabBlock;
import snownee.snow.block.SnowStairsBlock;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.FallingSnowRenderer;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.loot.NormalLootEntry;

@KiwiModule
public class CoreModule extends AbstractModule {

	public static final Tag.Named<Block> BOTTOM_SNOW = blockTag(SnowRealMagic.MODID, "bottom_snow");

	public static final Tag.Named<Block> INVALID_SUPPORTERS = blockTag(SnowRealMagic.MODID, "invalid_supporters");

	public static final Tag.Named<Block> CONTAINABLES = blockTag(SnowRealMagic.MODID, "containables");

	public static final Tag.Named<Block> NOT_CONTAINABLES = blockTag(SnowRealMagic.MODID, "not_containables");

	//	@NoItem
	//	@Name("minecraft:snow")
	//	public static final ModSnowLayerBlock BLOCK = new ModSnowLayerBlock(blockProp(Blocks.SNOW));

	@NoItem
	@Name("snow")
	@RenderLayer(Layer.CUTOUT)
	public static final EntitySnowLayerBlock TILE_BLOCK = new EntitySnowLayerBlock(blockProp(Blocks.SNOW));

	//	@Name("minecraft:snow")
	//	public static final SnowLayerBlockItem ITEM = new SnowLayerBlockItem(BLOCK);

	@NoItem
	@RenderLayer(Layer.CUTOUT)
	public static final SnowFenceBlock FENCE = new SnowFenceBlock(blockProp(Blocks.OAK_FENCE).randomTicks());

	@NoItem
	@RenderLayer(Layer.CUTOUT)
	public static final SnowFenceBlock FENCE2 = new SnowFenceBlock(blockProp(Blocks.NETHER_BRICK_FENCE).randomTicks());

	@NoItem
	@RenderLayer(Layer.CUTOUT)
	public static final SnowStairsBlock STAIRS = new SnowStairsBlock(blockProp(Blocks.OAK_STAIRS).randomTicks());

	@NoItem
	@RenderLayer(Layer.CUTOUT)
	public static final SnowSlabBlock SLAB = new SnowSlabBlock(blockProp(Blocks.OAK_SLAB).randomTicks());

	@NoItem
	@RenderLayer(Layer.CUTOUT)
	public static final SnowFenceGateBlock FENCE_GATE = new SnowFenceGateBlock(blockProp(Blocks.OAK_FENCE_GATE).randomTicks());

	@NoItem
	@RenderLayer(Layer.CUTOUT)
	public static final SnowWallBlock WALL = new SnowWallBlock(blockProp(Blocks.COBBLESTONE_WALL).randomTicks());

	@Name("snow")
	public static final BlockEntityType<SnowBlockEntity> TILE = blockEntity(SnowBlockEntity::new, null, TILE_BLOCK);

	public static final BlockEntityType<SnowCoveredBlockEntity> TEXTURE_TILE = blockEntity(SnowCoveredBlockEntity::new, null, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL);

	@Name("snow")
	public static final EntityType<FallingSnowEntity> ENTITY = FabricEntityTypeBuilder.<FallingSnowEntity>create(MobCategory.MISC, FallingSnowEntity::new).entityFactory((spawnEntity, world) -> new FallingSnowEntity(world)).dimensions(EntityDimensions.fixed(0.98F, 0.001F)).build();

	public static final LootPoolEntryType NORMAL = new LootPoolEntryType(new NormalLootEntry.Serializer());

	public static final GameRules.Key<IntegerValue> BLIZZARD_STRENGTH = GameRuleRegistry.register("blizzardStrength", GameRules.Category.MISC, GameRuleFactory.createIntRule(0));

	public static final GameRules.Key<IntegerValue> BLIZZARD_FREQUENCY = GameRuleRegistry.register("blizzardFrequency", GameRules.Category.MISC, GameRuleFactory.createIntRule(10000));

	public CoreModule() {
		if (Platform.isPhysicalClient()) {
			if (Platform.isModLoaded("sodium") && !Platform.isModLoaded("indium")) {
				SnowRealMagic.LOGGER.warn("Please install Indium mod to make Snow! Real Magic! work with Sodium.");
			}
			ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> out.accept(ClientVariables.OVERLAY_MODEL));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		EntityRendererRegistry.register(ENTITY, FallingSnowRenderer::new);
	}

	//	@SubscribeEvent
	//	@Environment(EnvType.CLIENT)
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
