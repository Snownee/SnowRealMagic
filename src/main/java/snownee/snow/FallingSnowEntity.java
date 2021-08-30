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

public class FallingSnowEntity extends Entity {
	public int fallTime;
	private BlockPos prevPos;
	private int layers;
	protected static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(FallingSnowEntity.class, DataSerializers.BLOCK_POS);
	private static final DataParameter<Integer> LAYERS = EntityDataManager.createKey(FallingSnowEntity.class, DataSerializers.VARINT);

	public FallingSnowEntity(World worldIn) {
		super(worldIn);
		prevPos = BlockPos.ORIGIN;
		layers = 1;
	}

	public FallingSnowEntity(World worldIn, double x, double y, double z, int layers) {
		super(worldIn);
		preventEntitySpawning = true;
		setSize(0.98F, 0.98F);
		setPosition(x, y + (1.0F - height) / 2.0F, z);
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
		this.layers = layers;
		setData(new BlockPos(this), layers);
		prevPos = new BlockPos(this);
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		++fallTime;

		if (!hasNoGravity()) {
			motionY -= 0.03999999910593033D;
		}

		move(MoverType.SELF, motionX, motionY, motionZ);

		BlockPos pos = new BlockPos(this);
		if (!world.isRemote) {
			if (!onGround) {
				if (fallTime > 100 && !world.isRemote && (pos.getY() < 1 || pos.getY() > 256) || fallTime > 600) {
					setDead();
				} else if (!pos.equals(prevPos)) {
					prevPos = pos;
					IBlockState state = world.getBlockState(pos);
					Block block = state.getBlock();
					if (ModConfig.snowMakingIce && block == Blocks.WATER) {
						world.setBlockState(pos, Blocks.ICE.getDefaultState());
						setDead();
						return;
					}
					if (state.getMaterial() == Material.LAVA) {
						if (world.isRemote) {
							Random random = world.rand;
							for (int i = 0; i < 10; ++i) {
								double d0 = random.nextGaussian() * 0.02D;
								double d1 = random.nextGaussian() * 0.02D;
								double d2 = random.nextGaussian() * 0.02D;
								world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + random.nextFloat(), pos.getY() + 1, pos.getZ() + random.nextFloat(), d0, d1, d2);
							}
						}
						world.playSound(null, pos.up(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.AMBIENT, 0.8F, 0.8F);
						setDead();
						return;
					}
					if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
						setDead();
						return;
					}
				}
			} else {
				IBlockState stateDown = world.getBlockState(pos.down());
				if (ModSnowBlock.canContainState(stateDown)) {
					pos = pos.down();
				}
				IBlockState state = world.getBlockState(pos);

				motionX *= 0.699999988079071D;
				motionZ *= 0.699999988079071D;
				motionY *= -0.5D;

				if (state.getBlock() != Blocks.PISTON_EXTENSION) {
					ModSnowBlock.placeLayersOn(world, pos, layers, true, true, 3);
					setDead();
					return;
				}
			}
		}

		motionX *= 0.9800000190734863D;
		motionY *= 0.9800000190734863D;
		motionZ *= 0.9800000190734863D;
	}

	@Override
	protected void entityInit() {
		dataManager.register(ORIGIN, BlockPos.ORIGIN);
		dataManager.register(LAYERS, 1);
	}

	public void setData(BlockPos pos, int layers) {
		dataManager.set(ORIGIN, pos);
		dataManager.set(LAYERS, layers);
	}

	@SideOnly(Side.CLIENT)
	public BlockPos getOrigin() {
		return dataManager.get(ORIGIN);
	}

	@SideOnly(Side.CLIENT)
	public int getLayers() {
		return dataManager.get(LAYERS);
	}

	@SideOnly(Side.CLIENT)
	public World getWorldObj() {
		return world;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("Time", fallTime);
		compound.setInteger("Layers", layers);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		fallTime = compound.getInteger("Time");
		if (compound.hasKey("Layers", Constants.NBT.TAG_INT)) {
			layers = compound.getInteger("Layers");
		}
	}

	@Override
	public boolean canBeCollidedWith() {
		return !isDead;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

}
