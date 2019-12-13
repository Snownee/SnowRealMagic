package snownee.snow.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.ModSnowBlock;

public class SnowBlockItem extends BlockItem {

    public SnowBlockItem(Block block) {
        super(block, new Item.Properties().group(ItemGroup.DECORATIONS));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (SnowCommonConfig.placeSnowInBlock && world.getFluidState(pos).isEmpty()) {
            BlockState state = world.getBlockState(pos);
            BlockItemUseContext blockContext = new BlockItemUseContext(context);
            if (ModSnowBlock.canContainState(state)) {
                if (ModSnowBlock.placeLayersOn(world, pos, 1, false, blockContext, true) && !world.isRemote && !context.getPlayer().isCreative()) {
                    context.getItem().shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
            if (state.isReplaceable(blockContext)) {
                pos = pos.offset(context.getFace());
                state = world.getBlockState(pos);
                if (ModSnowBlock.canContainState(state)) {
                    if (ModSnowBlock.placeLayersOn(world, pos, 1, false, blockContext, true) && !world.isRemote && !context.getPlayer().isCreative()) {
                        context.getItem().shrink(1);
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.onItemUse(context);
    }

}
