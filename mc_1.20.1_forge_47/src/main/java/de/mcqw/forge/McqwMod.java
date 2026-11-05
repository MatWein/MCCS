package de.mcqw.forge;

import de.mcqw.core.McqwIds;
import de.mcqw.forge.item.ForgeCrystalStaffItem;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.registry.McqwObjects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
        modBus.addListener(McqwMod::onBuildContents);
    }

    private static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CRYSTAL_STAFF);
        }
    }
}
