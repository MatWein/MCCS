package de.mccs.mc.mixin;

import de.mccs.mc.staff.StaffLightning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lightning charges the staff (Forge 1.20.1: EntityStruckByLightningEvent). */
@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "thunderHit", at = @At("HEAD"))
    private void mccs$charge(ServerLevel level, LightningBolt bolt, CallbackInfo callback) {
        StaffLightning.struck((Entity) (Object) this);
    }
}
