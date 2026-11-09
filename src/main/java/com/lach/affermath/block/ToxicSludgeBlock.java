package com.lach.affermath.block;

import com.lach.affermath.Affermath;
import com.lach.affermath.registry.ModEffects;
import com.lach.affermath.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A thin skin of contaminated sludge. Wading through it slows you and makes you sick unless you wear a gas mask. */
public class ToxicSludgeBlock extends Block {
    public static final MapCodec<ToxicSludgeBlock> CODEC = simpleCodec(ToxicSludgeBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public ToxicSludgeBlock(Properties p) {
        super(p);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState s, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState s, Direction d, BlockState n, LevelAccessor level, BlockPos pos, BlockPos np) {
        return canSurvive(s, level, pos) ? s : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void entityInside(BlockState s, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(s, new Vec3(0.85, 1.0, 0.85));
        if (!level.isClientSide && entity instanceof LivingEntity le && !protectedFrom(le)) {
            MobEffectInstance cur = le.getEffect(ModEffects.FALLOUT_SICKNESS);
            int amp = cur != null && cur.getDuration() > 300 ? Math.min(2, cur.getAmplifier() + 1) : cur != null ? cur.getAmplifier() : 0;
            le.addEffect(new MobEffectInstance(ModEffects.FALLOUT_SICKNESS, 240, amp));
        }
    }

    /** Gas mask wearers and the dimension's own creatures shrug it off. */
    public static boolean protectedFrom(LivingEntity e) {
        if (e.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GAS_MASK.get())) return true;
        return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals(Affermath.MODID);
    }

    @Override
    public void animateTick(BlockState s, Level level, BlockPos pos, RandomSource r) {
        if (r.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + r.nextDouble(), pos.getY() + 0.13, pos.getZ() + r.nextDouble(), 0, 0.01, 0);
        }
        if (r.nextInt(30) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + r.nextDouble(), pos.getY() + 0.2 + r.nextDouble() * 0.6, pos.getZ() + r.nextDouble(), 0, 0, 0);
        }
    }
}
