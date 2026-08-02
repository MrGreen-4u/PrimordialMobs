package com.primordialmobs.client;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.event.ClientEvents;
import com.primordialmobs.client.particle.AmberMonolithParticle;
import com.primordialmobs.client.particle.PMParticleRegistry;
import com.primordialmobs.client.particle.SmallExplosionParticle;
import com.primordialmobs.client.particle.StunStarParticle;
import com.primordialmobs.client.particle.WaterTremorParticle;
import com.primordialmobs.client.render.blockentity.AmberMonolithBlockRenderer;
import com.primordialmobs.server.block.blockentity.PMBlockEntityRegistry;
import com.primordialmobs.server.item.PMItemRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import com.primordialmobs.client.render.entity.FallingTreeBlockRenderer;
import com.primordialmobs.client.render.entity.GrottoceratopsRenderer;
import com.primordialmobs.client.render.entity.PrimordialSnifferRenderer;
import com.primordialmobs.client.render.entity.RelicheirusRenderer;
import com.primordialmobs.client.render.entity.SubterranodonRenderer;
import com.primordialmobs.client.render.entity.TremorsaurusRenderer;
import com.primordialmobs.client.render.entity.TrilocarisRenderer;
import com.primordialmobs.client.render.entity.VallumraptorRenderer;
import com.primordialmobs.client.render.item.PMItemRenderProperties;
import com.primordialmobs.server.CommonProxy;
import com.primordialmobs.server.entity.PMEntityRegistry;
import com.primordialmobs.server.misc.PMKeybindRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientProxy extends CommonProxy {

    public static final RandomSource random = RandomSource.create();
    public static int lastTremorTick = -1;
    public static float[] randomTremorOffsets = new float[3];
    public static int renderNukeSkyDarkFor = 0;
    /**
     * Entities whose normal world render must be suppressed for one frame because a layer already drew
     * them (Logger's held fish, Roarer's held prey, Drifter's rider). ClientEvents#preRenderLiving
     * consumes this; without it both renders happen and the entity appears twice.
     */
    public static final List<UUID> blockedEntityRenders = new ArrayList<>();
    private final PMItemRenderProperties isterProperties = new PMItemRenderProperties();

    @Override
    public void commonInit() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (!PrimordialMobs.ALEXS_CAVES_INSTALLED) {
            // With the full Alex's Caves installed, our particles are not registered and its own
            // special-ability keybind already exists; registering ours would duplicate both.
            bus.addListener(this::setupParticles);
            bus.addListener(this::registerKeybinds);
            bus.addListener(com.primordialmobs.client.model.PMModelLayers::register);
        } else {
            // Force our renamed names (Roarer/Grazer/... and their items) to win over Alex's Caves' lang.
            bus.addListener(new com.primordialmobs.compat.client.CompatLangPack()::addPackFinders);
        }
    }

    @Override
    public void clientInit() {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        // The retextured Sniffer renderer applies in both modes (the Sniffer changes always carry over).
        EntityRenderers.register(EntityType.SNIFFER, PrimordialSnifferRenderer::new);
        if (!PrimordialMobs.ALEXS_CAVES_INSTALLED) {
            EntityRenderers.register(PMEntityRegistry.FALLING_TREE_BLOCK.get(), FallingTreeBlockRenderer::new);
            EntityRenderers.register(PMEntityRegistry.TRILOCARIS.get(), TrilocarisRenderer::new);
            EntityRenderers.register(PMEntityRegistry.SUBTERRANODON.get(), SubterranodonRenderer::new);
            EntityRenderers.register(PMEntityRegistry.VALLUMRAPTOR.get(), VallumraptorRenderer::new);
            EntityRenderers.register(PMEntityRegistry.GROTTOCERATOPS.get(), GrottoceratopsRenderer::new);
            EntityRenderers.register(PMEntityRegistry.TREMORSAURUS.get(), TremorsaurusRenderer::new);
            EntityRenderers.register(PMEntityRegistry.RELICHEIRUS.get(), RelicheirusRenderer::new);
            BlockEntityRenderers.register(PMBlockEntityRegistry.AMBER_MONOLITH.get(), AmberMonolithBlockRenderer::new);
            EntityRenderers.register(PMEntityRegistry.EXTINCTION_SPEAR.get(), com.primordialmobs.client.render.entity.ExtinctionSpearRenderer::new);
            EntityRenderers.register(PMEntityRegistry.DINOSAUR_SPIRIT.get(), com.primordialmobs.client.render.entity.DinosaurSpiritRenderer::new);
            EntityRenderers.register(PMEntityRegistry.BOAT.get(), context -> new com.primordialmobs.client.render.entity.PMBoatRenderer(context, false));
            EntityRenderers.register(PMEntityRegistry.CHEST_BOAT.get(), context -> new com.primordialmobs.client.render.entity.PMBoatRenderer(context, true));
            // Registers the pewen sign/hanging-sign material so the sign block entity renderer can draw them.
            net.minecraft.client.renderer.Sheets.addWoodType(com.primordialmobs.server.block.PMBlockRegistry.PEWEN_WOOD_TYPE);
            // Dinosaur Nuggets come in four shapes, chosen by stack count exactly like upstream.
            ItemProperties.register(PMItemRegistry.DINOSAUR_NUGGET.get(), new ResourceLocation("nugget"), (stack, level, living, j) -> {
                return (stack.getCount() % 4) / 4F;
            });
            ItemProperties.register(PMItemRegistry.EXTINCTION_SPEAR.get(), new ResourceLocation("throwing"), (stack, level, living, j) -> {
                return living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F;
            });
        }
    }

    public void setupParticles(RegisterParticleProvidersEvent registry) {
        registry.registerSpriteSet(PMParticleRegistry.WATER_TREMOR.get(), WaterTremorParticle.Factory::new);
        registry.registerSpecial(PMParticleRegistry.STUN_STAR.get(), new StunStarParticle.Factory());
        registry.registerSpriteSet(PMParticleRegistry.AMBER_MONOLITH.get(), AmberMonolithParticle.Factory::new);
        registry.registerSpriteSet(PMParticleRegistry.AMBER_EXPLOSION.get(), SmallExplosionParticle.AmberFactory::new);
        registry.registerSpriteSet(PMParticleRegistry.FLY.get(), com.primordialmobs.client.particle.FlyParticle.Factory::new);
        registry.registerSpriteSet(PMParticleRegistry.TEPHRA_FLAME.get(), com.primordialmobs.client.particle.TephraParticle.FlameFactory::new);
    }

    private void registerKeybinds(RegisterKeyMappingsEvent e) {
        e.register(PMKeybindRegistry.KEY_SPECIAL_ABILITY);
    }

    @Override
    public Player getClientSidePlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public boolean isKeyDown(int keyType) {
        if (keyType == -1) {
            return Minecraft.getInstance().options.keyLeft.isDown() || Minecraft.getInstance().options.keyRight.isDown() || Minecraft.getInstance().options.keyUp.isDown() || Minecraft.getInstance().options.keyDown.isDown() || Minecraft.getInstance().options.keyJump.isDown();
        }
        if (keyType == 0) {
            return Minecraft.getInstance().options.keyJump.isDown();
        }
        if (keyType == 1) {
            return Minecraft.getInstance().options.keySprint.isDown();
        }
        if (keyType == 2) {
            return PMKeybindRegistry.KEY_SPECIAL_ABILITY.isDown();
        }
        if (keyType == 3) {
            return Minecraft.getInstance().options.keyAttack.isDown();
        }
        if (keyType == 4) {
            return Minecraft.getInstance().options.keyShift.isDown();
        }
        return false;
    }

    @Override
    public Object getISTERProperties() {
        return isterProperties;
    }

    private final com.primordialmobs.client.render.item.PMArmorRenderProperties armorProperties = new com.primordialmobs.client.render.item.PMArmorRenderProperties();

    @Override
    public Object getArmorProperties() {
        return armorProperties;
    }

    @Override
    public float getPartialTicks() {
        return Minecraft.getInstance().getPartialTick();
    }

    @Override
    public boolean isFirstPersonPlayer(Entity entity) {
        return entity.equals(Minecraft.getInstance().cameraEntity) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @Override
    public void blockRenderingEntity(UUID id) {
        blockedEntityRenders.add(id);
    }

    @Override
    public void releaseRenderingEntity(UUID id) {
        blockedEntityRenders.remove(id);
    }
}
