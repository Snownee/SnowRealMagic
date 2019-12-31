package snownee.snow;

import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
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
import snownee.snow.client.SnowRenderer;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.item.SnowBlockItem;
import snownee.snow.world.gen.feature.ModIceAndSnowFeature;

@KiwiModule(modid = SnowRealMagic.MODID)
@KiwiModule.Subscriber({ Bus.MOD, Bus.FORGE })
@KiwiModule.Group
public class MainModule extends AbstractModule {
    public static final ItemGroup GROUP = new ItemGroup(SnowRealMagic.MODID) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(Items.SNOWBALL);
        }
    };

    @NoItem
    @Name("minecraft:snow")
    public static final ModSnowBlock BLOCK = new ModSnowBlock(Block.Properties.from(Blocks.SNOW));

    @NoItem
    @Name("snow")
    public static final ModSnowTileBlock TILE_BLOCK = new ModSnowTileBlock(Block.Properties.from(BLOCK));

    @Name("minecraft:snow")
    public static final SnowBlockItem ITEM = new SnowBlockItem(BLOCK);

    public static final SnowFenceBlock FENCE = new SnowFenceBlock(Block.Properties.from(Blocks.OAK_FENCE).tickRandomly());

    public static final SnowStairsBlock STAIRS = new SnowStairsBlock(Block.Properties.from(Blocks.OAK_STAIRS).tickRandomly());

    public static final SnowSlabBlock SLAB = new SnowSlabBlock(Block.Properties.from(Blocks.OAK_SLAB).tickRandomly());

    public static final SnowFenceGateBlock FENCE_GATE = new SnowFenceGateBlock(Block.Properties.from(Blocks.OAK_FENCE_GATE).tickRandomly());

    public static final SnowWallBlock WALL = new SnowWallBlock(Block.Properties.from(Blocks.COBBLESTONE_WALL).tickRandomly());

    @Name("snow")
    public static final TileEntityType<?> TILE = TileEntityType.Builder.create(() -> new SnowTile(), TILE_BLOCK).build(null);

    public static final TileEntityType<?> TEXTURE_TILE = TileEntityType.Builder.create(() -> new SnowTextureTile(), FENCE, STAIRS, SLAB, FENCE_GATE, WALL).build(null);

    @Name("snow")
    public static final EntityType<?> ENTITY = EntityType.Builder.create(EntityClassification.MISC).setCustomClientFactory((
            spawnEntity, world
    ) -> new FallingSnowEntity(world)).size(0.98F, 0.001F).build(SnowRealMagic.MODID + ".snow");

    @Name("minecraft:freeze_top_layer")
    public static final ModIceAndSnowFeature FEATURE = new ModIceAndSnowFeature();

    public MainModule() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SnowCommonConfig.spec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SnowClientConfig.spec);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientInit(FMLClientSetupEvent event) {
        SnowClientConfig.refresh();

        RenderingRegistry.registerEntityRenderingHandler(FallingSnowEntity.class, FallingSnowRenderer::new);

        ClientRegistry.bindTileEntitySpecialRenderer(SnowTile.class, new SnowRenderer());
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        SnowCommonConfig.refresh();
    }

    @Override
    protected void postInit() {
        for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
            if (biome.getFeatures(GenerationStage.Decoration.TOP_LAYER_MODIFICATION).removeIf(MainModule::isVanillaFeature)) {
                biome.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Biome.createDecoratedFeature(FEATURE, IFeatureConfig.NO_FEATURE_CONFIG, Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
            }
        }
    }

    private static boolean isVanillaFeature(ConfiguredFeature<?> cf) {
        if (cf.feature == Feature.DECORATED && cf.config instanceof DecoratedFeatureConfig) {
            return ((DecoratedFeatureConfig) cf.config).feature.feature == Feature.FREEZE_TOP_LAYER;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        Block block = FENCE;
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

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != Hand.MAIN_HAND) {
            return;
        }
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = worldIn.getBlockState(pos);
        if (!(state.getBlock() instanceof ISnowVariant)) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        if (!ForgeHooks.canHarvestBlock(BLOCK.getDefaultState(), player, worldIn, pos)) {
            return;
        }
        BlockState newState = ((ISnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
        worldIn.setBlockState(pos, newState);
        if (player instanceof ServerPlayerEntity) {
            if (newState.isSolid())
                pos = pos.up();
            Block.spawnAsEntity(worldIn, pos, new ItemStack(Items.SNOWBALL));
            player.getHeldItemMainhand().damageItem(1, player, stack -> {
                stack.sendBreakAnimation(Hand.MAIN_HAND);
            });
        }
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
    }

    public static ItemStack makeTextureItem(Item item, ItemStack mark) {
        ItemStack stack = new ItemStack(item);
        NBTHelper helper = NBTHelper.of(stack);
        String v = Util.trimRL(mark.getItem().getRegistryName());
        helper.setString("BlockEntityTag.Textures.0", NBTUtil.writeBlockState(((BlockItem) mark.getItem()).getBlock().getDefaultState()).toString());
        helper.setString("BlockEntityTag.Items.0", v);
        return stack;
    }

    public static void fillTextureItems(Tag<Item> tag, Block block, NonNullList<ItemStack> items) {
        Item item = block.asItem();
        items.addAll(tag.getAllElements().stream().filter(i -> i instanceof BlockItem && ((BlockItem) i).getBlock().getRenderLayer() == BlockRenderLayer.SOLID && !i.getRegistryName().getNamespace().equals(SnowRealMagic.MODID)).map(ItemStack::new).filter(FullBlockIngredient::isTextureBlock).map(m -> MainModule.makeTextureItem(item, m)).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (SnowCommonConfig.placeSnowInBlock && event.side.isServer() && event.phase == TickEvent.Phase.END && event.world instanceof ServerWorld) {
            WorldTickHandler.tick(event);
        }
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
                return blockColors.getColor(raw, world, pos, index);
            }
            return -1;
        }, SLAB, STAIRS, WALL, FENCE, FENCE_GATE);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTint(ColorHandlerEvent.Item event) {
        if (!SnowClientConfig.colorTint)
            return;
        ItemColors itemColors = event.getItemColors();
        itemColors.register((stack, index) -> {
            NBTHelper data = NBTHelper.of(stack);
            ResourceLocation rl = Util.RL(data.getString("BlockEntityTag.Items.0"));
            if (rl != null) {
                Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item != null) {
                    return itemColors.getColor(new ItemStack(item), index);
                }
            }
            return -1;
        }, SLAB, STAIRS, WALL, FENCE, FENCE_GATE);
    }
}
