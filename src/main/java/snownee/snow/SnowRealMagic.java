package snownee.snow;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.snow.compat.FBPHack;
import snownee.snow.mixin.ItemBlockAccessor;

@Mod(
		modid = SnowRealMagic.MODID, name = SnowRealMagic.NAME, version = "@VERSION_INJECT@", acceptedMinecraftVersions = "[1.12, 1.13)"
)
@EventBusSubscriber
public class SnowRealMagic {
	public static final String MODID = "snowrealmagic";
	public static final String NAME = "Snow! Real Magic!";

	public static Logger logger;

	public static Block BLOCK = Blocks.SNOW_LAYER;

	public SnowRealMagic() {
		MinecraftForge.TERRAIN_GEN_BUS.register(SnowGenerator.class);
		if (FMLCommonHandler.instance().getSide().isClient() && Loader.isModLoaded("fbp") && !Loader.instance().getIndexedModList().get("fbp").getName().contains("Fancier")) {
			MinecraftForge.EVENT_BUS.register(new FBPHack());
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerWorldGenerator(new SnowGenerator(), ModConfig.snowWorldGenPriority);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ModConfig.postInit();
	}

	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event) {
		BLOCK = new ModSnowBlock();
		event.getRegistry().register(BLOCK);
		Item item = Item.getItemFromBlock(BLOCK);
		if (item instanceof ItemBlock) {
			((ItemBlockAccessor) item).setBlock(BLOCK);
		}
		if (ModConfig.placeSnowInBlock) {
			GameRegistry.registerTileEntity(SnowTile.class, new ResourceLocation(MODID, "snow"));
		}
	}

	@SubscribeEvent
	public static void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
		event.getRegistry().register(EntityEntryBuilder.create().entity(FallingSnowEntity.class).id(new ResourceLocation(MODID, "snow"), 0).name(MODID + ".snow").tracker(160, 20, true).build());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(FallingSnowEntity.class, RenderFallingSnow::new);

		if (ModConfig.placeSnowInBlock) {
			ModelLoader.setCustomStateMapper(BLOCK, new StateMap.Builder().ignore(ModSnowBlock.TILE).build());
		}
	}

}
