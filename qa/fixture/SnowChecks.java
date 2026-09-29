package srm.qa;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;

/** Runs only in a disposable test world, never bundled in the production mod. */
public final class SnowChecks implements ModInitializer {
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> check(server, "startup"));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            require(success, "datapack reload");
            check(server, "reload");
        });
    }

    private static void check(MinecraftServer server, String phase) {
        var level = server.overworld();
        var pos = new BlockPos(0, 120, 0);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        for (Block original : new Block[] {Blocks.OAK_FENCE, Blocks.COBBLESTONE_WALL, Blocks.OAK_SLAB, Blocks.OAK_STAIRS}) {
            level.setBlockAndUpdate(pos, original.defaultBlockState());
            require(Hooks.convert(level, pos, original.defaultBlockState(), 2, Block.UPDATE_ALL, true),
                "convert " + original);
            BlockState snowy = level.getBlockState(pos);
            require(snowy.getBlock() instanceof SnowVariant, "snow variant " + original);
            require(level.getBlockEntity(pos) instanceof SnowBlockEntity, "snow block entity " + original);
            var entity = (SnowBlockEntity) level.getBlockEntity(pos);
            require(entity.getContainedState().is(original), "contained block " + original);
            if (snowy.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
                require(((SnowVariant) snowy.getBlock()).srm$layers(snowy, level, pos) == 2, "two layers " + original);
            } else if (original == Blocks.OAK_SLAB) {
                require(((SnowVariant) snowy.getBlock()).srm$getSnowState(snowy, level, pos).is(Blocks.SNOW), "slab snow overlay");
            } else {
                require(((SnowVariant) snowy.getBlock()).srm$canRenderDecoration(snowy), "stair snow decoration");
            }
            var drops = Block.getDrops(snowy, level, pos, entity, null, new ItemStack(Items.DIAMOND_PICKAXE));
            require(drops.stream().anyMatch(stack -> stack.is(original.asItem())), "contained-block loot " + original);
        }
        BlockPos saplingOrigin = new BlockPos(20, 120, 0);
        for (BlockPos ceiling : BlockPos.betweenClosed(16, 122, -4, 25, 122, 5)) {
            level.setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
        }
        for (BlockPos sapling : BlockPos.betweenClosed(saplingOrigin, saplingOrigin.offset(1, 0, 1))) {
            level.setBlockAndUpdate(sapling.below(), Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(sapling, Blocks.DARK_OAK_SAPLING.defaultBlockState());
            require(Hooks.convert(level, sapling, Blocks.DARK_OAK_SAPLING.defaultBlockState(), 2, Block.UPDATE_ALL, true), "snowy sapling");
        }
        boolean grown = net.minecraft.world.level.block.grower.TreeGrower.DARK_OAK.growTree(level,
                level.getChunkSource().getGenerator(), saplingOrigin, Blocks.DARK_OAK_SAPLING.defaultBlockState(),
                net.minecraft.util.RandomSource.create(1));
        require(!grown, "blocked tree must fail to grow");
        for (BlockPos sapling : BlockPos.betweenClosed(saplingOrigin, saplingOrigin.offset(1, 0, 1))) {
            require(level.getBlockEntity(sapling) instanceof SnowBlockEntity, "failed tree restores snow entity");
            require(((SnowBlockEntity) level.getBlockEntity(sapling)).getContainedState().is(Blocks.DARK_OAK_SAPLING), "failed tree restores sapling");
            BlockState snowySapling = level.getBlockState(sapling);
            require(((SnowVariant) snowySapling.getBlock()).srm$layers(snowySapling, level, sapling) == 2, "failed tree restores layers");
        }
        for (BlockPos floor : BlockPos.betweenClosed(-5, 119, -5, 10, 119, 8)) {
            level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        }
        int x = 0;
        for (Block original : new Block[] {Blocks.OAK_FENCE, Blocks.COBBLESTONE_WALL, Blocks.OAK_SLAB, Blocks.OAK_STAIRS}) {
            BlockPos exhibit = new BlockPos(x, 120, 0);
            level.setBlockAndUpdate(exhibit, original.defaultBlockState());
            require(Hooks.convert(level, exhibit, original.defaultBlockState(), 2, Block.UPDATE_ALL, true), "render exhibit");
            x += 2;
        }
        System.out.println("SRM_QA_PASS " + phase + " conversion/containment/layers/loot and failed 2x2 sapling restoration");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError("SRM_QA_FAIL " + message);
    }
}
