package de.mcqw.fabric;

import de.mcqw.core.McqwIds;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.item.CrystalStaffItem;
import de.mcqw.mc.registry.McqwObjects;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** MCQW for Minecraft 26.3 on Fabric. */
public final class McqwFabric implements ModInitializer {
    private static final Item CRYSTAL_STAFF = new CrystalStaffItem(McqwObjects.crystalStaffProperties());

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, Mcqw.id(McqwIds.CRYSTAL_STAFF), CRYSTAL_STAFF);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> output.accept(CRYSTAL_STAFF));
    }
}
