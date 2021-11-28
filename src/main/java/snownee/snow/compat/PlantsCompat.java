package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class PlantsCompat {

	public static boolean enabled;
	private static Set<Block> plants;

	public static void init() {
		if (!Loader.isModLoaded("plants2")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("plants2".equals(name.getNamespace()) && (name.getPath().contains("cosmetic") || name.getPath().contains("desert") || name.getPath().contains("double") || name.getPath().contains("harvest") || name.getPath().contains("bushling"))) {
				set.add(block);
			}
		}
		plants = set.build();
	}

	public static boolean isPlant(Block block) {
		return enabled && plants.contains(block);
	}

}
