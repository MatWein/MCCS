package de.mccs.fabric.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import de.mccs.mc.client.KeyBindings;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Minecraft 1.20.1 allows one binding per key, and vanilla already uses C ("save hotbar") L (advancements) and the middle mouse button (pick block): the staff's keys also count
 * when another binding got their key, and that binding keeps working (Forge and Minecraft 26.3 handle shared keys themselves).
 */
@Mixin(KeyMapping.class)
abstract class KeyMappingMixin {
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    @Shadow private InputConstants.Key key;
    @Shadow private int clickCount;

    @Inject(method = "click", at = @At("HEAD"))
    private static void mccs$clickStaffKeys(InputConstants.Key pressed, CallbackInfo callback) {
        for (KeyMapping staffKey : List.of(KeyBindings.SHIELD, KeyBindings.CLOAK, KeyBindings.TELEPORT, KeyBindings.LIGHTS, KeyBindings.BLOCK_DAMAGE, KeyBindings.TELEKINESIS)) {
            KeyMappingMixin mapping = (KeyMappingMixin) (Object) staffKey;
            if (mapping.key.equals(pressed) && MAP.get(pressed) != staffKey) mapping.clickCount++;
        }
    }
}
