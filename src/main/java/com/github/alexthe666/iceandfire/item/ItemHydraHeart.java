package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHydraHeart extends Item {

    public ItemHydraHeart() {
        super();
        this.setCreativeTab(IceAndFire.TAB);
        this.setTranslationKey("iceandfire.hydra_heart");
        this.setRegistryName(IceAndFire.MODID, "hydra_heart");
        this.maxStackSize = 1;
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isItemEqual(newStack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean unused2) {
        if(entity instanceof EntityPlayer && slot >= 0 && slot <= 8){
            double healthPercentage = ((EntityPlayer) entity).getHealth() / Math.max(1, ((EntityPlayer) entity).getMaxHealth());
            if(healthPercentage < 1.0D){
                int level = 0;
                if(healthPercentage < 0.25D){
                    level = 3;
                }else if(healthPercentage < 0.5D){
                    level = 2;
                }else if(healthPercentage < 0.75D){
                    level = 1;
                }
                if(!((EntityPlayer) entity).isPotionActive(MobEffects.REGENERATION))
                ((EntityPlayer) entity).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 900, level, true, false));

            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("item.iceandfire.hydra_heart.desc_0"));
        tooltip.add(I18n.format("item.iceandfire.hydra_heart.desc_1"));
    }
}
