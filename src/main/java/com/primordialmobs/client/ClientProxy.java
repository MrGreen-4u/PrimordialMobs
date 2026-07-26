package com.primordialmobs.client;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.event.ClientEvents;
import com.primordialmobs.client.particle.PMParticleRegistry;
import com.primordialmobs.client.particle.StunStarParticle;
import com.primordialmobs.client.particle.WaterTremorParticle;
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

public class ClientProxy extends CommonProxy {

    public static final RandomSource random = RandomSource.create();
    public static int lastTremorTick = -1;
    public static float[] randomTremorOffsets = new float[3];
    public static int renderNukeSkyDarkFor = 0;
    private final PMItemRenderProperties isterProperties = new PMItemRenderProperties();

    @Override
    public void commonInit() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (!PrimordialMobs.ALEXS_CAVES_INSTALLED) {
            // With the full Alex's Caves installed, our particles are not registered and its own
            // special-ability keybind already exists; registering ours would duplicate both.
            bus.addListener(this::setupParticles);
            bus.addListener(this::registerKeybinds);
        } else {
            // Force our renamed names (Roarer/Rammer/... and their items) to win over Alex's Caves' lang.
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
        }
    }

    public void setupParticles(RegisterParticleProvidersEvent registry) {
        registry.registerSpriteSet(PMParticleRegistry.WATER_TREMOR.get(), WaterTremorParticle.Factory::new);
        registry.registerSpecial(PMParticleRegistry.STUN_STAR.get(), new StunStarParticle.Factory());
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

    @Override
    public float getPartialTicks() {
        return Minecraft.getInstance().getPartialTick();
    }

    @Override
    public boolean isFirstPersonPlayer(Entity entity) {
        return entity.equals(Minecraft.getInstance().cameraEntity) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }
}
