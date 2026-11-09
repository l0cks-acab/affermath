package com.lach.affermath.item;

import com.lach.affermath.Affermath;
import com.lach.affermath.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/** Flies like an Eye of Ender, but toward the nearest pre-war bunker in Affermath. */
public class EyeOfCratomancerItem extends Item {
    public static final TagKey<Structure> LOCATES = TagKey.create(Registries.STRUCTURE, Affermath.id("eye_of_the_cratomancer_located"));

    public EyeOfCratomancerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.consume(stack);
        if (!Affermath.isAffermath(level)) {
            player.displayClientMessage(Component.translatable("message.affermath.eye_wrong_dimension"), true);
            return InteractionResultHolder.fail(stack);
        }
        BlockPos target = server.findNearestMapStructure(LOCATES, player.blockPosition(), 100, false);
        if (target == null) return InteractionResultHolder.consume(stack);
        EyeOfEnder eye = new EyeOfEnder(level, player.getX(), player.getY(0.5), player.getZ());
        eye.setItem(stack);
        eye.signalTo(target);
        level.gameEvent(GameEvent.PROJECTILE_SHOOT, eye.position(), GameEvent.Context.of(player));
        level.addFreshEntity(eye);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 1f, 0.6f);
        stack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(ModItems.tip("eye_of_the_cratomancer"));
    }
}
