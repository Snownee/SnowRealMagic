package snownee.snow.compat;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Set;

public final class DefiledLandsCompat {

	public static boolean enabled;
	private static Set<Block> defiledPlants;

	public static void init() {
		if (!Loader.isModLoaded("defiledlands")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("defiledlands".equals(name.getNamespace()) && (name.getPath().contains("scuronotte") || name.getPath().contains("blastem"))) {
				set.add(block);
			}
		}
		defiledPlants = set.build();
	}

	public static boolean isDefiledPlant(Block block) {
		return enabled && defiledPlants.contains(block);
	}

}
