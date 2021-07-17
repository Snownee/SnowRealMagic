package snownee.snow;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.Skip;
import snownee.kiwi.client.model.TextureModel;
import snownee.kiwi.crafting.FullBlockIngredient;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;
import snownee.snow.block.ISnowVariant;
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
@KiwiModule.Group
public class CoreModule extends AbstractModule {
	public static final ItemGroup GROUP = new ItemGroup(SnowRealMagic.MODID) {
		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack createIcon() {
			return new ItemStack(Items.SNOWBALL);
		}
	};

	public static final INamedTag<Block> BOTTOM_SNOW = blockTag(SnowRealMagic.MODID, "bottom_snow");

	public static final INamedTag<Block> INVALID_SUPPORTERS = blockTag(SnowRealMagic.MODID, "invalid_supporters");

	public static final INamedTag<Block> CONTAINABLES = blockTag(SnowRealMagic.MODID, "containables");

	public static final INamedTag<Block> NOT_CONTAINABLES = blockTag(SnowRealMagic.MODID, "not_containables");

	@NoItem
	@Name("minecraft:snow")
	public static final ModSnowBlock BLOCK = new ModSnowBlock(blockProp(Blocks.SNOW).harvestTool(ToolType.SHOVEL));

	@NoItem
	@Name("snow")
	@RenderLayer(Layer.CUTOUT)
	public static final ModSnowTileBlock TILE_BLOCK = new ModSnowTileBlock(blockProp(BLOCK));

	@Name("minecraft:snow")
	public static final SnowBlockItem ITEM = new SnowBlockItem(BLOCK);

	public static final SnowFenceBlock FENCE = new SnowFenceBlock(blockProp(Blocks.OAK_FENCE).tickRandomly());

	public static final SnowFenceBlock FENCE2 = new SnowFenceBlock(blockProp(Blocks.NETHER_BRICK_FENCE).tickRandomly().harvestTool(ToolType.PICKAXE));

	public static final SnowStairsBlock STAIRS = new SnowStairsBlock(blockProp(Blocks.OAK_STAIRS).tickRandomly());

	public static final SnowSlabBlock SLAB = new SnowSlabBlock(blockProp(Blocks.OAK_SLAB).tickRandomly());

	public static final SnowFenceGateBlock FENCE_GATE = new SnowFenceGateBlock(blockProp(Blocks.OAK_FENCE_GATE).tickRandomly());

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
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		Block block = FENCE;
		TextureModel.register(event, block, null, "0");
		TextureModel.registerInventory(event, block, "0");

		block = FENCE2;
		TextureModel.register(event, block, null, "0");
		TextureModel.registerInventory(event, block, "0");

		block = STAIRS;
		BlockState state = block.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
		TextureModel.register(event, block, state, "0");

		block = SLAB;
		state = block.getDefaultState();
		TextureModel.register(event, block, state, "0");

		block = FENCE_GATE;
		state = block.getDefaultState().with(SnowFenceGateBlock.DOWN, false);
		TextureModel.register(event, block, state, "0");

		block = WALL;
		TextureModel.register(event, block, null, "0");
		TextureModel.registerInventory(event, block, "0");

		ModBlockItem.INSTANT_UPDATE_TILES.add(TEXTURE_TILE);
	}

	public static ItemStack makeTextureItem(Item item, ItemStack mark) {
		ItemStack stack = new ItemStack(item);
		NBTHelper helper = NBTHelper.of(stack);
		String v = Util.trimRL(mark.getItem().getRegistryName());
		helper.setString("BlockEntityTag.Textures.0", NBTUtil.writeBlockState(((BlockItem) mark.getItem()).getBlock().getDefaultState()).toString());
		helper.setString("BlockEntityTag.Items.0", v);
		return stack;
	}

	public static void fillTextureItems(INamedTag<Item> tag, Block block, NonNullList<ItemStack> items) {
		fillTextureItems(tag, block, items, Predicates.alwaysTrue());
	}

	public static void fillTextureItems(INamedTag<Item> tag, Block block, NonNullList<ItemStack> items, Predicate<Item> filter) {
		if (!Kiwi.areTagsUpdated()) {
			return;
		}
		Item item = block.asItem();
		items.addAll(tag.getAllElements().stream().filter(i -> i instanceof BlockItem && ((BlockItem) i).getBlock().getDefaultState().isSolid() && !i.getRegistryName().getNamespace().equals(SnowRealMagic.MODID)).filter(filter).map(ItemStack::new).filter(FullBlockIngredient::isTextureBlock).map(m -> CoreModule.makeTextureItem(item, m)).collect(Collectors.toList()));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockTint(ColorHandlerEvent.Block event) {
		if (!SnowClientConfig.colorTint)
			return;
		BlockColors blockColors = event.getBlockColors();
		blockColors.register((state, world, pos, index) -> {
			if (world == null || pos == null) {
				return -1;
			}
			Block block = state.getBlock();
			if (block instanceof ISnowVariant) {
				BlockState raw = ((ISnowVariant) block).getRaw(state, world, pos);
				return blockColors.getColor(raw, world, pos, index); // getColor
			}
			return -1;
		}, SLAB, STAIRS, WALL, FENCE, FENCE2, FENCE_GATE);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onItemTint(ColorHandlerEvent.Item event) {
		if (!SnowClientConfig.colorTint)
			return;
		ItemColors itemColors = event.getItemColors();
		itemColors.register((stack, index) -> {
			NBTHelper data = NBTHelper.of(stack);
			String rl = data.getString("BlockEntityTag.Items.0");
			if (rl != null && ResourceLocation.isResouceNameValid(rl)) {
				Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rl));
				if (item != null) {
					return itemColors.getColor(new ItemStack(item), index);
				}
			}
			return -1;
		}, SLAB, STAIRS, WALL, FENCE, FENCE2, FENCE_GATE);
	}
}
