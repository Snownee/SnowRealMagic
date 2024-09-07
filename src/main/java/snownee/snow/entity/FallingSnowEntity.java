package snownee.snow.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
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
import snownee.snow.CoreModule;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.network.SLavaSmokeEffectPacket;
import snownee.snow.util.CommonProxy;

// FallingBlockEntity
public class FallingSnowEntity extends Entity {
	protected static final EntityDataAccessor<BlockPos> START_POS = SynchedEntityData.defineId(
			FallingSnowEntity.class,
			EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Integer> LAYERS = SynchedEntityData.defineId(
			FallingSnowEntity.class,
			EntityDataSerializers.INT);
	public int fallTime;
	private BlockPos prevPos;
	private int layers;

	public FallingSnowEntity(Level worldIn) {
		this(CoreModule.ENTITY.get(), worldIn);
	}

	public FallingSnowEntity(EntityType<? extends FallingSnowEntity> type, Level worldIn) {
		super(type, worldIn);
		blocksBuilding = true;
		prevPos = BlockPos.ZERO;
		layers = 1;
	}

	public FallingSnowEntity(Level worldIn, double x, double y, double z, int layers) {
		this(worldIn);
		this.dimensions = EntityDimensions.fixed(0.98f, 0.1225f * layers);
		setPos(x, y, z);
		setDeltaMovement(Vec3.ZERO);
		xo = x;
		yo = y;
		zo = z;
		setLayers(this.layers = layers);
		setStartPos(prevPos = blockPosition());
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
		Level level = level();
		if (!level.isClientSide) {
			if (!onGround()) {
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
					if (CommonProxy.isHot(state.getFluidState(), level, pos)) {
						new SLavaSmokeEffectPacket(pos.above()).sendToAround((ServerLevel) level);
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

				if (!state.is(Blocks.MOVING_PISTON)) {
					if (state.getCollisionShape(level, pos, CollisionContext.of(this)).isEmpty()) {
						BlockPos posDown = pos.below();
						BlockState stateDown = level.getBlockState(posDown);
						Block block = stateDown.getBlock();
						if (block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof WallBlock ||
								block instanceof StairBlock && stateDown.getValue(StairBlock.HALF) == Half.BOTTOM) {
							pos = posDown;
						}
					}
					Hooks.placeLayersOn(
							level,
							pos,
							layers,
							true,
							new DirectionalPlaceContext(level, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP),
							true,
							true);
					discard();
					return;
				}
			}
		}

		this.setDeltaMovement(getDeltaMovement().scale(0.98D));
	}

	public BlockPos getStartPos() {
		return entityData.get(START_POS);
	}

	public void setStartPos(BlockPos pos) {
		entityData.set(START_POS, pos);
	}

	public int getLayers() {
		return entityData.get(LAYERS);
	}

	public void setLayers(int layers) {
		entityData.set(LAYERS, Mth.clamp(layers, 1, 8));
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
	@NotNull
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(START_POS, BlockPos.ZERO);
		builder.define(LAYERS, 1);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		fallTime = compound.getInt("Time");
		if (compound.contains("Layers", Tag.TAG_INT)) {
			layers = Mth.clamp(compound.getInt("Layers"), 1, 8);
			dimensions = EntityDimensions.fixed(0.98f, 0.1225f * layers);
			setLayers(layers);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putInt("Time", fallTime);
		compound.putInt("Layers", layers);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(this, serverEntity, layers);
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket packet) {
		super.recreateFromPacket(packet);
		this.layers = packet.getData();
		this.blocksBuilding = true;
		this.setPos(packet.getX(), packet.getY(), packet.getZ());
		this.setStartPos(this.blockPosition());
	}

	@Override
	public Component getTypeName() {
		return Component.translatable("entity.minecraft.falling_block_type", Blocks.SNOW.getName());
	}

	@Nullable
	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Blocks.SNOW);
	}
}
