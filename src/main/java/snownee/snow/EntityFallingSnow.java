package snownee.snow;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityFallingSnow extends EntityFallingBlock implements IEntityAdditionalSpawnData
{
    private BlockPos prevPos;

    public EntityFallingSnow(World worldIn)
    {
        super(worldIn);
        shouldDropItem = false;
        dontSetBlock = true;
        prevPos = BlockPos.ORIGIN;
    }

    public EntityFallingSnow(World worldIn, double x, double y, double z, IBlockState fallingBlockState)
    {
        super(worldIn, x, y, z, fallingBlockState);
        shouldDropItem = false;
        dontSetBlock = true;
        prevPos = new BlockPos(this);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        BlockPos pos = new BlockPos(this);
        if (onGround)
        {
            if (this.fallTile.getBlock() == Blocks.SNOW_LAYER)
            {
                int layers = this.fallTile.getValue(BlockSnow.LAYERS);
                BlockSnowLayer.placeLayersOn(world, pos, layers, true);
            }
            setDead();
            return;
        }
        else if (!pos.equals(prevPos))
        {
            prevPos = pos;
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.WATER)
            {
                world.setBlockState(pos, Blocks.ICE.getDefaultState());
                setDead();
                return;
            }
            if (state.getMaterial() == Material.LAVA)
            {
                if (world.isRemote)
                {
                    Random random = world.rand;
                    for (int i = 0; i < 10; ++i)
                    {
                        double d0 = random.nextGaussian() * 0.02D;
                        double d1 = random.nextGaussian() * 0.02D;
                        double d2 = random.nextGaussian() * 0.02D;
                        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + random.nextFloat(),
                                pos.getY() + 1, pos.getZ() + random.nextFloat(), d0, d1, d2);
                    }
                }
                world.playSound(null, pos.up(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.AMBIENT, 0.8F, 0.8F);
                setDead();
                return;
            }
            if (block instanceof IFluidBlock || block instanceof BlockLiquid)
            {
                setDead();
                return;
            }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        ByteBufUtils.writeTag(buffer, NBTUtil.writeBlockState(new NBTTagCompound(), this.fallTile));
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        this.fallTile = NBTUtil.readBlockState(ByteBufUtils.readTag(additionalData));
    }

}
