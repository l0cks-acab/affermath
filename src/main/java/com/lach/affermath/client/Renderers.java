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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class Renderers {
    public static final ModelLayerLocation SOLDIER = new ModelLayerLocation(Affermath.id("hollow_soldier"), "main");
    public static final ModelLayerLocation WRAITH = new ModelLayerLocation(Affermath.id("ash_wraith"), "main");
    public static final ModelLayerLocation CROW = new ModelLayerLocation(Affermath.id("cinder_crow"), "main");
    public static final ModelLayerLocation CRATOMANCER = new ModelLayerLocation(Affermath.id("cratomancer"), "main");

    private static ResourceLocation tex(String n) {
        return Affermath.id("textures/entity/" + n + ".png");
    }

    /** Full-bright emissive pass for eyes, lenses, embers and the boss's furnace core. */
    public static class Glow<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
        private final RenderType type;

        public Glow(RenderLayerParent<T, M> parent, String texture) {
            super(parent);
            type = RenderType.eyes(tex(texture + "_glow"));
        }

        @Override
        public RenderType renderType() {
            return type;
        }
    }

    public static class Soldier extends HumanoidMobRenderer<HollowSoldier, SoldierModel> {
        private static final ResourceLocation TEX = tex("hollow_soldier");

        public Soldier(EntityRendererProvider.Context ctx) {
            super(ctx, new SoldierModel(ctx.bakeLayer(SOLDIER)), 0.5f);
            addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                    new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), ctx.getModelManager()));
            addLayer(new Glow<>(this, "hollow_soldier"));
        }

        @Override
        public ResourceLocation getTextureLocation(HollowSoldier e) {
            return TEX;
        }
    }

    public static class Wraith extends MobRenderer<AshWraith, WraithModel> {
        private static final ResourceLocation TEX = tex("ash_wraith");

        public Wraith(EntityRendererProvider.Context ctx) {
            super(ctx, new WraithModel(ctx.bakeLayer(WRAITH)), 0f);
            addLayer(new Glow<>(this, "ash_wraith"));
        }

        @Override
        public void render(AshWraith e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
            // fades into the fog with distance: solid up close, a faint smear beyond ~10 blocks; the eyes stay lit
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
            super(ctx, new CrowModel(ctx.bakeLayer(CROW)), 0.3f);
            addLayer(new Glow<>(this, "cinder_crow"));
        }

        @Override
        public ResourceLocation getTextureLocation(CinderCrow e) {
            return TEX;
        }
    }

    public static class Boss extends MobRenderer<Cratomancer, CratomancerModel> {
        private static final ResourceLocation TEX = tex("cratomancer");

        public Boss(EntityRendererProvider.Context ctx) {
            super(ctx, new CratomancerModel(ctx.bakeLayer(CRATOMANCER)), 1.1f);
            addLayer(new Glow<>(this, "cratomancer"));
        }

        @Override
        protected void scale(Cratomancer e, PoseStack pose, float partial) {
            pose.scale(1.3f, 1.3f, 1.3f);
        }

        @Override
        public ResourceLocation getTextureLocation(Cratomancer e) {
            return TEX;
        }
    }

    private Renderers() {}
}
