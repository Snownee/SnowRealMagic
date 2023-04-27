package snownee.snow.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.network.NetworkHooks;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;

// FallingBlockEntity
public class FallingSnowEntity extends Entity {
	public int fallTime;
	private BlockPos prevPos;
	private int layers;
	protected static final EntityDataAccessor<BlockPos> START_POS = SynchedEntityData.defineId(FallingSnowEntity.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Integer> LAYERS = SynchedEntityData.defineId(FallingSnowEntity.class, EntityDataSerializers.INT);
	private EntityDimensions size;

	public FallingSnowEntity(Level worldIn) {
		super(CoreModule.ENTITY.get(), worldIn);
		prevPos = BlockPos.ZERO;
		layers = 1;
		size = new EntityDimensions(0.98f, 0.1225f * layers, true);
	}

	public FallingSnowEntity(EntityType<FallingSnowEntity> type, Level worldIn) {
		this(worldIn);
	}

	public FallingSnowEntity(Level worldIn, double x, double y, double z, int layers) {
		super(CoreModule.ENTITY.get(), worldIn);
		blocksBuilding = true;
		setPos(x, y + (1.0F - getBbHeight()) / 2.0F, z);
		setDeltaMovement(Vec3.ZERO);
		xo = x;
		yo = y;
		zo = z;
		this.layers = layers;
		prevPos = blockPosition();
		setData(prevPos, layers);
		size = new EntityDimensions(0.98f, 0.1225f * layers, true);
	}

	@Override
	public EntityDimensions getDimensions(Pose poseIn) {
		return size;
	}

	@Override
	public void tick() {
		//        this.prevPosX = this.posX;
		//        this.prevPosY = this.posY;
		//        this.prevPosZ = this.posZ;

		++fallTime;

		if (!isNoGravity()) {
			this.setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
		}

		move(MoverType.SELF, getDeltaMovement());

		BlockPos pos = blockPosition();
		if (!level.isClientSide) {
			if (!onGround) {
				if (fallTime > 600 || (fallTime > 100 && level.isOutsideBuildHeight(pos))) {
					discard();
				} else if (!pos.equals(prevPos)) {
					prevPos = pos;
					BlockState state = level.getBlockState(pos);
					if (SnowCommonConfig.snowMakingIce && state.is(Blocks.WATER)) {
						level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
						discard();
						return;
					}
					if (state.getFluidState().is(FluidTags.LAVA)) {
						if (level.isClientSide) {
							RandomSource random = level.random;
							for (int i = 0; i < 10; ++i) {
								double d0 = random.nextGaussian() * 0.02D;
								double d1 = random.nextGaussian() * 0.02D;
								double d2 = random.nextGaussian() * 0.02D;
								level.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextFloat(), pos.getY() + 1, pos.getZ() + random.nextFloat(), d0, d1, d2);
							}
						}
						level.playSound(null, pos.above(), SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.8F, 0.8F);
						discard();
						return;
					}
					if (!state.getFluidState().isEmpty()) {
						discard();
						return;
					}
				}
			} else {
				BlockState state = level.getBlockState(pos);

				this.setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));

				if (state.getBlock() != Blocks.MOVING_PISTON) {
					if (state.getCollisionShape(level, pos, CollisionContext.of(this)).isEmpty()) {
						BlockPos posDown = pos.below();
						BlockState stateDown = level.getBlockState(posDown);
						Block block = stateDown.getBlock();
						if (block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof WallBlock || block instanceof StairBlock && stateDown.getValue(StairBlock.HALF) == Half.BOTTOM) {
							pos = posDown;
						}
					}
					Hooks.placeLayersOn(level, pos, layers, true, new DirectionalPlaceContext(level, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP), true, true);
					discard();
					return;
				}
			}
		}

		this.setDeltaMovement(getDeltaMovement().scale(0.98D));
	}

	public void setData(BlockPos pos, int layers) {
		entityData.set(START_POS, pos);
		entityData.set(LAYERS, layers);
	}

	public BlockPos getOrigin() {
		return entityData.get(START_POS);
	}

	public int getLayers() {
		return entityData.get(LAYERS);
	}

	@Override
	public boolean canBeCollidedWith() {
		return isAlive();
	}

	@Override
	public boolean displayFireAnimation() {
		return false;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(START_POS, BlockPos.ZERO);
		entityData.define(LAYERS, 1);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		fallTime = compound.getInt("Time");
		if (compound.contains("Layers", Tag.TAG_INT)) {
			layers = compound.getInt("Layers");
			size = new EntityDimensions(0.98f, 0.1225f * layers, true);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putInt("Time", fallTime);
		compound.putInt("Layers", layers);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
