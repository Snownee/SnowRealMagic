package snownee.snow.mixin.neoforge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import snownee.snow.block.SnowVariant;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {
	// prevent mods like Diagonal Blocks from breaking things
	@WrapOperation(
			method = "setRenderLayer(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/client/renderer/RenderType;)V", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;setRenderLayer(Lnet/minecraft/world/level/block/Block;Lnet/neoforged/neoforge/client/ChunkRenderTypeSet;)V"))
	private static void srm_setRenderLayer(Block block, ChunkRenderTypeSet layers, Operation<Void> original) {
		if (block instanceof SnowVariant) {
			layers = ChunkRenderTypeSet.all();
		}
		original.call(block, layers);
	}
}
