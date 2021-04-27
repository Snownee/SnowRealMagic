package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kiwi.tile.TextureTile;
import snownee.snow.CoreModule;

public class SnowTextureTile extends TextureTile {

	public SnowTextureTile() {
		super(CoreModule.TEXTURE_TILE, "0");
	}

	@Override
	public boolean isMark(String key) {
		return "0".equals(key);
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		readPacketData(compound);
		super.read(state, compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		writePacketData(compound);
		return super.write(compound);
	}

}
