package cf.litetech.litebotmod.mixins;

import cf.litetech.litebotmod.LiteBotMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(at = @At("RETURN"), method = "loadWorld")
    public void loadWorld(CallbackInfo ci) {
        LiteBotMod.setServer((MinecraftServer) (Object) this);
        LiteBotMod.getDispatcher().onServerStart();
    }

    @Inject(at = @At("RETURN"), method = "shutdown")
    public void shutDown(CallbackInfo ci) {
        LiteBotMod.getDispatcher().onServerStop();
        if (LiteBotMod.getConnection().isOpen()) {
            LiteBotMod.getConnection().close();
        }
    }

    @Inject(at = @At("RETURN"), method = "save")
    public void connect(boolean suppressLogs, boolean bl, boolean bl2, CallbackInfoReturnable<Boolean> cir) {
        if (LiteBotMod.getConnection().isClosed()) {
            LiteBotMod.getConnection().reconnect();
        }
    }

    @Inject(at = @At(
            value = "CONSTANT",
            args = "stringValue=tallying"
    ), method = "tick")
    public void dispatchDick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        LiteBotMod.getDispatcher().onTick();
    }
}
