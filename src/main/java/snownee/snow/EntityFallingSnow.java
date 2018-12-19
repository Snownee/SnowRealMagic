package snownee.snow;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFallingSnow extends Entity
{
    public int fallTime;
    private BlockPos prevPos;
    private int layers;
    protected static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(EntityFallingSnow.class,
            DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> LAYERS = EntityDataManager.createKey(EntityFallingSnow.class,
            DataSerializers.VARINT);

    public EntityFallingSnow(World worldIn)
    {
        super(worldIn);
        prevPos = BlockPos.ORIGIN;
        this.layers = 1;
    }

    public EntityFallingSnow(World worldIn, double x, double y, double z, int layers)
    {
        super(worldIn);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.setPosition(x, y + (1.0F - this.height) / 2.0F, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.layers = layers;
        this.setData(new BlockPos(this), layers);
        prevPos = new BlockPos(this);
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        ++this.fallTime;

        if (!this.hasNoGravity())
        {
            this.motionY -= 0.03999999910593033D;
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        BlockPos pos = new BlockPos(this);
        if (!this.world.isRemote)
        {
            if (!this.onGround)
            {
                if (this.fallTime > 100 && !this.world.isRemote && (pos.getY() < 1 || pos.getY() > 256)
                        || this.fallTime > 600)
                {

                    this.setDead();
                }
                else if (!pos.equals(prevPos))
                {
                    prevPos = pos;
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (ModConfig.snowMakingIce && block == Blocks.WATER)
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
                        world.playSound(null, pos.up(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.AMBIENT, 0.8F,
                                0.8F);
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
            else
            {
                IBlockState state = this.world.getBlockState(pos);

                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
                this.motionY *= -0.5D;

                if (state.getBlock() != Blocks.PISTON_EXTENSION)
                {
                    BlockSnowLayer.placeLayersOn(world, pos, layers, true);
                    this.setDead();
                    return;
                }
            }
        }

        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(ORIGIN, BlockPos.ORIGIN);
        this.dataManager.register(LAYERS, 1);
    }

    public void setData(BlockPos pos, int layers)
    {
        this.dataManager.set(ORIGIN, pos);
        this.dataManager.set(LAYERS, layers);
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getOrigin()
    {
        return this.dataManager.get(ORIGIN);
    }

    @SideOnly(Side.CLIENT)
    public int getLayers()
    {
        return this.dataManager.get(LAYERS);
    }

    @SideOnly(Side.CLIENT)
    public World getWorldObj()
    {
        return this.world;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setInteger("Time", this.fallTime);
        compound.setInteger("Layers", this.layers);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        this.fallTime = compound.getInteger("Time");
        if (compound.hasKey("Layers", Constants.NBT.TAG_INT))
        {
            this.layers = compound.getInteger("Layers");
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire()
    {
        return false;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean canBeAttackedWithItem()
    {
        return false;
    }

}
