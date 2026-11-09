package com.lach.affermath.client;

import com.lach.affermath.Affermath;
import com.lach.affermath.registry.ModEntities;
import com.lach.affermath.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Affermath.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(Renderers.WRAITH, WraithModel::createLayer);
        e.registerLayerDefinition(Renderers.CROW, CrowModel::createLayer);
        e.registerLayerDefinition(Renderers.CRATOMANCER, Renderers::humanoid64);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.HOLLOW_SOLDIER.get(), Renderers.Soldier::new);
        e.registerEntityRenderer(ModEntities.ASH_WRAITH.get(), Renderers.Wraith::new);
        e.registerEntityRenderer(ModEntities.CINDER_CROW.get(), Renderers.Crow::new);
        e.registerEntityRenderer(ModEntities.CRATOMANCER.get(), Renderers.Boss::new);
    }

    @SubscribeEvent
    public static void particles(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(ModParticles.ASH_FLAKE.get(), AshParticle.Flake::new);
        e.registerSpriteSet(ModParticles.ASH_STREAK.get(), AshParticle.Streak::new);
    }

    private ClientSetup() {}
}
