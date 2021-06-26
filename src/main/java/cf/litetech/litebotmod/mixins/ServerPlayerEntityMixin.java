package cf.litetech.litebotmod.mixins;

import cf.litetech.litebotmod.LiteBotMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin{

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathEvent(DamageSource source, CallbackInfo ci) {
        LiteBotMod.getDispatcher().onPlayerDeath((ServerPlayerEntity) (Object) this);
    }
}
