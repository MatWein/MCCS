package de.mcqw.neoforge;

import de.mcqw.core.McqwIds;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.item.CrystalStaffItem;
import de.mcqw.mc.registry.McqwObjects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** MCQW for Minecraft 26.3 on NeoForge. */
@Mod(Mcqw.MOD_ID)
public final class McqwNeoForge {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Mcqw.MOD_ID);

    private static final DeferredHolder<Item, Item> CRYSTAL_STAFF = ITEMS.register(McqwIds.CRYSTAL_STAFF,
            () -> new CrystalStaffItem(McqwObjects.crystalStaffProperties()));

    public McqwNeoForge(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(McqwNeoForge::onBuildContents);
    }

    private static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CRYSTAL_STAFF.get());
        }
    }
}
