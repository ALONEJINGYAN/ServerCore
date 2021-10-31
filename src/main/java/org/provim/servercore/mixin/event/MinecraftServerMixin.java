package org.provim.servercore.mixin.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.objectweb.asm.Opcodes;
import org.provim.servercore.ServerCore;
import org.provim.servercore.config.Config;
import org.provim.servercore.config.tables.FeatureConfig;
import org.provim.servercore.utils.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    private int ticks;

    /**
     * [Server Tick Event]
     */

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (this.ticks % 300 == 0) {
            TickManager.runPerformanceChecks((MinecraftServer) (Object) this);
        }
    }

    /**
     * [Server Startup Event]
     */

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
    private void onSetupServer(CallbackInfo info) {
        ServerCore.setServer((MinecraftServer) (Object) this);
        Config.load();
    }

    @Inject(at = @At("HEAD"), method = "prepareStartRegion", cancellable = true)
    private void disableSpawnChunks(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        if (FeatureConfig.DISABLE_SPAWN_CHUNKS.get()) {
            ci.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/MinecraftServer;ticks:I", ordinal = 1))
    public int modifyAutoSaveInterval(MinecraftServer minecraftServer) {
        return this.ticks % (FeatureConfig.AUTO_SAVE_INTERVAL.get() * 1200) == 0 ? 6000 : -1;
    }
}
