package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;

@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

	@WrapOperation(
			method = "place",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z"))
	private boolean srm_placeIce(
			Biome biome,
			LevelReader levelReader,
			BlockPos pos,
			boolean checkCanSurvive,
			Operation<Boolean> original) {
		boolean result = original.call(biome, levelReader, pos, checkCanSurvive);
		if (!result && SnowCommonConfig.worldGenSnowAndFreezeEverywhere) {
			result = Hooks.shouldFreezeIgnoringTemperature(levelReader, pos);
		}
		return result;
	}

	@WrapOperation(
			method = "place",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean srm_place(
			Biome biome,
			LevelReader levelReader,
			BlockPos pos,
			Operation<Boolean> original,
			@Local WorldGenLevel level,
			@Local(ordinal = 1) BlockPos.MutableBlockPos belowPos) {
		boolean result = original.call(biome, levelReader, pos);
		if (!result && SnowCommonConfig.worldGenSnowAndFreezeEverywhere) {
			result = Hooks.shouldSnowIgnoringTemperature(levelReader, pos);
		}
		if (!result) {
			Hooks.placeFeatureExtra(biome, level, pos, belowPos);
		}
		return result;
	}

}
