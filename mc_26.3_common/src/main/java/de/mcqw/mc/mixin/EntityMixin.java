package de.mcqw.mc.mixin;

import de.mcqw.mc.staff.StaffLightning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lightning charges the staff (Forge: EntityStruckByLightningEvent). */
@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "thunderHit", at = @At("HEAD"))
    private void mcqw$charge(ServerLevel level, LightningBolt bolt, CallbackInfo callback) {
        StaffLightning.struck((Entity) (Object) this);
    }
}
