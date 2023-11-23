package com.github.alexthe666.iceandfire.world.gen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public class MixinGameOptions {
    @Inject(
            method = "<init>",
            at = @At(value = "RETURN")
    )
    private void $GameOptions(Minecraft pMinecraft, File pGameDirectory, CallbackInfo ci) {
        if (pMinecraft.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            Option.RENDER_DISTANCE.setMaxValue(64f);
            Option.SIMULATION_DISTANCE.setMaxValue(64f);
        }
    }
}
