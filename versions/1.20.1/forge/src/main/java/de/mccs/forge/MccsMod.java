package de.mccs.forge;

import de.mccs.core.MccsIds;
import de.mccs.core.staff.StaffKind;
import de.mccs.forge.config.ForgeConfigs;
import de.mccs.forge.item.ForgeCrystalStaffItem;
import de.mccs.forge.network.ModNetwork;
import de.mccs.mc.Mccs;
import de.mccs.mc.entity.EnergyBallEntity;
import de.mccs.mc.registry.MccsObjects;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffLightning;
import de.mccs.mc.staff.StaffLights;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.List;

/** MCCS for Minecraft 1.20.1 on Forge 47. */
@Mod(Mccs.MOD_ID)
public final class MccsMod {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mccs.MOD_ID);

    /** One staff per kind, in the order of {@link StaffKind}. */
    private static final List<RegistryObject<Item>> STAFFS = Arrays.stream(StaffKind.values())
            .map(kind -> ITEMS.<Item>register(kind.id(), () -> new ForgeCrystalStaffItem(MccsObjects.crystalStaffProperties(), kind)))
            .toList();

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Mccs.MOD_ID);

    static {
        ENTITY_TYPES.register(MccsIds.ENERGY_BALL, () -> EnergyBallEntity.TYPE = MccsObjects.energyBallType());
    }

    public MccsMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        context.registerConfig(ModConfig.Type.SERVER, ForgeConfigs.SERVER);
        modBus.addListener(MccsMod::onBuildContents);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ModNetwork::register));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) StaffSystems.tick(event.getServer());
        });
        MinecraftForge.EVENT_BUS.addListener((LivingAttackEvent event) -> {
            if (StaffSystems.absorb(event.getEntity(), event.getSource(), event.getAmount())) event.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((AttackEntityEvent event) -> StaffSystems.onAttack(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> {
            if (event.getEntity() instanceof LightningBolt bolt) StaffLightning.attract(bolt);
        });
        MinecraftForge.EVENT_BUS.addListener((EntityStruckByLightningEvent event) -> StaffLightning.struck(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> StaffLights.removeAll());
        MinecraftForge.EVENT_BUS.addListener((LivingFallEvent event) -> {
            if (StaffSystems.absorbFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier())) event.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((LivingEvent.LivingVisibilityEvent event) -> {
            if (StaffSystems.isCloaked(event.getEntity())) event.modifyVisibility(0.0);
        });
        MinecraftForge.EVENT_BUS.addListener((LivingChangeTargetEvent event) -> {
            if (StaffSystems.isCloaked(event.getNewTarget())) event.setCanceled(true);
        });
    }

    private static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (RegistryObject<Item> staff : STAFFS) {
                event.accept(staff);
                ItemStack charged = new ItemStack(staff.get());
                StaffData.setSavedEnergy(charged, Integer.MAX_VALUE);
                event.accept(charged, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY); // the search would show the staff twice
            }
        }
    }
}
