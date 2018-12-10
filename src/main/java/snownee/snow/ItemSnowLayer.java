package snownee.snow;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSnow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemSnowLayer extends ItemSnow
{

    public ItemSnowLayer(Block block)
    {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack))
        {
            if (ModConfig.placeSnowInBlock)
            {
                IBlockState state = worldIn.getBlockState(pos);
                Block block = state.getBlock();
                BlockPos blockpos = pos;

                if (facing == EnumFacing.UP && !block.isReplaceable(worldIn, blockpos)
                        && Blocks.SNOW_LAYER.canPlaceBlockAt(worldIn, blockpos))
                {
                    blockpos = pos.up();
                    state = worldIn.getBlockState(blockpos);
                }

                if (BlockSnowLayer.canContainState(state))
                {
                    BlockSnowLayer.placeLayersOn(worldIn, blockpos, 1, false);
                    return EnumActionResult.SUCCESS;
                }
            }

            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        EnumActionResult actionResult = EnumActionResult.PASS;
        if (ModConfig.placeSnowInBlock)
        {
            RayTraceResult result = rayTrace(worldIn, playerIn, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit == EnumFacing.UP)
            {
                actionResult = onItemUse(playerIn, worldIn, result.getBlockPos(), handIn, EnumFacing.UP,
                        (float) result.hitVec.x, (float) result.hitVec.y, (float) result.hitVec.z);
            }
        }
        return new ActionResult<ItemStack>(actionResult, playerIn.getHeldItem(handIn));
    }
}
