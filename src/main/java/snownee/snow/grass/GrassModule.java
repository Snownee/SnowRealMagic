package snownee.snow.grass;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.MyceliumBlock;
import net.minecraft.tags.Tag;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;
import snownee.snow.SnowRealMagic;

//@KiwiModule(name = "fix_grass_block")
@KiwiModule.Optional
@KiwiModule.Group("building_blocks")
public class GrassModule extends AbstractModule {
    public static final Tag<Block> BOTTOM_SNOW = blockTag(SnowRealMagic.MODID, "bottom_snow");

    @Name("minecraft:grass_block")
    public static final GrassBlock GRASS_BLOCK = new ModGrassBlock(Block.Properties.from(Blocks.GRASS_BLOCK));

    @Name("minecraft:mycelium")
    public static final MyceliumBlock MYCELIUM = new ModMyceliumBlock(Block.Properties.from(Blocks.MYCELIUM));
}
