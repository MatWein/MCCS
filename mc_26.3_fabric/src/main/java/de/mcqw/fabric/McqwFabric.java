package de.mcqw.fabric;

import de.mcqw.core.McqwIds;
import de.mcqw.fabric.compat.TeamRebornEnergy;
import de.mcqw.fabric.config.FabricConfigs;
import de.mcqw.fabric.item.FabricCrystalStaffItem;
import de.mcqw.fabric.network.FabricNetwork;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.entity.EnergyBallEntity;
import de.mcqw.mc.registry.McqwObjects;
import de.mcqw.mc.staff.StaffData;
import de.mcqw.mc.staff.StaffSystems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** MCQW for Minecraft 26.3 on Fabric. */
public final class McqwFabric implements ModInitializer {
    private static final String TEAM_REBORN_ENERGY = "team_reborn_energy";

    @Override
    public void onInitialize() {
        FabricConfigs.define();
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Mcqw.id("energy"), McqwObjects.ENERGY);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Mcqw.id("shield_on"), McqwObjects.SHIELD_ON);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Mcqw.id("cloak_on"), McqwObjects.CLOAK_ON);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Mcqw.id("shield_hits"), McqwObjects.SHIELD_HITS);
        Item staff = new FabricCrystalStaffItem(McqwObjects.crystalStaffProperties());
        Registry.register(BuiltInRegistries.ITEM, Mcqw.id(McqwIds.CRYSTAL_STAFF), staff);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
            output.accept(staff);
            ItemStack charged = new ItemStack(staff);
            StaffData.setSavedEnergy(charged, Integer.MAX_VALUE);
            output.accept(charged, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY); // the search would show the staff twice
        });
        EnergyBallEntity.TYPE = McqwObjects.energyBallType();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mcqw.id(McqwIds.ENERGY_BALL), EnergyBallEntity.TYPE);
        FabricNetwork.register();
        ServerTickEvents.END_SERVER_TICK.register(StaffSystems::tick);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            StaffSystems.onAttack(player);
            return InteractionResult.PASS;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !StaffSystems.absorb(entity, source, amount));
        if (FabricLoader.getInstance().isModLoaded(TEAM_REBORN_ENERGY)) TeamRebornEnergy.register(staff);
    }
}
