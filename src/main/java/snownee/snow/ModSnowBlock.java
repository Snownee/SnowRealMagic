package snownee.snow;

import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.snow.compat.NoTreePunchingCompat;

public class ModSnowBlock extends BlockSnow {
	protected static final AxisAlignedBB[] SNOW_AABB_MAGIC = new AxisAlignedBB[] { new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.4375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D) };

	public static final PropertyBool TILE = PropertyBool.create("tile");

	public ModSnowBlock() {
		if (ModConfig.placeSnowInBlock) {
			setDefaultState(blockState.getBaseState().withProperty(LAYERS, 1).withProperty(TILE, false));
		}
		setRegistryName("minecraft", "snow_layer");
		setHardness(0.1F);
		setSoundType(SoundType.SNOW);
		setTranslationKey("snow");
		setLightOpacity(0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		if (ModConfig.placeSnowInBlock) {
			return new BlockStateContainer(this, LAYERS, TILE);
		}
		return super.createBlockState();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		if (ModConfig.placeSnowInBlock) {
			return state.getValue(TILE);
		}
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		if (ModConfig.placeSnowInBlock) {
			return new SnowTile();
		}
		return null;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (ModConfig.placeSnowInBlock) {
			IBlockState state = super.getStateFromMeta(meta);
			if (meta >= 8) {
				state = state.withProperty(TILE, true);
			}
			return state;
		}
		return super.getStateFromMeta(meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = super.getMetaFromState(state);
		if (ModConfig.placeSnowInBlock && state.getValue(TILE)) {
			meta += 8;
		}
		return meta;
	}

	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (!ModConfig.thinnerBoundingBox) {
			return super.getCollisionBoundingBox(blockState, worldIn, pos);
		}
		int layers = blockState.getValue(LAYERS);
		BlockPos posUp = pos.up();
		if (layers == 8 && worldIn.getBlockState(posUp).getCollisionBoundingBox(worldIn, posUp) != null) {
			return FULL_BLOCK_AABB;
		}
		return SNOW_AABB_MAGIC[layers - 1];
	}

	@Override
	@SuppressWarnings("deprecation")
	public RayTraceResult collisionRayTrace(IBlockState state, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
		RayTraceResult hit1 = null;
		if (ModConfig.placeSnowInBlock && state.getValue(TILE)) {
			hit1 = rayTrace(pos, start, end, getContainedState(worldIn, pos).getBoundingBox(worldIn, pos));
		}
		RayTraceResult hit2 = super.collisionRayTrace(state, worldIn, pos, start, end);
		if (hit1 == null) {
			return hit2;
		}
		if (hit2 == null) {
			return hit1;
		}
		Vec3d vec1 = hit1.hitVec;
		Vec3d vec2 = hit2.hitVec;
		if (start.squareDistanceTo(vec1) < start.squareDistanceTo(vec2)) {
			return hit1;
		} else {
			return hit2;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		AxisAlignedBB aabb = super.getSelectedBoundingBox(state, worldIn, pos);
		if (ModConfig.placeSnowInBlock && state.getValue(TILE)) {
			IBlockState contained = getContainedState(worldIn, pos);
			if (contained.getBlock() != Blocks.AIR)
				aabb = aabb.union(contained.getSelectedBoundingBox(worldIn, pos));
		}
		return aabb;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (state.getBlock() != this) // Fix Fancy Block Particles mod crash
			return false;
		return state.getValue(LAYERS) == 8;
	}

	@Override
	public int tickRate(World worldIn) {
		return 2;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (ModConfig.snowGravity && !BlockFalling.fallInstantly) {
			worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (ModConfig.snowGravity && !BlockFalling.fallInstantly) {
			worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.isRemote)
			return;
		checkFallable(worldIn, pos, state);
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		IBlockState iblockstate = worldIn.getBlockState(pos.down());
		Block block = iblockstate.getBlock();

		if (ModConfig.snowOnIce || (block != Blocks.ICE && block != Blocks.PACKED_ICE && block != Blocks.BARRIER)) {
			BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP);
			return blockfaceshape == BlockFaceShape.SOLID || iblockstate.getBlock().isLeaves(iblockstate, worldIn, pos.down()) || block == this && iblockstate.getValue(LAYERS).intValue() == 8;
		} else {
			return false;
		}
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		if (!ModConfig.snowNeverMelt && worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11) {
			worldIn.setBlockToAir(pos);
			return;
		}
		if ((!ModConfig.snowAccumulationDuringSnowfall && !ModConfig.snowAccumulationDuringSnowstorm) || random.nextInt(8) > 0 || !worldIn.canSeeSky(pos.up())) {
			return;
		}

		boolean flag = false;
		if (worldIn.isRaining()) {
			if (ModConfig.snowAccumulationDuringSnowfall) {
				flag = true;
			} else if (ModConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering()) {
				flag = true;
			}
		}

		int layers = state.getValue(LAYERS);

		if (worldIn.canSnowAt(pos, false)) {
			if (flag) {
				accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.down()).getBlock() instanceof ModSnowBlock) && w.getLightFor(EnumSkyBlock.BLOCK, p) < 10, true);
			} else if (layers > 1 && !worldIn.isRaining() && worldIn.getBlockState(pos.up()).getBlock() != this) {
				accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.up()).getBlock() instanceof ModSnowBlock), false);
			}
		}
	}

	private static void accumulate(World world, BlockPos pos, IBlockState centerState, BiPredicate<World, BlockPos> filter, boolean accumulate) {
		int i = centerState.getValue(LAYERS);
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			EnumFacing direction = EnumFacing.byHorizontalIndex(k);
			BlockPos pos2 = pos.offset(direction);
			if (j % 2 == 1) {
				pos2 = pos2.offset(EnumFacing.byHorizontalIndex(k + 1));
			}
			if (!world.isBlockLoaded(pos2) || !filter.test(world, pos2)) {
				continue;
			}
			IBlockState state = world.getBlockState(pos2);
			boolean isAir = state.getBlock().isAir(state, world, pos2);
			int height = world.getPrecipitationHeight(pos2).getY();
			if (isAir || state.getBlock() == Blocks.SNOW_LAYER) {
				if (height != pos2.getY()) {
					continue;
				}
			} else if (ModSnowBlock.canContainState(state)) {
				if (height != pos2.getY() && height != pos2.getY() + 1) {
					continue;
				}
			} else {
				continue;
			}

			if (!Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos2)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof ModSnowBlock) {
				l = state.getValue(LAYERS);
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				i = l;
				pos = pos2;
				break;
			}
		}
		if (accumulate) {
			//			world.setBlockState(pos, Blocks.DIAMOND_BLOCK.getDefaultState());
			if (i < 8 && placeLayersOn(world, pos, 1, false, false, 3)) {
				AxisAlignedBB aabb = new AxisAlignedBB(pos);
				for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, aabb)) {
					entity.setPositionAndUpdate(entity.posX, entity.posY + (ModConfig.thinnerBoundingBox ? 0.0625D : 0.125D), entity.posZ);
				}
			}
		} else {
			world.setBlockState(pos, centerState.withProperty(LAYERS, i - 1));
		}
	}

	private boolean checkFallable(World worldIn, BlockPos pos, IBlockState state) {
		BlockPos posDown = pos.down();
		if ((worldIn.isAirBlock(posDown) || canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) && pos.getY() >= 0) {
			if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				if (!worldIn.isRemote) {
					worldIn.setBlockToAir(pos);
					FallingSnowEntity entityfallingblock = new FallingSnowEntity(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state.getValue(LAYERS));
					worldIn.spawnEntity(entityfallingblock);
				}
			} else {
				worldIn.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos.down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos), worldIn, blockpos)) && blockpos.getY() > 0; blockpos = blockpos.down()) {
				}

				if (blockpos.getY() > 0) {
					worldIn.setBlockState(blockpos.up(), state); //Forge: Fix loss of state information during world gen.
				}
			}
			return true;
		}
		return false;
	}

	public static boolean placeLayersOn(World world, BlockPos pos, int layers, boolean falling, boolean playSound, int flags) {
		if (!Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos)) {
			return false;
		}
		layers = MathHelper.clamp(layers, 1, 8);
		IBlockState state = world.getBlockState(pos);
		int originLayers = 0;
		IBlockState base;
		boolean flag = false;
		if (state.getBlock() == Blocks.SNOW_LAYER) {
			originLayers = state.getValue(LAYERS);
			base = state;
		} else {
			base = Blocks.SNOW_LAYER.getDefaultState();
			if (canContainState(state)) {
				flag = true;
				base = base.withProperty(ModSnowBlock.TILE, true);
			}
		}
		if (flag || state.getBlock() == Blocks.SNOW_LAYER || state.getBlock().isReplaceable(world, pos)) {
			world.setBlockState(pos, base.withProperty(LAYERS, MathHelper.clamp(originLayers + layers, 1, 8)), flags);
			if (flag) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile instanceof SnowTile) {
					((SnowTile) tile).setState(state);
				}
			}
			if (falling) {
				world.addBlockEvent(pos, Blocks.SNOW_LAYER, originLayers, layers);
			} else if (playSound) {
				SoundType soundtype = Blocks.SNOW_LAYER.getSoundType(state, world, pos, null);
				world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
			}
			if (originLayers + layers > 8) {
				pos = pos.up();
				if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
					world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(LAYERS, MathHelper.clamp(originLayers + layers - 8, 1, 8)), flags);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int originLayers, int layers) {
		double offsetY = originLayers / 8D;
		layers *= 10;
		for (int i = 0; i < layers; ++i) {
			double d0 = RANDOM.nextGaussian() * 0.2D;
			double d1 = RANDOM.nextGaussian() * 0.02D;
			double d2 = RANDOM.nextGaussian() * 0.2D;
			worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, pos.getX() + RANDOM.nextFloat(), pos.getY() + offsetY, pos.getZ() + RANDOM.nextFloat(), d0, d1, d2);
		}
		SoundType soundtype = getSoundType(state, worldIn, pos, null);
		worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		return true;
	}

	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		IBlockState state = worldIn.getBlockState(pos);
		if (state.getValue(LAYERS) == 8) {
			return false;
		}
		if (state.getBlock() == this && state.getValue(TILE)) {
			return getContainedState(worldIn, pos).getMaterial().isReplaceable();
		}
		return ModConfig.snowAlwaysReplaceable || super.isReplaceable(worldIn, pos);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!ModConfig.particleThroughLeaves || rand.nextInt(31) != 1) {
			return;
		}
		IBlockState stateDown = worldIn.getBlockState(pos.down());
		if (stateDown.getBlock().isLeaves(stateDown, worldIn, pos.down())) {
			double d0 = pos.getX() + rand.nextDouble();
			double d1 = pos.getY() - 0.05D;
			double d2 = pos.getZ() + rand.nextDouble();
			worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	public static boolean canContainState(IBlockState state) {
		if (!ModConfig.placeSnowInBlock || state.getBlock().hasTileEntity(state)) {
			return false;
		}
		Block block = state.getBlock();
		if (block instanceof BlockTallGrass || block instanceof BlockFlower || block instanceof BlockSapling || block instanceof BlockMushroom || block instanceof BlockDeadBush) {
			return true;
		}
		if (block instanceof BlockFence || block instanceof BlockWall || (block instanceof BlockPane && !(block instanceof BlockStainedGlassPane))) {
			return true;
		}
		if (NoTreePunchingCompat.isRock(block)) {
			return true;
		}
		return false;
	}

	public IBlockState getContainedState(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof SnowTile) {
			return ((SnowTile) tile).getState();
		}
		return Blocks.AIR.getDefaultState();
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		if (state.getValue(TILE)) {
			IBlockState stateIn = getContainedState(worldIn, pos);
			Block block = stateIn.getBlock();
			if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockPane) {
				return face == EnumFacing.UP ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
			}
			return stateIn.getBlockFaceShape(worldIn, pos, face);
		}
		return super.getBlockFaceShape(worldIn, state, pos, face);
	}

	@Override
	public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		return getContainedState(world, pos).getBlock().canBeConnectedTo(world, pos, facing);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
		if (ModConfig.placeSnowInBlock && state.getValue(TILE)) {
			getContainedState(worldIn, pos).addCollisionBoxToList(worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.getBlockState(pos).getMaterial() == Material.AIR && hasTileEntity(state)) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof SnowTile) {
				IBlockState newState = ((SnowTile) tile).getState();
				worldIn.removeTileEntity(pos);
				if (worldIn.setBlockState(pos, newState))
					newState.getBlock().neighborChanged(newState, worldIn, pos, newState.getBlock(), pos.down());
			} else {
				worldIn.removeTileEntity(pos);
			}
		}
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		if (ModConfig.placeSnowInBlock) {
			return getContainedState(worldIn, pos).getBlock().isPassable(worldIn, pos);
		}
		return super.isPassable(worldIn, pos);
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state) {
		return hasTileEntity(state) ? EnumPushReaction.BLOCK : EnumPushReaction.DESTROY;
	}

	public static boolean canFallThrough(IBlockState state, World worldIn, BlockPos pos) {
		return state.getCollisionBoundingBox(worldIn, pos) == null;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		if ((layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.CUTOUT_MIPPED) && state.getValue(TILE))
			return true;
		return layer == BlockRenderLayer.SOLID;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (ModConfig.placeSnowInBlock) {
			if (state.getValue(TILE)) {
				IBlockState contained = getContainedState(worldIn, pos);
				if (NoTreePunchingCompat.isRock(contained.getBlock())) {
					try {
						if (contained.getBlock().onBlockActivated(worldIn, pos, contained, playerIn, hand, facing, hitX, hitY, hitZ)) {
							return worldIn.setBlockState(pos, state.withProperty(TILE, false));
						}
					} catch (Exception e) {
					}
				}
			} else {
				ItemStack stack = playerIn.getHeldItem(hand);
				Block block = Block.getBlockFromItem(stack.getItem());
				if (block != null && block != Blocks.AIR) {
					IBlockState state2 = block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, stack.getMetadata(), playerIn, hand);
					if (state2 != null && canContainState(state2) && state2.getBlock().canPlaceBlockOnSide(worldIn, pos, EnumFacing.UP)) {
						if (!worldIn.isRemote) {
							worldIn.setBlockState(pos, state2, 16 | 32);
							int i = state.getValue(LAYERS);
							if (placeLayersOn(worldIn, pos, i, false, true, 3) && !playerIn.isCreative()) {
								stack.shrink(1);
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer player) {
		if (worldIn.isRemote)
			return;
		IBlockState state = worldIn.getBlockState(pos);
		if (ModConfig.placeSnowInBlock && state.getValue(TILE)) {
			try {
				IBlockState contained = getContainedState(worldIn, pos);
				if (contained.getBlockHardness(worldIn, pos) == 0) {
					worldIn.playEvent(2001, pos, Block.getStateId(contained));
					contained.getBlock().harvestBlock(worldIn, player, pos, contained, null, player.getHeldItemMainhand());
					worldIn.setBlockState(pos, state.withProperty(TILE, false));
				}
			} catch (Exception e) {
			}
		}
	}
}
