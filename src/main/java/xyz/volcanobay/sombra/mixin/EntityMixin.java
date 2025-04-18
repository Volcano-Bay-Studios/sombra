package xyz.volcanobay.sombra.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.volcanobay.sombra.Utils;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract void tick();

    @Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
    public void shouldBroadcast(ServerPlayer observer, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        ServerPlayer actor;
        if (self instanceof ServerPlayer player) {
            actor = player;
        } else if (self instanceof TraceableEntity traceableEntity && traceableEntity.getOwner() instanceof ServerPlayer owner) {
            actor = owner;
        } else {
            return;
        }
        if (Utils.isInvisible(actor,observer)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void markRenderInvisible(Player player, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player observer) {
            if (Utils.isInvisible(player,observer)) {
                cir.setReturnValue(true);
            }
        }
    }
}
