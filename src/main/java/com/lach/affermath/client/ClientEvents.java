package com.lach.affermath.client;

import com.lach.affermath.Affermath;
import com.lach.affermath.entity.Cratomancer;
import com.lach.affermath.item.CurioHelper;
import com.lach.affermath.registry.ModItems;
import com.lach.affermath.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Affermath.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    /** Base fog distance in Affermath, in blocks. */
    private static final float BASE_FOG = 34f;
    private static float fogNow = BASE_FOG;
    private static float fogTarget = BASE_FOG;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null || mc.isPaused() || !Affermath.isAffermath(level)) return;
        fogTarget = computeFog(player);
        fogNow += (fogTarget - fogNow) * 0.05f;
        ParticleStatus ps = mc.options.particles().get();
        int flakes = ps == ParticleStatus.ALL ? 18 : ps == ParticleStatus.DECREASED ? 8 : 3;
        int streaks = ps == ParticleStatus.ALL ? 2 : ps == ParticleStatus.DECREASED ? 1 : 0;
        RandomSource r = level.getRandom();
        spawn(level, player, r, flakes, false);
        spawn(level, player, r, streaks, true);
    }

    private static void spawn(ClientLevel level, LocalPlayer p, RandomSource r, int n, boolean streak) {
        for (int i = 0; i < n; i++) {
            double x = p.getX() + (r.nextDouble() - 0.5) * 40;
            double z = p.getZ() + (r.nextDouble() - 0.5) * 40;
            double y = p.getY() + 3 + r.nextDouble() * 16;
            int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
            if (top > y) {
                if (top > p.getY() + 20) continue;
                y = top + 1 + r.nextDouble() * 10;
            }
            level.addParticle(streak ? ModParticles.ASH_STREAK.get() : ModParticles.ASH_FLAKE.get(), x, y, z, 0, 0, 0);
        }
    }

    private static float computeFog(LocalPlayer p) {
        float fog = BASE_FOG;
        if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GAS_MASK.get())) fog *= 1.6f;
        if (CurioHelper.has(p, ModItems.FIELD_LANTERN.get())) fog *= 1.3f;
        if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.WARFORGED_HELMET.get())
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.WARFORGED_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.WARFORGED_LEGGINGS.get())
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.WARFORGED_BOOTS.get())) fog *= 3.5f;
        // the Cratomancer chokes the air once wounded
        for (Cratomancer c : p.level().getEntitiesOfClass(Cratomancer.class, new AABB(p.blockPosition()).inflate(48))) {
            if (c.getPhase() >= 2) fog = Math.min(fog, c.getPhase() >= 3 ? 12f : 18f);
        }
        return fog;
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog e) {
        Minecraft mc = Minecraft.getInstance();
        if (!Affermath.isAffermath(mc.level) || e.getType() != FogType.NONE) return;
        var d = e.getFogData();
        float far = Math.min(fogNow, d.renderDistanceEnd);
        d.environmentalStart = far * 0.05f;
        d.environmentalEnd = far;
        d.renderDistanceStart = Math.min(d.renderDistanceStart, far * 0.05f);
        d.renderDistanceEnd = Math.min(d.renderDistanceEnd, far);
        d.skyEnd = far;
        d.cloudEnd = far;
    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor e) {
        Minecraft mc = Minecraft.getInstance();
        if (!Affermath.isAffermath(mc.level) || e.getCamera().getFluidInCamera() != FogType.NONE) return;
        e.setRed(0.56f);
        e.setGreen(0.555f);
        e.setBlue(0.55f);
    }

    private ClientEvents() {}
}
