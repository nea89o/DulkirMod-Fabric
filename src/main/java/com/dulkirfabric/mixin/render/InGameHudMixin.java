package com.dulkirfabric.mixin.render;

import com.dulkirfabric.config.DulkirConfig;
import com.dulkirfabric.util.Utils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.scoreboard.number.NumberFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(
            method = "renderStatusEffectOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onRenderStatusEffectOverlay(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (DulkirConfig.ConfigVars.getConfigOptions().getStatusEffectHidden()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/ScoreboardObjective;getNumberFormatOr(Lnet/minecraft/scoreboard/number/NumberFormat;)Lnet/minecraft/scoreboard/number/NumberFormat;"
            )
    )
    public NumberFormat removeScoreBoardNumbers(NumberFormat original) {
        return BlankNumberFormat.INSTANCE;
    }

    @Inject(
            method = "renderArmor",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onGrabArmorAmount(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (DulkirConfig.ConfigVars.getConfigOptions().getHideArmorOverlay() && Utils.INSTANCE.isInSkyblock()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"
            )
    )
    public int onCheckForRiding(int original) {
        if (DulkirConfig.ConfigVars.getConfigOptions().getHideHungerOverlay() && Utils.INSTANCE.isInSkyblock())
            return 1;
        return original;
    }

    @Inject(
            method = "renderHeldItemTooltip",
            at = @At("HEAD"),
            cancellable = true
    )
    public void changeItemDisplay (DrawContext context, CallbackInfo ci) {
        if (DulkirConfig.ConfigVars.getConfigOptions().getHideHeldItemTooltip())
            ci.cancel();
    }
}
