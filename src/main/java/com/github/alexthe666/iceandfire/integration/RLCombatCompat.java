package com.github.alexthe666.iceandfire.integration;

import bettercombat.mod.event.RLCombatModifyDamageEvent;
import com.github.alexthe666.iceandfire.item.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RLCombatCompat {

    @SubscribeEvent
    public static void modifyAttackDamagePre(RLCombatModifyDamageEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        if (player == null || !(target instanceof EntityLivingBase) || event.getStack().isEmpty()) return;

        Item item = event.getStack().getItem();
        if (item instanceof IHitEffect) {
            event.setDamageModifier(event.getDamageModifier() + ((IHitEffect) item).getHitEffectModifier((EntityLivingBase)target, player));
        }
    }
}