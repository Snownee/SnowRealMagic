package snownee.snow.compat;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;

@JeiPlugin
public class JeiCompat implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(SnowRealMagic.MODID, "main");

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		IIngredientManager manager = jeiRuntime.getIngredientManager();
		IIngredientType<ItemStack> type = manager.getIngredientType(ItemStack.class);
		Collection<ItemStack> collection = Arrays.asList(CoreModule.FENCE, CoreModule.FENCE2, CoreModule.FENCE_GATE, CoreModule.SLAB, CoreModule.STAIRS, CoreModule.WALL).stream().map(ItemStack::new).collect(Collectors.toList());
		manager.removeIngredientsAtRuntime(type, collection);
	}

}
