package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class FerdinandsFlowersCompat {

	public static boolean enabled;
	private static Set<Block> flowers;

	public static void init() {
		if (!Loader.isModLoaded("ferdinandsflowers")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("ferdinandsflowers".equals(name.getNamespace()) && (name.getPath().contains("flower") || name.getPath().contains("double") || name.getPath().contains("desert") || name.getPath().contains("ouch") || name.getPath().contains("fungus") || name.getPath().contains("dark"))) {
				set.add(block);
			}
		}
		flowers = set.build();
	}

	public static boolean isFlower(Block block) {
		return enabled && flowers.contains(block);
	}

}
