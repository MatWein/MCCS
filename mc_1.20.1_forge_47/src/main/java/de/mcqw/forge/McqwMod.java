package de.mcqw.forge;

import de.mcqw.core.McqwIds;
import de.mcqw.forge.config.ForgeConfigs;
import de.mcqw.forge.item.ForgeCrystalStaffItem;
import de.mcqw.forge.network.ModNetwork;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.entity.EnergyBallEntity;
import de.mcqw.mc.registry.McqwObjects;
import de.mcqw.mc.staff.StaffData;
import de.mcqw.mc.staff.StaffLightning;
import de.mcqw.mc.staff.StaffSystems;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** MCQW for Minecraft 1.20.1 on Forge 47. */
@Mod(Mcqw.MOD_ID)
public final class McqwMod {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mcqw.MOD_ID);

    private static final RegistryObject<Item> CRYSTAL_STAFF = ITEMS.register(McqwIds.CRYSTAL_STAFF,
            () -> new ForgeCrystalStaffItem(McqwObjects.crystalStaffProperties()));

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Mcqw.MOD_ID);

    static {
        ENTITY_TYPES.register(McqwIds.ENERGY_BALL, () -> EnergyBallEntity.TYPE = McqwObjects.energyBallType());
    }

    public McqwMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        context.registerConfig(ModConfig.Type.SERVER, ForgeConfigs.SERVER);
        modBus.addListener(McqwMod::onBuildContents);
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
            event.accept(CRYSTAL_STAFF);
            ItemStack charged = new ItemStack(CRYSTAL_STAFF.get());
            StaffData.setSavedEnergy(charged, Integer.MAX_VALUE);
            event.accept(charged, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY); // the search would show the staff twice
        }
    }
}
