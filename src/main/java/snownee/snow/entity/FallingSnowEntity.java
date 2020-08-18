package snownee.snow.entity;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.ModSnowBlock;

public class FallingSnowEntity extends Entity {
    public int fallTime;
    private BlockPos prevPos;
    private int layers;
    protected static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(FallingSnowEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> LAYERS = EntityDataManager.createKey(FallingSnowEntity.class, DataSerializers.VARINT);
    private EntitySize size;

    public FallingSnowEntity(World worldIn) {
        super(MainModule.ENTITY, worldIn);
        prevPos = BlockPos.ZERO;
        this.layers = 1;
        size = new EntitySize(0.98f, 0.1225f * layers, true);
    }

    public FallingSnowEntity(World worldIn, double x, double y, double z, int layers) {
        super(MainModule.ENTITY, worldIn);
        this.preventEntitySpawning = true;
        this.setPosition(x, y + (1.0F - this.getHeight()) / 2.0F, z);
        this.setMotion(Vector3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.layers = layers;
        this.setData(getPosition(), layers);
        prevPos = getPosition();
        size = new EntitySize(0.98f, 0.1225f * layers, true);
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return size;
    }

    @Override
    public void tick() {
        //        this.prevPosX = this.posX;
        //        this.prevPosY = this.posY;
        //        this.prevPosZ = this.posZ;

        ++this.fallTime;

        if (!this.hasNoGravity()) {
            this.setMotion(this.getMotion().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getMotion());

        BlockPos pos = getPosition();
        if (!this.world.isRemote) {
            if (!this.onGround) {
                if (this.fallTime > 100 && !this.world.isRemote && (pos.getY() < 1 || pos.getY() > 256) || this.fallTime > 600) {

                    this.remove();
                } else if (!pos.equals(prevPos)) {
                    prevPos = pos;
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (SnowCommonConfig.snowMakingIce && block == Blocks.WATER) {
                        world.setBlockState(pos, Blocks.ICE.getDefaultState());
                        remove();
                        return;
                    }
                    if (state.getMaterial() == Material.LAVA) {
                        if (world.isRemote) {
                            Random random = world.rand;
                            for (int i = 0; i < 10; ++i) {
                                double d0 = random.nextGaussian() * 0.02D;
                                double d1 = random.nextGaussian() * 0.02D;
                                double d2 = random.nextGaussian() * 0.02D;
                                world.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextFloat(), pos.getY() + 1, pos.getZ() + random.nextFloat(), d0, d1, d2);
                            }
                        }
                        world.playSound(null, pos.up(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.AMBIENT, 0.8F, 0.8F);
                        remove();
                        return;
                    }
                    if (state.getMaterial().isLiquid()) {
                        remove();
                        return;
                    }
                }
            } else {
                BlockState state = this.world.getBlockState(pos);

                this.setMotion(this.getMotion().mul(0.7D, -0.5D, 0.7D));

                if (state.getBlock() != Blocks.MOVING_PISTON) {
                    if (state.getCollisionShape(world, pos, ISelectionContext.forEntity(this)).isEmpty()) {
                        BlockPos posDown = pos.down();
                        BlockState stateDown = world.getBlockState(posDown);
                        Block block = stateDown.getBlock();
                        if (block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof WallBlock || block instanceof StairsBlock && stateDown.get(StairsBlock.HALF) == Half.BOTTOM) {
                            pos = posDown;
                        }
                    }
                    ModSnowBlock.placeLayersOn(world, pos, layers, true, new DirectionalPlaceContext(this.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP), true);
                    this.remove();
                    return;
                }
            }
        }

        this.setMotion(this.getMotion().scale(0.98D));
    }

    public void setData(BlockPos pos, int layers) {
        this.dataManager.set(ORIGIN, pos);
        this.dataManager.set(LAYERS, layers);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getOrigin() {
        return this.dataManager.get(ORIGIN);
    }

    @OnlyIn(Dist.CLIENT)
    public int getLayers() {
        return this.dataManager.get(LAYERS);
    }

    @OnlyIn(Dist.CLIENT)
    public World getWorldObj() {
        return this.world;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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

    @Override
    protected void registerData() {
        this.dataManager.register(ORIGIN, BlockPos.ZERO);
        this.dataManager.register(LAYERS, 1);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.fallTime = compound.getInt("Time");
        if (compound.contains("Layers", Constants.NBT.TAG_INT)) {
            this.layers = compound.getInt("Layers");
            size = new EntitySize(0.98f, 0.1225f * layers, true);
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("Time", this.fallTime);
        compound.putInt("Layers", this.layers);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
