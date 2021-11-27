package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class HarvestCraftCompat {

	public static boolean enabled;
	private static Set<Block> gardens;

	public static void init() {
		if (!Loader.isModLoaded("harvestcraft")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("harvestcraft".equals(name.getNamespace()) && (name.getPath().contains("garden"))) {
				set.add(block);
			}
		}
		gardens = set.build();
	}

	public static boolean isGarden(Block block) {
		return enabled && gardens.contains(block);
	}

}
