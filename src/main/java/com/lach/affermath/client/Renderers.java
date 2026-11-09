package com.lach.affermath.client;

import com.lach.affermath.Affermath;
import com.lach.affermath.entity.AshWraith;
import com.lach.affermath.entity.CinderCrow;
import com.lach.affermath.entity.Cratomancer;
import com.lach.affermath.entity.HollowSoldier;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class Renderers {
    public static final ModelLayerLocation WRAITH = new ModelLayerLocation(Affermath.id("ash_wraith"), "main");
    public static final ModelLayerLocation CROW = new ModelLayerLocation(Affermath.id("cinder_crow"), "main");
    public static final ModelLayerLocation CRATOMANCER = new ModelLayerLocation(Affermath.id("cratomancer"), "main");

    private static Identifier tex(String n) {
        return Affermath.id("textures/entity/" + n + ".png");
    }

    public static LayerDefinition humanoid64() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0f), 64, 64);
    }

    public static class Soldier extends AbstractZombieRenderer<HollowSoldier, ZombieRenderState, ZombieModel<ZombieRenderState>> {
        private static final Identifier TEX = tex("hollow_soldier");

        public Soldier(EntityRendererProvider.Context ctx) {
            super(ctx, new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), new BabyZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                    ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, ctx.getModelSet(), ZombieModel::new),
                    ArmorModelSet.bake(ModelLayers.ZOMBIE_BABY_ARMOR, ctx.getModelSet(), BabyZombieModel::new));
        }

        @Override
        public ZombieRenderState createRenderState() {
            return new ZombieRenderState();
        }

        @Override
        public Identifier getTextureLocation(ZombieRenderState s) {
            return TEX;
        }
    }

    public static class Wraith extends HumanoidMobRenderer<AshWraith, WraithModel.State, WraithModel> {
        private static final Identifier TEX = tex("ash_wraith");

        public Wraith(EntityRendererProvider.Context ctx) {
            super(ctx, new WraithModel(ctx.bakeLayer(WRAITH)), 0f);
        }

        @Override
        public WraithModel.State createRenderState() {
            return new WraithModel.State();
        }

        @Override
        public void extractRenderState(AshWraith e, WraithModel.State s, float partial) {
            super.extractRenderState(e, s, partial);
            s.aggressive = e.isAggressive();
            // fades into the fog with distance: solid up close, a faint smear beyond ~10 blocks
            var cam = Minecraft.getInstance().gameRenderer.getMainCamera().position();
            double d = Math.sqrt(e.distanceToSqr(cam));
            s.alpha = (float) Mth.clampedLerp((d - 4) / 12.0, 0.85, 0.12);
            if (e.hurtTime > 0) s.alpha = 0.9f;
        }

        @Override
        protected int getModelTint(WraithModel.State s) {
            return ARGB.color(Math.round(s.alpha * 255), 0xFFFFFF);
        }

        @Override
        public Identifier getTextureLocation(WraithModel.State s) {
            return TEX;
        }
    }

    public static class Crow extends MobRenderer<CinderCrow, CrowModel.State, CrowModel> {
        private static final Identifier TEX = tex("cinder_crow");

        public Crow(EntityRendererProvider.Context ctx) {
            super(ctx, new CrowModel(ctx.bakeLayer(CROW)), 0.25f);
        }

        @Override
        public CrowModel.State createRenderState() {
            return new CrowModel.State();
        }

        @Override
        public void extractRenderState(CinderCrow e, CrowModel.State s, float partial) {
            super.extractRenderState(e, s, partial);
            s.flying = e.isFlying();
            s.id = e.getId();
        }

        @Override
        public Identifier getTextureLocation(CrowModel.State s) {
            return TEX;
        }
    }

    public static class Boss extends HumanoidMobRenderer<Cratomancer, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
        private static final Identifier TEX = tex("cratomancer");

        public Boss(EntityRendererProvider.Context ctx) {
            super(ctx, new HumanoidModel<>(ctx.bakeLayer(CRATOMANCER)), 0.9f);
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        protected void scale(HumanoidRenderState s, PoseStack pose) {
            pose.scale(1.4f, 1.4f, 1.4f);
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState s) {
            return TEX;
        }
    }

    private Renderers() {}
}
