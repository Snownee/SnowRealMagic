package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import snownee.snow.Hooks;

@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

	@WrapOperation(
			method = "place",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean srm_place(
			Biome biome,
			LevelReader level,
			BlockPos pos,
			Operation<Boolean> original,
			@Local(argsOnly = true) FeaturePlaceContext<NoneFeatureConfiguration> context,
			@Local(name = "belowPos") BlockPos.MutableBlockPos belowPos) {
		boolean result = original.call(biome, level, pos);
		if (!result) {
			Hooks.placeFeatureExtra(biome, context.level(), pos, belowPos);
		}
		return result;
	}

}
