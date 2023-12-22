package snownee.snow.mixin.fabric;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.entity.RenderData;
import snownee.snow.block.entity.SnowBlockEntity;

@Mixin(value = SnowBlockEntity.class, remap = false)
public abstract class SnowBlockEntityMixin implements RenderDataBlockEntity {

	@Unique
	private RenderData renderData;
	@Shadow
	private SnowBlockEntity.Options options;
	@Shadow
	private BlockState state;

	@Override
	public @Nullable RenderData getRenderData() {
		if (renderData == null || renderData.state() != state) {
			renderData = new RenderData(state, options);
		}
		return renderData;
	}

}
