package com.lach.affermath.client;

import com.lach.affermath.Affermath;
import com.lach.affermath.entity.AshWraith;
import com.lach.affermath.entity.CinderCrow;
import com.lach.affermath.entity.Cratomancer;
import com.lach.affermath.entity.HollowSoldier;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class Renderers {
    public static final ModelLayerLocation SOLDIER = new ModelLayerLocation(Affermath.id("hollow_soldier"), "main");
    public static final ModelLayerLocation WRAITH = new ModelLayerLocation(Affermath.id("ash_wraith"), "main");
    public static final ModelLayerLocation CROW = new ModelLayerLocation(Affermath.id("cinder_crow"), "main");
    public static final ModelLayerLocation CRATOMANCER = new ModelLayerLocation(Affermath.id("cratomancer"), "main");

    private static Identifier tex(String n) {
        return Affermath.id("textures/entity/" + n + ".png");
    }

    /** Full-bright emissive pass for eyes, lenses, embers and the boss's furnace core. */
    public static class Glow<S extends EntityRenderState, M extends EntityModel<S>> extends EyesLayer<S, M> {
        private final RenderType type;

        public Glow(RenderLayerParent<S, M> parent, String texture) {
            super(parent);
            type = RenderTypes.eyes(tex(texture + "_glow"));
        }

        @Override
        public RenderType renderType() {
            return type;
        }
    }

    public static class Soldier extends HumanoidMobRenderer<HollowSoldier, SoldierModel.State, SoldierModel> {
        private static final Identifier TEX = tex("hollow_soldier");

        public Soldier(EntityRendererProvider.Context ctx) {
            super(ctx, new SoldierModel(ctx.bakeLayer(SOLDIER)), 0.5f);
            addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, ctx.getModelSet(), HumanoidModel::new),
                    ctx.getEquipmentRenderer()));
            addLayer(new Glow<>(this, "hollow_soldier"));
        }

        @Override
        public SoldierModel.State createRenderState() {
            return new SoldierModel.State();
        }

        @Override
        public void extractRenderState(HollowSoldier e, SoldierModel.State s, float partial) {
            super.extractRenderState(e, s, partial);
            s.isAggressive = e.isAggressive();
            s.id = e.getId();
        }

        @Override
        public Identifier getTextureLocation(SoldierModel.State s) {
            return TEX;
        }
    }

    public static class Wraith extends MobRenderer<AshWraith, WraithModel.State, WraithModel> {
        private static final Identifier TEX = tex("ash_wraith");

        public Wraith(EntityRendererProvider.Context ctx) {
            super(ctx, new WraithModel(ctx.bakeLayer(WRAITH)), 0f);
            addLayer(new Glow<>(this, "ash_wraith"));
        }

        @Override
        public WraithModel.State createRenderState() {
            return new WraithModel.State();
        }

        @Override
        public void extractRenderState(AshWraith e, WraithModel.State s, float partial) {
            super.extractRenderState(e, s, partial);
            s.aggressive = e.isAggressive();
            s.attackTime = e.getAttackAnim(partial);
            // fades into the fog with distance: solid up close, a faint smear beyond ~10 blocks; the eyes stay lit
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
            super(ctx, new CrowModel(ctx.bakeLayer(CROW)), 0.3f);
            addLayer(new Glow<>(this, "cinder_crow"));
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

    public static class Boss extends MobRenderer<Cratomancer, CratomancerModel.State, CratomancerModel> {
        private static final Identifier TEX = tex("cratomancer");

        public Boss(EntityRendererProvider.Context ctx) {
            super(ctx, new CratomancerModel(ctx.bakeLayer(CRATOMANCER)), 1.1f);
            addLayer(new Glow<>(this, "cratomancer"));
        }

        @Override
        public CratomancerModel.State createRenderState() {
            return new CratomancerModel.State();
        }

        @Override
        public void extractRenderState(Cratomancer e, CratomancerModel.State s, float partial) {
            super.extractRenderState(e, s, partial);
            s.attackTime = e.getAttackAnim(partial);
            s.phase = e.getPhase();
        }

        @Override
        protected void scale(CratomancerModel.State s, PoseStack pose) {
            pose.scale(1.3f, 1.3f, 1.3f);
        }

        @Override
        public Identifier getTextureLocation(CratomancerModel.State s) {
            return TEX;
        }
    }

    private Renderers() {}
}
