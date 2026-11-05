package de.mcqw.forge;

import de.mcqw.core.McqwIds;
import de.mcqw.forge.config.ForgeConfigs;
import de.mcqw.forge.item.ForgeCrystalStaffItem;
import de.mcqw.forge.network.ModNetwork;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.registry.McqwObjects;
import de.mcqw.mc.staff.StaffData;
import de.mcqw.mc.staff.StaffShield;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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

    public McqwMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ITEMS.register(modBus);
        context.registerConfig(ModConfig.Type.SERVER, ForgeConfigs.SERVER);
        modBus.addListener(McqwMod::onBuildContents);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ModNetwork::register));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) StaffShield.tick(event.getServer());
        });
        MinecraftForge.EVENT_BUS.addListener((LivingAttackEvent event) -> {
            if (StaffShield.absorb(event.getEntity(), event.getSource(), event.getAmount())) event.setCanceled(true);
        });
    }

    private static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CRYSTAL_STAFF);
            ItemStack charged = new ItemStack(CRYSTAL_STAFF.get());
            StaffData.setSavedEnergy(charged, Integer.MAX_VALUE);
            event.accept(charged);
        }
    }
}
