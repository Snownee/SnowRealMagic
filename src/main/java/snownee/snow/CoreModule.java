package snownee.snow;

import java.util.Arrays;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
import snownee.kiwi.Skip;
import snownee.snow.block.ModSnowBlock;
import snownee.snow.block.ModSnowTileBlock;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.block.SnowSlabBlock;
import snownee.snow.block.SnowStairsBlock;
import snownee.snow.block.SnowTextureTile;
import snownee.snow.block.SnowTile;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.client.FallingSnowRenderer;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.item.SnowBlockItem;
import snownee.snow.loot.NormalLootEntry;

@KiwiModule(modid = SnowRealMagic.MODID)
@KiwiModule.Subscriber(Bus.MOD)
public class CoreModule extends AbstractModule {

	public static final INamedTag<Block> BOTTOM_SNOW = blockTag(SnowRealMagic.MODID, "bottom_snow");

	public static final INamedTag<Block> INVALID_SUPPORTERS = blockTag(SnowRealMagic.MODID, "invalid_supporters");

	public static final INamedTag<Block> CONTAINABLES = blockTag(SnowRealMagic.MODID, "containables");

	public static final INamedTag<Block> NOT_CONTAINABLES = blockTag(SnowRealMagic.MODID, "not_containables");

	@NoItem
	@Name("minecraft:snow")
	public static final ModSnowBlock BLOCK = new ModSnowBlock(blockProp(Blocks.SNOW).harvestTool(ToolType.SHOVEL));

	@NoItem
	@Name("snow")
	public static final ModSnowTileBlock TILE_BLOCK = new ModSnowTileBlock(blockProp(BLOCK));

	@Name("minecraft:snow")
	public static final SnowBlockItem ITEM = new SnowBlockItem(BLOCK);

	@NoItem
	public static final SnowFenceBlock FENCE = new SnowFenceBlock(blockProp(Blocks.OAK_FENCE).tickRandomly());

	@NoItem
	public static final SnowFenceBlock FENCE2 = new SnowFenceBlock(blockProp(Blocks.NETHER_BRICK_FENCE).tickRandomly().harvestTool(ToolType.PICKAXE));

	@NoItem
	public static final SnowStairsBlock STAIRS = new SnowStairsBlock(blockProp(Blocks.OAK_STAIRS).tickRandomly());

	@NoItem
	public static final SnowSlabBlock SLAB = new SnowSlabBlock(blockProp(Blocks.OAK_SLAB).tickRandomly());

	@NoItem
	public static final SnowFenceGateBlock FENCE_GATE = new SnowFenceGateBlock(blockProp(Blocks.OAK_FENCE_GATE).tickRandomly());

	@NoItem
	public static final SnowWallBlock WALL = new SnowWallBlock(blockProp(Blocks.COBBLESTONE_WALL).tickRandomly());

	@Name("snow")
	public static final TileEntityType<SnowTile> TILE = TileEntityType.Builder.create(SnowTile::new, TILE_BLOCK).build(null);

	public static final TileEntityType<SnowTextureTile> TEXTURE_TILE = TileEntityType.Builder.create(SnowTextureTile::new, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL).build(null);

	@Name("snow")
	public static final EntityType<FallingSnowEntity> ENTITY = EntityType.Builder.<FallingSnowEntity>create(FallingSnowEntity::new, EntityClassification.MISC).setCustomClientFactory((spawnEntity, world) -> new FallingSnowEntity(world)).size(0.98F, 0.001F).build(SnowRealMagic.MODID + ".snow");

	@Skip
	public static final LootPoolEntryType NORMAL = Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, SnowRealMagic.MODID + ":normal", new LootPoolEntryType(new NormalLootEntry.Serializer()));

	public static final GameRules.RuleKey<GameRules.IntegerValue> BLIZZARD_STRENGTH = GameRules.func_234903_a_("blizzardStrength", GameRules.Category.MISC, GameRules.IntegerValue.create(0));

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(ENTITY, FallingSnowRenderer::new);

		Predicate<RenderType> blockRenderTypes = ImmutableSet.of(RenderType.getSolid(), RenderType.getCutout(), RenderType.getCutoutMipped(), RenderType.getTranslucent())::contains;
		for (Block block : Arrays.asList(TILE_BLOCK, FENCE, FENCE2, FENCE_GATE, SLAB, STAIRS, WALL))
			RenderTypeLookup.setRenderLayer(block, blockRenderTypes);
	}

	public static final ResourceLocation OVERLAY_MODEL = new ResourceLocation(SnowRealMagic.MODID, "block/overlay");

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerExtraModel(ModelRegistryEvent event) {
		ModelLoader.addSpecialModel(OVERLAY_MODEL);
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
