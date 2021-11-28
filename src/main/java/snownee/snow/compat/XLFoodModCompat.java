package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class XLFoodModCompat {

	public static boolean enabled;
	private static Set<Block> plants;

	public static void init() {
		if (!Loader.isModLoaded("xlfoodmod")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("xlfoodmod".equals(name.getNamespace()) && (name.getPath().contains("grass") || name.getPath().contains("vanilla") || name.getPath().contains("plant"))) {
				set.add(block);
			}
		}
		plants = set.build();
	}

	public static boolean isPlant(Block block) {
		return enabled && plants.contains(block);
	}

}
