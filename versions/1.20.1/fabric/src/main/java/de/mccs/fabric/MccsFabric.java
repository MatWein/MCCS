package de.mccs.fabric;

import de.mccs.core.MccsIds;
import de.mccs.fabric.compat.TeamRebornEnergy;
import de.mccs.fabric.config.FabricConfigs;
import de.mccs.fabric.item.FabricCrystalStaffItem;
import de.mccs.fabric.network.FabricNetwork;
import de.mccs.mc.Mccs;
import de.mccs.mc.entity.EnergyBallEntity;
import de.mccs.mc.registry.MccsObjects;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffLightning;
import de.mccs.mc.staff.StaffLights;
import de.mccs.mc.staff.StaffSystems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** MCCS for Minecraft 1.20.1 on Fabric. */
public final class MccsFabric implements ModInitializer {
    private static final String TEAM_REBORN_ENERGY = "team_reborn_energy";
    private static final Item CRYSTAL_STAFF = new FabricCrystalStaffItem(MccsObjects.crystalStaffProperties());

    @Override
    public void onInitialize() {
        FabricConfigs.define();
        Registry.register(BuiltInRegistries.ITEM, Mccs.id(MccsIds.CRYSTAL_STAFF), CRYSTAL_STAFF);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(CRYSTAL_STAFF);
            ItemStack charged = new ItemStack(CRYSTAL_STAFF);
            StaffData.setSavedEnergy(charged, Integer.MAX_VALUE);
            entries.accept(charged, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY); // the search would show the staff twice
        });
        EnergyBallEntity.TYPE = MccsObjects.energyBallType();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mccs.id(MccsIds.ENERGY_BALL), EnergyBallEntity.TYPE);
        FabricNetwork.registerServer();
        ServerTickEvents.END_SERVER_TICK.register(StaffSystems::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> StaffLights.removeAll());
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            StaffSystems.onAttack(player);
            return InteractionResult.PASS;
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof LightningBolt bolt) StaffLightning.attract(bolt);
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !StaffSystems.absorb(entity, source, amount));
        if (FabricLoader.getInstance().isModLoaded(TEAM_REBORN_ENERGY)) TeamRebornEnergy.register(CRYSTAL_STAFF);
    }
}
