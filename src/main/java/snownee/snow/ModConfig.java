package snownee.snow;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Config(modid = SnowRealMagic.MODID)
@Mod.EventBusSubscriber(modid = SnowRealMagic.MODID)
public final class ModConfig {
	private ModConfig() {
		throw new UnsupportedOperationException("No instance for you");
	}

	@SubscribeEvent
	public static void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(SnowRealMagic.MODID)) {
			ConfigManager.sync(SnowRealMagic.MODID, Config.Type.INSTANCE);
		}
	}

	@Config.Name("PlaceSnowInBlock")
	@Config.RequiresMcRestart
	public static boolean placeSnowInBlock = true;

	@Config.Name("ReplaceSnowWorldGen")
	public static boolean replaceSnowWorldGen = true;

	@Config.Name("SnowWorldGenPriority")
	public static int snowWorldGenPriority = 5;

	@Config.Name("SnowGravity")
	public static boolean snowGravity = true;

	@Config.Name("SnowAlwaysReplaceable")
	public static boolean snowAlwaysReplaceable = true;

	@Config.Name("SnowAccumulationDuringSnowstorm")
	public static boolean snowAccumulationDuringSnowstorm = true;

	@Config.Name("SnowAccumulationDuringSnowfall")
	public static boolean snowAccumulationDuringSnowfall = false;

	@Config.Name("ThinnerBoundingBox")
	public static boolean thinnerBoundingBox = true;

	@Config.Name("ParticleThroughLeaves")
	public static boolean particleThroughLeaves = true;

	@Config.Name("SnowMakingIce")
	public static boolean snowMakingIce = true;

	@Config.Name("SnowOnIce")
	public static boolean snowOnIce = false;

	@Config.Name("SnowNeverMelt")
	public static boolean snowNeverMelt = false;

	public static void postInit() {
		new Thread(() -> {
			filterBlocks(snowLoggableBlocksSet, snowLoggableBlocks);
			filterBlocks(yOffsetBlocksSet, yOffsetBlocks);
		}).start();
	}

	private static void filterBlocks(Set<ResourceLocation> set, String[] array) {
		set.clear();
		for (String s : array) {
			if (s.startsWith("/") && s.endsWith("/")) {
				Predicate<String> pattern = Pattern.compile("^" + s.substring(1, s.length() - 1) + "$").asPredicate();
				for (Block block : ForgeRegistries.BLOCKS) {
					if (pattern.test(block.getRegistryName().toString())) {
						set.add(block.getRegistryName());
					}
				}
			} else {
				ResourceLocation id = new ResourceLocation(s);
				if (ForgeRegistries.BLOCKS.containsKey(id)) {
					set.add(id);
				}
			}
		}
	}

	@Config.Ignore
	public static final Set<ResourceLocation> snowLoggableBlocksSet = Sets.newHashSet();

	@Config.Ignore
	public static final Set<ResourceLocation> yOffsetBlocksSet = Sets.newHashSet();

	/* off */
	@Config.RequiresMcRestart
	public static String[] snowLoggableBlocks = {
			"/(biomesoplenty|xlfoodmod|weeeflowers):.*(flower|mushroom|grass|plant|bamboo|vanilla)/",
			"defiledlands:scuronotte",
			"defiledlands:blastem",
			"/ferdinandsflowers:.+(flower|double|desert|ouch|fungus|dark)/",
			"/greenery:plant\\/upland\\/.+/",
			"/harvestcraft:.*garden.*/",
			"/notreepunching:loose_rock\\/.+/",
			"/(pyrotech|pyrotech_compat):.*rock.*/",
			"/plants2:.*(cosmetic|desert|double|harvest|bushling).*/"
	};

	@Config.RequiresMcRestart
	public static String[] yOffsetBlocks = {
			"/notreepunching:loose_rock\\/.+/",
			"/(pyrotech|pyrotech_compat):.*rock.*/"
	};
}
