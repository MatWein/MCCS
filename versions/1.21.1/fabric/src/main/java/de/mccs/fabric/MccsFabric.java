package de.mccs.fabric;

import de.mccs.core.MccsIds;
import de.mccs.core.staff.StaffKind;
import de.mccs.fabric.compat.TeamRebornEnergy;
import de.mccs.fabric.config.FabricConfigs;
import de.mccs.fabric.item.FabricCrystalStaffItem;
import de.mccs.fabric.network.FabricNetwork;
import de.mccs.mc.Mccs;
import de.mccs.mc.entity.EnergyBallEntity;
import de.mccs.mc.item.StaffStacks;
import de.mccs.mc.registry.MccsObjects;
import de.mccs.mc.staff.StaffLights;
import de.mccs.mc.staff.StaffSystems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class MccsFabric implements ModInitializer {
    private static final String TEAM_REBORN_ENERGY = "team_reborn_energy";

    @Override
    public void onInitialize() {
        FabricConfigs.define();
        MccsObjects.COMPONENTS.forEach((name, type) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Mccs.id(name), type));
        List<Item> staffs = new ArrayList<>();
        for (StaffKind kind : StaffKind.values()) {
            Item staff = new FabricCrystalStaffItem(MccsObjects.crystalStaffProperties(), kind);
            Registry.register(BuiltInRegistries.ITEM, Mccs.id(kind.id()), staff);
            staffs.add(staff);
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(output -> {
            for (Item staff : staffs) {
                output.accept(staff);
                output.accept(StaffStacks.fullyCharged(staff), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY); // the search would show the staff twice
            }
        });
        EnergyBallEntity.TYPE = MccsObjects.energyBallType();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mccs.id(MccsIds.ENERGY_BALL), EnergyBallEntity.TYPE);
        FabricNetwork.register();
        ServerTickEvents.END_SERVER_TICK.register(StaffSystems::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> StaffLights.removeAll());
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            StaffSystems.onAttack(player);
            return InteractionResult.PASS;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !StaffSystems.absorb(entity, source, amount));
        if (FabricLoader.getInstance().isModLoaded(TEAM_REBORN_ENERGY)) TeamRebornEnergy.register(staffs);
    }
}
