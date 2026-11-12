package de.mccs.neoforge;

import de.mccs.core.MccsIds;
import de.mccs.core.staff.StaffKind;
import de.mccs.mc.Mccs;
import de.mccs.mc.entity.EnergyBallEntity;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.network.StaffActionPacket;
import de.mccs.mc.registry.MccsObjects;
import de.mccs.mc.staff.StaffBeam;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffLights;
import de.mccs.mc.staff.StaffSystems;
import de.mccs.neoforge.config.NeoForgeConfigs;
import de.mccs.neoforge.item.NeoForgeCrystalStaffItem;
import de.mccs.neoforge.item.StaffEnergyHandler;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.List;

/** MCCS for Minecraft 26.3 on NeoForge. */
@Mod(Mccs.MOD_ID)
public final class MccsNeoForge {
    private static final String NETWORK_PROTOCOL = "2";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Mccs.MOD_ID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Mccs.MOD_ID);

    /** One staff per kind, in the order of {@link StaffKind}. */
    private static final List<DeferredHolder<Item, Item>> STAFFS = Arrays.stream(StaffKind.values())
            .map(kind -> ITEMS.<Item>register(kind.id(), () -> new NeoForgeCrystalStaffItem(MccsObjects.crystalStaffProperties(kind), kind)))
            .toList();

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Mccs.MOD_ID);

    static {
        ENTITY_TYPES.register(MccsIds.ENERGY_BALL, () -> EnergyBallEntity.TYPE = MccsObjects.energyBallType());
        COMPONENTS.register("energy", () -> MccsObjects.ENERGY);
        COMPONENTS.register("shield_on", () -> MccsObjects.SHIELD_ON);
        COMPONENTS.register("cloak_on", () -> MccsObjects.CLOAK_ON);
        COMPONENTS.register("lights_on", () -> MccsObjects.LIGHTS_ON);
        COMPONENTS.register("block_damage", () -> MccsObjects.BLOCK_DAMAGE);
        COMPONENTS.register("shield_hits", () -> MccsObjects.SHIELD_HITS);
    }

    public MccsNeoForge(IEventBus modBus, ModContainer container) {
        ITEMS.register(modBus);
        COMPONENTS.register(modBus);
        ENTITY_TYPES.register(modBus);
        container.registerConfig(ModConfig.Type.SYNCED, NeoForgeConfigs.SERVER, "mccs-server.toml");
        modBus.addListener(MccsNeoForge::onBuildContents);
        modBus.addListener(RegisterCapabilitiesEvent.class, event -> event.registerItem(Capabilities.Energy.ITEM,
                (stack, access) -> new StaffEnergyHandler(access, StaffSystems.capacity(stack)),
                STAFFS.stream().map(DeferredHolder::get).toArray(Item[]::new)));
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            var registrar = event.registrar(NETWORK_PROTOCOL);
            registrar.playToServer(StaffActionPacket.TYPE, StaffActionPacket.STREAM_CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()));
            registrar.playToClient(BeamStatePacket.TYPE, BeamStatePacket.STREAM_CODEC, (packet, context) -> packet.handle());
        });
        StaffBeam.sender = (shooter, firing, tether, targets) ->
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(shooter, new BeamStatePacket(shooter.getId(), firing, tether, targets));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> StaffSystems.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener(AttackEntityEvent.class, event -> StaffSystems.onAttack(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> StaffLights.removeAll());
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, event -> {
            if (StaffSystems.absorb(event.getEntity(), event.getSource(), event.getAmount())) event.setCanceled(true);
        });
    }

    private static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (DeferredHolder<Item, Item> staff : STAFFS) {
                event.accept(staff.get());
                ItemStack charged = new ItemStack(staff.get());
                StaffData.setSavedEnergy(charged, Integer.MAX_VALUE);
                event.accept(charged, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY); // the search would show the staff twice
            }
        }
    }
}
