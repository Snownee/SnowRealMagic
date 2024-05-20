package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

	public BlockItemMixin(Properties properties) {
		super(properties);
	}

	@WrapOperation(
			method = "useOn", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"))
	private InteractionResult srm_useOn(
			BlockItem instance,
			BlockPlaceContext placeContext,
			Operation<InteractionResult> original,
			UseOnContext useOnContext) {
		if (this != Items.SNOW || !SnowCommonConfig.canPlaceSnowInBlock()) {
			return original.call(instance, placeContext);
		}
		Level level = placeContext.getLevel();
		BlockPos placePos = null;
		if (Hooks.canPlaceAt(level, useOnContext.getClickedPos())) {
			placePos = useOnContext.getClickedPos();
		} else if (Hooks.canPlaceAt(level, placeContext.getClickedPos())) {
			placePos = placeContext.getClickedPos();
		}
		if (placePos == null) {
			return original.call(instance, placeContext);
		} else {
			Player player = placeContext.getPlayer();
			if (Hooks.placeLayersOn(level, placePos, 1, false, placeContext, true, true) && !level.isClientSide &&
					(player == null || !player.getAbilities().instabuild)) {
				placeContext.getItemInHand().shrink(1);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Inject(
			at = @At("HEAD"),
			method = "updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z")
	private void srm_updateCustomBlockEntityTag(
			BlockPos pos,
			Level worldIn,
			Player player,
			ItemStack stack,
			BlockState blockState,
			CallbackInfoReturnable<Boolean> ci) {
		if (this != Items.SNOW) {
			return;
		}
		var tile = worldIn.getBlockEntity(pos);
		if (worldIn.isClientSide && tile != null) {
			var blockEntityData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
			if (!blockEntityData.isEmpty()) {
				blockEntityData.loadInto(tile, worldIn.registryAccess());
				tile.setChanged();
			}
		}
	}
}
