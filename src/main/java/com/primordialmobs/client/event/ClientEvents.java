package com.primordialmobs.client.event;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.ClientProxy;
import com.primordialmobs.server.entity.living.SubterranodonEntity;
import com.primordialmobs.server.entity.living.TremorsaurusEntity;
import com.primordialmobs.server.entity.util.RidingMeterMount;
import com.primordialmobs.server.entity.util.ShakesScreen;
import com.primordialmobs.server.potion.PMEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    private static final ResourceLocation DINOSAUR_HUD_OVERLAYS = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/misc/dinosaur_hud_overlays.png");

    @SubscribeEvent
    public void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        float partialTick = Minecraft.getInstance().getPartialTick();
        float tremorAmount = 0F;
        if (player != null && PrimordialMobs.CLIENT_CONFIG.screenShaking.get()) {
            double shakeDistanceScale = 64;
            double distance = Double.MAX_VALUE;
            AABB aabb = player.getBoundingBox().inflate(shakeDistanceScale);
            for (Mob screenShaker : Minecraft.getInstance().level.getEntitiesOfClass(Mob.class, aabb, (mob -> mob instanceof ShakesScreen))) {
                ShakesScreen shakesScreen = (ShakesScreen) screenShaker;
                if (shakesScreen.canFeelShake(player) && screenShaker.distanceTo(player) < distance) {
                    distance = screenShaker.distanceTo(player);
                    tremorAmount = Math.min((1F - (float) Math.min(1, distance / shakesScreen.getShakeDistance())) * Math.max(shakesScreen.getScreenShakeAmount(partialTick), 0F), 2.0F);
                }
            }
            if (tremorAmount > 0) {
                if (ClientProxy.lastTremorTick != player.tickCount) {
                    RandomSource rng = player.level().random;
                    ClientProxy.randomTremorOffsets[0] = rng.nextFloat();
                    ClientProxy.randomTremorOffsets[1] = rng.nextFloat();
                    ClientProxy.randomTremorOffsets[2] = rng.nextFloat();
                    ClientProxy.lastTremorTick = player.tickCount;
                }
                double intensity = tremorAmount * Minecraft.getInstance().options.screenEffectScale().get();
                event.getCamera().move(ClientProxy.randomTremorOffsets[0] * 0.2F * intensity, ClientProxy.randomTremorOffsets[1] * 0.2F * intensity, ClientProxy.randomTremorOffsets[2] * 0.5F * intensity);
            }
        }
        if (player != null && player.isPassenger() && player.getVehicle() instanceof TremorsaurusEntity && event.getCamera().isDetached()) {
            event.getCamera().move(-event.getCamera().getMaxZoom(2F), 0, 0);
        }
        // A stunned player's view wobbles, exactly like in Alex's Caves (whose handler already does this
        // when that mod is installed, so only apply it standalone).
        if (!PrimordialMobs.ALEXS_CAVES_INSTALLED && player instanceof LivingEntity livingEntity && livingEntity.hasEffect(PMEffectRegistry.STUNNED.get())) {
            event.setRoll((float) (Math.sin((player.tickCount + partialTick) * 0.2F) * 10F));
        }
    }

    /**
     * Suppresses the normal world render of any entity a layer has already drawn this frame (held fish,
     * held prey, the Drifter's rider). The layer calls PROXY.blockRenderingEntity, this consumes the flag.
     * Same contract as Alex's Caves' handler: forward the Post event so other listeners still run, and
     * never suppress the first-person player.
     */
    @SubscribeEvent
    public void preRenderLiving(RenderLivingEvent.Pre event) {
        if (ClientProxy.blockedEntityRenders.contains(event.getEntity().getUUID())) {
            if (!PrimordialMobs.PROXY.isFirstPersonPlayer(event.getEntity())) {
                MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(event.getEntity(), event.getRenderer(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()));
                event.setCanceled(true);
            }
            ClientProxy.blockedEntityRenders.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public void onPostRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Player player = PrimordialMobs.PROXY.getClientSidePlayer();
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && player.getVehicle() instanceof RidingMeterMount mount && mount.hasRidingMeter()) {
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            int forgeGuiY = Minecraft.getInstance().gui instanceof ForgeGui forgeGui ? Math.max(forgeGui.leftHeight, forgeGui.rightHeight) : 0;
            if (player.getArmorValue() > 0 && mount instanceof SubterranodonEntity) {
                forgeGuiY += 25;
            }
            if (forgeGuiY < 53) {
                forgeGuiY = 53;
            }
            int j = screenWidth / 2 - PrimordialMobs.CLIENT_CONFIG.subterranodonIndicatorX.get();
            int k = screenHeight - forgeGuiY - PrimordialMobs.CLIENT_CONFIG.subterranodonIndicatorY.get();
            float f = mount.getMeterAmount();
            float invProgress = 1 - f;
            int uOffset = 0;
            int vOffset = 0;
            int dinoHeight = 31;
            if (mount instanceof TremorsaurusEntity) {
                vOffset = 63;
                k += 5;
            }
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().blit(DINOSAUR_HUD_OVERLAYS, j, k, 50, uOffset, vOffset + dinoHeight, 43, dinoHeight, 128, 512);
            event.getGuiGraphics().blit(DINOSAUR_HUD_OVERLAYS, j, k, 50, uOffset, vOffset, 43, (int) Math.floor(dinoHeight * invProgress), 128, 512);
            event.getGuiGraphics().pose().popPose();
        }
    }
}
