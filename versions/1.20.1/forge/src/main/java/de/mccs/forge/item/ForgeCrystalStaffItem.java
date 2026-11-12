package de.mccs.forge.item;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.config.StaffConfig;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.client.StaffArmPose;
import de.mccs.mc.client.render.StaffItemRenderer;
import de.mccs.core.staff.StaffKind;
import de.mccs.mc.item.CrystalStaffItem;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffSystems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Consumer;

/** Forge's additions to the staff: Forge Energy, the item renderer and the arm pose (on the other platforms the pose is a mixin). */
public final class ForgeCrystalStaffItem extends CrystalStaffItem {
    private static final int USE_ANIMATION_TICKS = 8;
    // Where vanilla puts a held item in first person (ItemInHandRenderer)
    private static final float ITEM_X = 0.56f;
    private static final float ITEM_Y = -0.52f;
    private static final float ITEM_Z = -0.72f;

    public ForgeCrystalStaffItem(Properties properties, StaffKind kind) {
        super(properties, kind);
    }

    /** Takes energy from other mods (Mekanism, Thermal, ...); it cannot be drained. */
    private record EnergyView(ItemStack stack) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            StaffConfig config = ServerConfig.STAFF;
            int stored = StaffSystems.stored(stack);
            int accepted = StaffEnergy.accepted(stored, StaffSystems.capacity(stack), config.maxReceive(), amount);
            if (!simulate && accepted > 0) StaffData.setSavedEnergy(stack, stored + accepted);
            return accepted;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return StaffSystems.stored(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return StaffSystems.capacity(stack);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> new EnergyView(stack));
        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
                return capability == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }

    /** The stack changes constantly while the shield runs: this must neither replay the equip animation... */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    /** ...nor restart breaking a block. */
    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !oldStack.is(newStack.getItem());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final HumanoidModel.ArmPose pose = HumanoidModel.ArmPose.create("MCCS_STAFF", false,
                    (model, entity, arm) -> {
                        if (arm == HumanoidArm.RIGHT) StaffArmPose.applyRight(model.rightArm);
                        else StaffArmPose.applyLeft(model.leftArm);
                    });

            /** While charging, and shortly after, the equip animation of a right click is left out: the staff stays in the picture. */
            private int lastUsedTick = -USE_ANIMATION_TICKS;

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
                                                   float partialTick, float equipProcess, float swingProcess) {
                if (player.isUsingItem()) lastUsedTick = player.tickCount;
                int sinceUse = player.tickCount - lastUsedTick;
                if (sinceUse < 0 || sinceUse >= USE_ANIMATION_TICKS || equipProcess <= 0) return false;
                poseStack.translate((arm == HumanoidArm.RIGHT ? 1 : -1) * ITEM_X, ITEM_Y, ITEM_Z);
                return true;
            }

            /** The item model is "builtin/entity": Minecraft hands the drawing to this renderer with the display transform applied. */
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return Renderer.INSTANCE;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return pose;
            }
        });
    }

    /** Forge's item renderer hook, handing over to the shared {@link StaffItemRenderer}; created on first use, when the game is ready. */
    private static final class Renderer extends BlockEntityWithoutLevelRenderer {
        static final Renderer INSTANCE = new Renderer();

        private Renderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
            StaffItemRenderer.render(stack, context, poseStack, buffers, light, overlay);
        }
    }
}
