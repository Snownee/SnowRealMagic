package snownee.snow.mixin;

import java.util.Random;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.CoreModule;
import snownee.snow.GameEvents;
import snownee.snow.Hooks;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.SnowClientConfig;
import snownee.snow.entity.FallingSnowEntity;

@Mixin(SnowLayerBlock.class)
public class ModSnowLayerBlock extends Block implements SnowVariant {
	private static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[] { Shapes.empty(), Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 0, 0, 16, 6, 16), Block.box(0, 0, 0, 16, 7, 16) };
	@Shadow
	private static VoxelShape[] SHAPE_BY_LAYER;

	public ModSnowLayerBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	@Overwrite
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (ModUtil.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
			return SHAPE_BY_LAYER[state.getValue(SnowLayerBlock.LAYERS) - 1];
		}
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		if (layers == 8) {
			return Shapes.block();
		}
		return SNOW_SHAPES_MAGIC[layers - 1];
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.scheduleTick(pos, this, tickRate());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	@Overwrite
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.scheduleTick(currentPos, this, tickRate());
			return stateIn;
		} else {
			return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}
	}

	@Override
	@Overwrite
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return Hooks.canSurvive(state, worldIn, pos, false);
	}

	protected int tickRate() {
		return 2;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		checkFallable(worldIn, pos, state);
	}

	@Override
	@Overwrite
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (ModUtil.shouldMelt(worldIn, pos)) {
			int layers = state.getValue(SnowLayerBlock.LAYERS);
			if (layers == 8) {
				BlockState upState = worldIn.getBlockState(pos.above());
				if (upState.getBlock() instanceof SnowLayerBlock) {
					return;
				}
			}
			if (CoreModule.TILE_BLOCK.is(state)) {
				GameEvents.onDestroyedByPlayer(worldIn, null, pos, state, worldIn.getBlockEntity(pos));
			} else {
				dropResources(state, worldIn, pos);
				worldIn.removeBlock(pos, false);
			}
			return;
		}
		if (ModUtil.terraforged) {
			return;
		}
		if (!SnowCommonConfig.snowAccumulationDuringSnowfall && !SnowCommonConfig.snowAccumulationDuringSnowstorm) {
			return;
		}
		if (random.nextInt(8) > 0) {
			return;
		}
		int layers = state.getValue(SnowLayerBlock.LAYERS);
		BlockPos height = worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
		if (layers == 8) {
			if (height.getY() - 1 != pos.getY()) {
				return;
			}
			BlockState upState = worldIn.getBlockState(pos.above());
			if (upState.getBlock() instanceof SnowLayerBlock) {
				return;
			}
		} else {
			if (height.getY() != pos.getY()) {
				return;
			}
		}

		Biome biome = worldIn.getBiome(pos).value();
		boolean flag = false;
		if (worldIn.isRaining() && biome.coldEnoughToSnow(pos)) {
			if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
				flag = true;
			} else if (SnowCommonConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering()) {
				flag = true;
			}
		}

		if (flag && layers < SnowCommonConfig.snowAccumulationMaxLayers) {
			accumulate(worldIn, pos, state, (w, p) -> (SnowCommonConfig.snowAccumulationMaxLayers > 8 || !(w.getBlockState(p.below()).getBlock() instanceof ModSnowLayerBlock)) && w.getBrightness(LightLayer.BLOCK, p) < 10, true);
		} else if (!SnowCommonConfig.snowNeverMelt && SnowCommonConfig.snowNaturalMelt && !worldIn.isRaining()) {
			if (layers == 1) {
				if (SnowCommonConfig.snowAccumulationMaxLayers > 8 && worldIn.getBlockState(pos.below()).getBlock() instanceof ModSnowLayerBlock) {
					worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				}
			} else {
				accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.above()).getBlock() instanceof ModSnowLayerBlock), false);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void accumulate(Level world, BlockPos pos, BlockState centerState, BiPredicate<LevelAccessor, BlockPos> filter, boolean accumulate) {
		int i = centerState.getValue(SnowLayerBlock.LAYERS);
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			Direction direction = Direction.from2DDataValue(k);
			BlockPos pos2 = pos.relative(direction);
			if (j % 2 == 1) {
				pos2 = pos2.relative(Direction.from2DDataValue(k + 1));
			}
			if (!world.isLoaded(pos2) || !filter.test(world, pos2)) {
				continue;
			}
			BlockState state = world.getBlockState(pos2);
			BlockPos height = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos2);
			if (height.getY() != pos2.getY()) {
				continue;
			}

			if (!Blocks.SNOW.canSurvive(state, world, pos2)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof SnowLayerBlock) {
				l = state.getValue(SnowLayerBlock.LAYERS);
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				if (accumulate) {
					Hooks.placeLayersOn(world, pos2, 1, false, new DirectionalPlaceContext(world, pos2, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
				} else {
					world.setBlockAndUpdate(pos2, state.setValue(SnowLayerBlock.LAYERS, l - 1));
				}
				return;
			}
		}
		if (accumulate) {
			Hooks.placeLayersOn(world, pos, 1, false, new DirectionalPlaceContext(world, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
		} else {
			world.setBlockAndUpdate(pos, centerState.setValue(SnowLayerBlock.LAYERS, i - 1));
		}
	}

	protected boolean checkFallable(Level worldIn, BlockPos pos, BlockState state) {
		BlockPos posDown = pos.below();
		if (Hooks.canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) {
			if (!worldIn.isClientSide) {
				worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
				FallingSnowEntity entity = new FallingSnowEntity(worldIn, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, state.getValue(SnowLayerBlock.LAYERS));
				worldIn.addFreshEntity(entity);
			}
			return true;
		}
		return false;
	}

	@Override
	@Overwrite
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		int i = state.getValue(SnowLayerBlock.LAYERS);
		if (useContext.getItemInHand().is(Blocks.SNOW.asItem()) && i < 8) {
			if (useContext.replacingClickedOnBlock() && state.is(Blocks.SNOW)) {
				return useContext.getClickedFace() == Direction.UP;
			} else {
				return true;
			}
		}
		return (SnowCommonConfig.snowAlwaysReplaceable && state.getValue(SnowLayerBlock.LAYERS) < 8) || i == 1;
	}

	private static final Random RANDOM = new Random();

	@Override
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int originLayers, int layers) {
		double offsetY = originLayers / 8D;
		layers *= 10;
		for (int i = 0; i < layers; ++i) {
			double d0 = RANDOM.nextGaussian() * 0.1D;
			double d1 = RANDOM.nextGaussian() * 0.02D;
			double d2 = RANDOM.nextGaussian() * 0.1D;
			worldIn.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + RANDOM.nextFloat(), pos.getY() + offsetY, pos.getZ() + RANDOM.nextFloat(), d0, d1, d2);
		}
		SoundType soundtype = getSoundType(state);
		worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		return true;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		if (!SnowClientConfig.particleThroughLeaves || rand.nextInt(32) > 0) {
			return;
		}
		Entity entity = Minecraft.getInstance().getCameraEntity();
		if (entity != null && entity.blockPosition().distSqr(pos) > 256) {
			return;
		}
		BlockState stateDown = worldIn.getBlockState(pos.below());
		if (stateDown.is(BlockTags.LEAVES)) {
			double d0 = pos.getX() + rand.nextDouble();
			double d1 = pos.getY() - 0.05D;
			double d2 = pos.getZ() + rand.nextDouble();
			worldIn.addParticle(ParticleTypes.SNOWFLAKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public BlockState onShovel(BlockState state, Level world, BlockPos pos) {
		int layers = state.getValue(SnowLayerBlock.LAYERS) - 1;
		if (layers > 0) {
			return state.setValue(SnowLayerBlock.LAYERS, layers);
		} else {
			return getRaw(state, world, pos);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
			BlockState stateDown = worldIn.getBlockState(pos.below());
			if (!(stateDown.getBlock() instanceof SnowLayerBlock) && !stateDown.hasProperty(BlockStateProperties.SNOWY)) {
				if (state.is(Blocks.SNOW)) {
					worldIn.setBlock(pos, Hooks.copyProperties(state, CoreModule.TILE_BLOCK.defaultBlockState()), 16 | 32);
				}
				BlockEntity blockEntity = worldIn.getBlockEntity(pos);
				if (blockEntity instanceof SnowBlockEntity) {
					SnowBlockEntity snowTile = (SnowBlockEntity) blockEntity;
					if (CoreModule.TILE_BLOCK.is(state) && snowTile.getState().isAir()) {
						worldIn.setBlock(pos, Hooks.copyProperties(state, Blocks.SNOW.defaultBlockState()), 16 | 32);
					} else {
						snowTile.options.renderOverlay = !snowTile.options.renderOverlay;
						if (worldIn.isClientSide) {
							worldIn.sendBlockUpdated(pos, state, state, 11);
						}
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		if (state.is(Blocks.SNOW)) {
			BlockPlaceContext context = new BlockPlaceContext(player, handIn, player.getItemInHand(handIn), hit);
			Block block = Block.byItem(context.getItemInHand().getItem());
			if (block != null && block != Blocks.AIR && context.replacingClickedOnBlock()) {
				BlockState state2 = block.getStateForPlacement(context);
				if (state2 != null && Hooks.canContainState(state2) && state2.canSurvive(worldIn, pos)) {
					if (!worldIn.isClientSide) {
						worldIn.setBlock(pos, state2, 16 | 32);
						block.setPlacedBy(worldIn, pos, state, player, context.getItemInHand());
						int i = state.getValue(SnowLayerBlock.LAYERS);
						if (Hooks.placeLayersOn(worldIn, pos, i, false, context, true) && !player.isCreative()) {
							context.getItemInHand().shrink(1);
						}
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		return super.use(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	@Nullable
	@Overwrite
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
		if (blockstate.getBlock() instanceof SnowLayerBlock) {
			int i = blockstate.getValue(SnowLayerBlock.LAYERS);
			return blockstate.setValue(SnowLayerBlock.LAYERS, Math.min(8, i + 1));
		}
		ItemStack stack = context.getItemInHand();
		CompoundTag tag = BlockItem.getBlockEntityData(stack);
		if (tag != null && "snowrealmagic:snow".equals(tag.getString("id"))) {
			return CoreModule.TILE_BLOCK.defaultBlockState();
		}
		return defaultBlockState();
	}

	@Override
	public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			if (!state.is(this))
				return;
			//FIXME why
			if (state.is(Blocks.SNOW) || CoreModule.TILE_BLOCK.is(state)) {
				entityIn.causeFallDamage(fallDistance, 0.2F, DamageSource.FALL);
				return;
			}
			state = worldIn.getBlockState(pos);
			entityIn.causeFallDamage(fallDistance, 1 - state.getValue(SnowLayerBlock.LAYERS) * 0.1F, DamageSource.FALL);
			return;
		}
		super.fallOn(worldIn, state, pos, entityIn, fallDistance);
	} //FIXME what if below block is snow as well

	@Override
	public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
		if (SnowCommonConfig.thinnerBoundingBox) {
			double d0 = Math.abs(entityIn.getDeltaMovement().y);
			if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
				if (!state.is(this))
					return;
				int layers = state.getValue(SnowLayerBlock.LAYERS) - 1;
				double d1 = 1 - layers * 0.05f;
				entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
			}
		}
	}

	//	@Override
	//	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
	//		ItemStack stack = getRaw(state, world, pos).getCloneItemStack(target, world, pos, player);
	//		return stack.isEmpty() ? new ItemStack(CoreModule.ITEM) : stack;
	//	}

	@Override
	public double getYOffset() {
		return -1;
	}

}
