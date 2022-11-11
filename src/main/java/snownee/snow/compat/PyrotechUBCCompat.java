package snownee.snow.compat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class PyrotechUBCCompat {

	public static boolean enabled;
	private static Set<Block> rocks;

	public static void init() {
		if (!Loader.isModLoaded("pyrotech_compat")) {
			return;
		}
		enabled = true;
		ImmutableSet.Builder<Block> set = ImmutableSet.builder();
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if ("pyrotech_compat".equals(name.getNamespace()) && name.getPath().contains("rock")) {
				set.add(block);
			}
		}
		rocks = set.build();
	}

	public static boolean isRock(Block block) {
		return enabled && rocks.contains(block);
	}

}
