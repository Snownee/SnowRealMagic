package snownee.snow.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.Half;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.Util;
import snownee.snow.MainModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;

@RenderLayer(Layer.CUTOUT)
public class SnowStairsBlock extends StairsBlock implements IWaterLoggableSnowVariant {

    @SuppressWarnings("deprecation")
    public SnowStairsBlock(Properties properties) {
        super(Blocks.STONE.getDefaultState(), properties);
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
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return ModBlock.pickBlock(state, target, world, pos, player);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
            worldIn.removeTileEntity(pos);
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String key = Util.getTextureItem(stack, "0");
        if (!key.isEmpty()) {
            tooltip.add(new TranslationTextComponent(key).mergeStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        MainModule.fillTextureItems(ItemTags.STAIRS, this, items);
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (SnowCommonConfig.retainOriginalBlocks) {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        } else if (ModUtil.shouldMelt(worldIn, pos)) {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).with(HALF, Half.BOTTOM);
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
        return getRaw(state, world, pos).getSoundType(world, pos, entity);
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
    }
}
