package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class BiomesOPlentyCompat {

	public static boolean enabled;
	private static Set<Block> plants;

	public static void init() {
		if (!Loader.isModLoaded("biomesoplenty")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("biomesoplenty".equals(name.getNamespace()) && (name.getPath().contains("bamboo") || name.getPath().contains("mushroom") || name.getPath().contains("plant") || name.getPath().contains("flower") || name.getPath().contains("gate"))) {
				set.add(block);
			}
		}
		plants = set.build();
	}

	public static boolean isPlantOrGate(Block block) {
		return enabled && plants.contains(block);
	}

}
