package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class WeeeFlowersCompat {

	public static boolean enabled;
	private static Set<Block> flowers;

	public static void init() {
		if (!Loader.isModLoaded("weeeflowers")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("weeeflowers".equals(name.getNamespace()) && (name.getPath().contains("flower"))) {
				set.add(block);
			}
		}
		flowers = set.build();
	}

	public static boolean isFlower(Block block) {
		return enabled && flowers.contains(block);
	}

}
