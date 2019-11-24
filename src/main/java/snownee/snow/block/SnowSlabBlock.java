package snownee.snow.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.tile.TextureTile;
import snownee.kiwi.util.Util;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;

public class SnowSlabBlock extends ModBlock implements ISnowVariant {
    protected static final VoxelShape BOTTOM_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape BOTTOM_RENDER_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public SnowSlabBlock(Properties builder) {
        super(builder);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SnowTextureTile();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return true;
        }

        TileEntity tile = worldIn.getTileEntity(pos);
        ItemStack stack = player.getHeldItem(handIn);
        if (hit.getFace() == Direction.UP && tile instanceof TextureTile && ((TextureTile) tile).getMark("0") == stack.getItem() && stack.getItem() instanceof BlockItem && stack.getItem().isIn(ItemTags.SLABS)) {
            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (block instanceof SlabBlock) {
                BlockState state2 = block.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
                worldIn.setBlockState(pos, state2);
                return true;
            }
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return BOTTOM_SHAPE;
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return BOTTOM_RENDER_SHAPE;
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String key = Util.getTextureItem(stack, "0");
        if (!key.isEmpty()) {
            tooltip.add(new TranslationTextComponent(key).applyTextStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        MainModule.fillTextureItems(ItemTags.SLABS, this, items);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!SnowCommonConfig.snowNeverMelt && worldIn.getLightFor(LightType.BLOCK, pos) > 11) {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        }
    }

}
