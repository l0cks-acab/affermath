package com.lach.affermath.client;

import com.lach.affermath.Affermath;
import com.lach.affermath.entity.AshWraith;
import com.lach.affermath.entity.CinderCrow;
import com.lach.affermath.entity.Cratomancer;
import com.lach.affermath.entity.HollowSoldier;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;

public final class Renderers {
    public static final ModelLayerLocation WRAITH = new ModelLayerLocation(Affermath.id("ash_wraith"), "main");
    public static final ModelLayerLocation CROW = new ModelLayerLocation(Affermath.id("cinder_crow"), "main");
    public static final ModelLayerLocation CRATOMANCER = new ModelLayerLocation(Affermath.id("cratomancer"), "main");

    private static ResourceLocation tex(String n) {
        return Affermath.id("textures/entity/" + n + ".png");
    }

    public static LayerDefinition humanoid64() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0f), 64, 64);
    }

    public static class Soldier extends AbstractZombieRenderer<HollowSoldier, ZombieModel<HollowSoldier>> {
        private static final ResourceLocation TEX = tex("hollow_soldier");

        public Soldier(EntityRendererProvider.Context ctx) {
            super(ctx, new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                    new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
        }

        @Override
        public ResourceLocation getTextureLocation(Zombie entity) {
            return TEX;
        }
    }

    public static class Wraith extends MobRenderer<AshWraith, WraithModel> {
        private static final ResourceLocation TEX = tex("ash_wraith");

        public Wraith(EntityRendererProvider.Context ctx) {
            super(ctx, new WraithModel(ctx.bakeLayer(WRAITH)), 0f);
        }

        @Override
        public void render(AshWraith e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
            // fades into the fog with distance: solid up close, a faint smear beyond ~10 blocks
            var cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            double d = Math.sqrt(e.distanceToSqr(cam));
            model.alpha = (float) Mth.clampedLerp(0.85, 0.12, (d - 4) / 12.0);
            if (e.hurtTime > 0) model.alpha = 0.9f;
            super.render(e, yaw, partial, pose, buf, light);
        }

        @Override
        public ResourceLocation getTextureLocation(AshWraith e) {
            return TEX;
        }
    }

    public static class Crow extends MobRenderer<CinderCrow, CrowModel> {
        private static final ResourceLocation TEX = tex("cinder_crow");

        public Crow(EntityRendererProvider.Context ctx) {
            super(ctx, new CrowModel(ctx.bakeLayer(CROW)), 0.25f);
        }

        @Override
        public ResourceLocation getTextureLocation(CinderCrow e) {
            return TEX;
        }
    }

    public static class Boss extends HumanoidMobRenderer<Cratomancer, HumanoidModel<Cratomancer>> {
        private static final ResourceLocation TEX = tex("cratomancer");

        public Boss(EntityRendererProvider.Context ctx) {
            super(ctx, new HumanoidModel<>(ctx.bakeLayer(CRATOMANCER)), 0.9f, 1.4f, 1.4f, 1.4f);
        }

        @Override
        public ResourceLocation getTextureLocation(Cratomancer e) {
            return TEX;
        }
    }

    private Renderers() {}
}
