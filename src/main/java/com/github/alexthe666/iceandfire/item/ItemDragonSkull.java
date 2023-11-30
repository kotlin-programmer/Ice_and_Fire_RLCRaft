package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.StatCollector;
import com.github.alexthe666.iceandfire.entity.EntityDragonSkull;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDragonSkull extends Item implements ICustomRendered {

    public ItemDragonSkull() {
        this.maxStackSize = 1;
        this.setHasSubtypes(true);
        this.setCreativeTab(IceAndFire.TAB);
        this.setTranslationKey("iceandfire.dragon_skull");
        this.setRegistryName(IceAndFire.MODID, "dragon_skull");
    }

    @Override
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
        itemStack.setTagCompound(new NBTTagCompound());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int f, boolean f1) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setInteger("Stage", 4);
            stack.getTagCompound().setInteger("DragonAge", 75);

        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        int meta = stack.getMetadata();
        String identifier = meta == 0 ? "dragon.fire" : meta == 1 ? "dragon.ice" : "dragon.lightning";
        tooltip.add(StatCollector.translateToLocal(identifier));
        if (stack.getTagCompound() != null) {
            tooltip.add(StatCollector.translateToLocal("dragon.stage") + stack.getTagCompound().getInteger("Stage"));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        /*
         * EntityDragonEgg egg = new EntityDragonEgg(worldIn);
         * egg.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() +
         * 0.5); if(!worldIn.isRemote){ worldIn.spawnEntityInWorld(egg); }
         */
        if (stack.getTagCompound() != null) {
            EntityDragonSkull skull = new EntityDragonSkull(worldIn);
            skull.setType(stack.getMetadata());
            skull.setStage(stack.getTagCompound().getInteger("Stage"));
            skull.setDragonAge(stack.getTagCompound().getInteger("DragonAge"));
            BlockPos offset = pos.offset(side, 1);
            skull.setLocationAndAngles(offset.getX() + 0.5, offset.getY(), offset.getZ() + 0.5, 0, 0);
            float yaw = player.rotationYaw;
            if(side != EnumFacing.UP){
                yaw = player.getHorizontalFacing().getHorizontalAngle();
            }
            skull.setYaw(yaw);

            if (!worldIn.isRemote) {
                worldIn.spawnEntity(skull);
            }
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;

    }
}
