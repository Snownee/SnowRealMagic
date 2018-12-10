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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
                        && Blocks.SNOW_LAYER.canPlaceBlockAt(worldIn, blockpos.up()))
                {
                    blockpos = pos.up();
                    state = worldIn.getBlockState(blockpos);
                    block = state.getBlock();
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
            float f = playerIn.rotationPitch;
            float f1 = playerIn.rotationYaw;
            double d0 = playerIn.posX;
            double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
            double d2 = playerIn.posZ;
            Vec3d vec3d = new Vec3d(d0, d1, d2);
            float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
            float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
            float f4 = -MathHelper.cos(-f * 0.017453292F);
            float f5 = MathHelper.sin(-f * 0.017453292F);
            float f6 = f3 * f4;
            float f7 = f2 * f4;
            double d3 = playerIn.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
            Vec3d vec3d1 = vec3d.add((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
            RayTraceResult result = worldIn.rayTraceBlocks(vec3d, vec3d1, false, false, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit != EnumFacing.DOWN)
            {
                actionResult = onItemUse(playerIn, worldIn, result.getBlockPos(), handIn, result.sideHit,
                        (float) result.hitVec.x, (float) result.hitVec.y, (float) result.hitVec.z);
            }
        }
        return new ActionResult<ItemStack>(actionResult, playerIn.getHeldItem(handIn));
    }
}
