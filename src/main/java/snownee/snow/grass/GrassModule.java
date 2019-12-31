package snownee.snow.grass;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.MyceliumBlock;
import net.minecraft.tags.Tag;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.snow.SnowRealMagic;

@KiwiModule(name = "fix_grass_block")
@KiwiModule.Optional
@KiwiModule.Group("building_blocks")
public class GrassModule extends AbstractModule {
    public static final Tag<Block> BOTTOM_SNOW = blockTag(SnowRealMagic.MODID, "bottom_snow");

    @RenderLayer(Layer.CUTOUT_MIPPED)
    @Name("minecraft:grass_block")
    public static final GrassBlock GRASS_BLOCK = new ModGrassBlock(blockProp(Blocks.GRASS_BLOCK));

    @Name("minecraft:mycelium")
    public static final MyceliumBlock MYCELIUM = new ModMyceliumBlock(blockProp(Blocks.MYCELIUM));
}
