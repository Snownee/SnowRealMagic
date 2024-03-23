package snownee.snow.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

	public BlockItemMixin(Properties properties) {
		super(properties);
	}

	@WrapOperation(
			method = "useOn", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"))
	private InteractionResult srm_useOn(BlockItem instance, BlockPlaceContext placeContext, Operation<InteractionResult> original) {
		InteractionResult result = original.call(instance, placeContext);
		if (result.consumesAction() || this != Items.SNOW || !SnowCommonConfig.canPlaceSnowInBlock()) {
			return result;
		}
		Level level = placeContext.getLevel();
		BlockPos relativePos = placeContext.getClickedPos();
		Player player = placeContext.getPlayer();
		BlockState state = level.getBlockState(relativePos);
		boolean canPlace = false;
		if (Hooks.canContainState(state)) {
			canPlace = true;
		} else if (state.getBlock() instanceof SnowVariant snowVariant) {
			int layers = snowVariant.layers(state, level, relativePos);
			int maxLayers = snowVariant.maxLayers(state, level, relativePos);
			canPlace = layers < maxLayers;
		}
		if (canPlace) {
			if (Hooks.placeLayersOn(level, relativePos, 1, false, placeContext, true, true) && !level.isClientSide &&
					(player == null || !player.getAbilities().instabuild)) {
				placeContext.getItemInHand().shrink(1);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return result;
	}

	@Inject(
			at = @At(
					"HEAD"
			),
			method = "updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z"
	)
	private void srm_updateCustomBlockEntityTag(
			BlockPos pos,
			Level worldIn,
			Player p_40599_,
			ItemStack stack,
			BlockState p_40601_,
			CallbackInfoReturnable<Boolean> ci) {
		if (this != Items.SNOW) {
			return;
		}
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (worldIn.isClientSide && tile != null) {
			CompoundTag data = BlockItem.getBlockEntityData(stack);
			if (data != null) {
				data = data.copy();
				tile.load(data);
				tile.setChanged();
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "registerBlocks")
	private void srm_registerBlocks(Map<Block, Item> blockToItemMap, Item itemIn, CallbackInfo ci) {
		if (this != Items.SNOW) {
			return;
		}
		blockToItemMap.put(CoreModule.TILE_BLOCK.get(), Items.SNOW);
	}

	//	@Override
	//	public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
	//		blockToItemMap.remove(CoreModule.TILE_BLOCK);
	//		super.removeFromBlockToItemMap(blockToItemMap, CoreModule.ITEM);
	//	}

	//	@Override
	//	public String getCreatorModId(ItemStack itemStack) {
	//		return SnowRealMagic.MODID;
	//	}

}
