package snownee.snow.item;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.ModSnowLayerBlock;

public class SnowLayerBlockItem extends BlockItem {

	public SnowLayerBlockItem(Block block) {
		super(block, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (SnowCommonConfig.placeSnowInBlock && level.getFluidState(pos).isEmpty()) {
			BlockState state = level.getBlockState(pos);
			BlockPlaceContext blockContext = new BlockPlaceContext(context);
			if (ModSnowLayerBlock.canContainState(state)) {
				if (ModSnowLayerBlock.placeLayersOn(level, pos, 1, false, blockContext, true) && !level.isClientSide && (player == null || !player.isCreative())) {
					context.getItemInHand().shrink(1);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
			if (!state.canBeReplaced(blockContext)) {
				pos = pos.relative(context.getClickedFace());
				state = level.getBlockState(pos);
				if (ModSnowLayerBlock.canContainState(state)) {
					if (ModSnowLayerBlock.placeLayersOn(level, pos, 1, false, blockContext, true) && !level.isClientSide && (player == null || !player.isCreative())) {
						context.getItemInHand().shrink(1);
					}
					return InteractionResult.sidedSuccess(level.isClientSide);
				}
			}
		}
		return super.useOn(context);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, Player p_40599_, ItemStack stack, BlockState p_40601_) {
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (worldIn.isClientSide && tile != null) {
			CompoundTag data = BlockItem.getBlockEntityData(stack);
			if (data != null) {
				data = data.copy();
				tile.load(data);
				tile.setChanged();
			}
		}
		return super.updateCustomBlockEntityTag(pos, worldIn, p_40599_, stack, p_40601_);
	}

	@Override
	public void registerBlocks(Map<Block, Item> blockToItemMap, Item itemIn) {
		blockToItemMap.put(CoreModule.TILE_BLOCK, CoreModule.ITEM);
		super.registerBlocks(blockToItemMap, CoreModule.ITEM);
	}

	@Override
	public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
		blockToItemMap.remove(CoreModule.TILE_BLOCK);
		super.removeFromBlockToItemMap(blockToItemMap, CoreModule.ITEM);
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return SnowRealMagic.MODID;
	}

}
