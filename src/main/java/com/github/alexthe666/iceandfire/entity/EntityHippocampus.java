package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.IEntityEffectCapability;
import com.github.alexthe666.iceandfire.api.InFCapabilities;
import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.*;
import com.github.alexthe666.iceandfire.entity.util.IDropArmor;
import com.github.alexthe666.iceandfire.message.MessageDragonControl;
import com.github.alexthe666.iceandfire.message.MessageHippogryphArmor;
import com.github.alexthe666.iceandfire.util.ParticleHelper;
import net.ilexiconn.llibrary.client.model.tools.ChainBuffer;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityHippocampus extends EntityTameable implements IAnimatedEntity, IDropArmor {

    private int animationTick;
    private Animation currentAnimation;
    public float onLandProgress;
    private boolean isLandNavigator;
    @SideOnly(Side.CLIENT)
    public ChainBuffer tail_buffer;
    private static final DataParameter<Integer> VARIANT = EntityDataManager.<Integer>createKey(EntityHippocampus.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SADDLE = EntityDataManager.<Boolean>createKey(EntityHippocampus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ARMOR = EntityDataManager.<Integer>createKey(EntityHippocampus.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CHESTED = EntityDataManager.<Boolean>createKey(EntityHippocampus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> CONTROL_STATE = EntityDataManager.createKey(EntityHippocampus.class, DataSerializers.BYTE);
    public EntityHippocampus.HippocampusInventory hippocampusInventory;
    private boolean isSitting;
    private boolean hasChestVarChanged = false;
    public float sitProgress;
    public int airBorneCounter;
    public static final ResourceLocation LOOT = LootTableList.register(new ResourceLocation("iceandfire", "hippocampus"));
    public static Animation ANIMATION_SPEAK;

    public EntityHippocampus(World worldIn) {
        super(worldIn);
        this.stepHeight = 1;
        this.spawnableBlock = Blocks.WATER;
        ANIMATION_SPEAK = Animation.create(15);
        this.setSize(1.95F, 0.95F);
        this.switchNavigator(true);
        this.tasks.addTask(0, new AquaticAITempt(this, 1.0D, Item.getItemFromBlock(Blocks.SPONGE), false));
        this.tasks.addTask(0, new AquaticAITempt(this, 1.0D, Items.PRISMARINE_CRYSTALS, false));
        this.tasks.addTask(1, new AquaticAIFindWaterTarget(this, 10, true));
        this.tasks.addTask(2, new AquaticAIGetInWater(this, 1.0D));
        this.tasks.addTask(3, new HippocampusAIWander(this, 1));
        this.tasks.addTask(4, new EntityAIMate(this, 1.0D));
        if (FMLCommonHandler.instance().getSide().isClient()) {
            tail_buffer = new ChainBuffer();
        }
        initHippocampusInv();
    }

    protected int getExperiencePoints(EntityPlayer player) {
        return 2;
    }

    public float getBlockPathWeight(BlockPos pos) {
        return this.world.getBlockState(pos.down()).getMaterial() == Material.WATER ? 10.0F : this.world.getLightBrightness(pos) - 0.5F;
    }

    public boolean isNotColliding() {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }

    public boolean isPushedByWater() {
        return false;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new PathNavigateAmphibious(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new EntityHippocampus.SwimmingMoveHelper();
            this.navigator = new PathNavigateSwimmer(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(ARMOR, 0);
        this.dataManager.register(SADDLE, Boolean.FALSE);
        this.dataManager.register(CHESTED, Boolean.FALSE);
        this.dataManager.register(CONTROL_STATE, (byte) 0);
    }

    public boolean canBeSteered() {
        return true;
    }

    private void initHippocampusInv() {
        EntityHippocampus.HippocampusInventory animalchest = this.hippocampusInventory;
        this.hippocampusInventory = new EntityHippocampus.HippocampusInventory("hippocampusInventory", 18, this);
        this.hippocampusInventory.setCustomName(this.getName());
        if (animalchest != null) {
            int i = Math.min(animalchest.getSizeInventory(), this.hippocampusInventory.getSizeInventory());
            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = animalchest.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    this.hippocampusInventory.setInventorySlotContents(j, itemstack.copy());
                }
            }

            if (world.isRemote) {
                ItemStack saddle = animalchest.getStackInSlot(0);
                ItemStack chest = animalchest.getStackInSlot(1);
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(animalchest.getStackInSlot(2))));
            }
        }
    }

    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer && this.getAttackTarget() != passenger) {
                return passenger;
            }
        }
        return null;
    }

    public int getIntFromArmor(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == Items.IRON_HORSE_ARMOR) {
            return 1;
        }
        if (!stack.isEmpty() && stack.getItem() == Items.GOLDEN_HORSE_ARMOR) {
            return 2;
        }
        if (!stack.isEmpty() && stack.getItem() == Items.DIAMOND_HORSE_ARMOR) {
            return 3;
        }
        return 0;
    }

    public boolean replaceItemInInventory(int inventorySlot, @Nullable ItemStack itemStackIn) {
        int j = inventorySlot - 500 + 2;
        if (j >= 0 && j < this.hippocampusInventory.getSizeInventory()) {
            this.hippocampusInventory.setInventorySlotContents(j, itemStackIn);
            return true;
        } else {
            return false;
        }
    }

    private void setStateField(int i, boolean newState) {
        byte prevState = dataManager.get(CONTROL_STATE);
        if (newState) {
            dataManager.set(CONTROL_STATE, (byte) (prevState | (1 << i)));
        } else {
            dataManager.set(CONTROL_STATE, (byte) (prevState & ~(1 << i)));
        }
    }

    public byte getControlState() {
        return dataManager.get(CONTROL_STATE);
    }

    public void setControlState(byte state) {
        dataManager.set(CONTROL_STATE, state);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
    }

    public boolean isRidingPlayer(EntityPlayer player) {
        return this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer && this.getControllingPassenger().getUniqueID().equals(player.getUniqueID());
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if (this.isPassenger(passenger)) {
            renderYawOffset = rotationYaw;
            this.rotationYaw = passenger.rotationYaw;
        }
        double ymod1 = this.onLandProgress * -0.02;
        passenger.setPosition(this.posX, this.posY + 0.6F + ymod1, this.posZ);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) {
            if (this.rand.nextInt(900) == 0 && this.deathTime == 0) {
                this.heal(1.0F);
            }
        }
        AnimationHandler.INSTANCE.updateAnimations(this);
        if (getControllingPassenger() != null && getControllingPassenger() instanceof EntityLivingBase && this.ticksExisted % 20 == 0) {
            ((EntityLivingBase) getControllingPassenger()).addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 30, 0, true, false));
        }
        if (!this.onGround) {
            airBorneCounter++;
        } else {
            airBorneCounter = 0;
        }
        if (world.isRemote) {
            this.updateClientControls();
        }
        if (world.isRemote) {
            tail_buffer.calculateChainSwingBuffer(40, 10, 1F, this);
        }
        if (this.up()) {
            if (!this.isInWater() && this.airBorneCounter == 0 && this.onGround) {
                this.jump();
            } else if (this.isInWater()) {
                this.motionY += 0.4D;
            }
        }
        if (this.down()) {
            this.motionY -= 0.4D;
        }
        if (this.isInWater() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!this.isInWater() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        boolean inWater = !this.isInWater();
        if (inWater && onLandProgress < 20.0F) {
            onLandProgress += 1F;
        } else if (!inWater && onLandProgress > 0.0F) {
            onLandProgress -= 1F;
        }
        boolean sitting = isSitting();
        if (sitting && sitProgress < 20.0F) {
            sitProgress += 0.5F;
        } else if (!sitting && sitProgress > 0.0F) {
            sitProgress -= 0.5F;
        }
        if (hasChestVarChanged && hippocampusInventory != null && !this.isChested()) {
            for (int i = 3; i < 18; i++) {
                if (!hippocampusInventory.getStackInSlot(i).isEmpty()) {
                    if (!world.isRemote) {
                        this.entityDropItem(hippocampusInventory.getStackInSlot(i), 1);
                    }
                    hippocampusInventory.removeStackFromSlot(i);
                }
            }
            hasChestVarChanged = false;
        }
    }

    public boolean up() {
        return (dataManager.get(CONTROL_STATE) & 1) == 1;
    }

    public boolean down() {
        return (dataManager.get(CONTROL_STATE) >> 1 & 1) == 1;
    }

    public boolean dismount() {
        return (dataManager.get(CONTROL_STATE) >> 2 & 1) == 1;
    }


    public boolean isBlinking() {
        return this.ticksExisted % 50 > 43;
    }

    public boolean getCanSpawnHere() {
        return this.posY > 30 && this.posY < (double) this.world.getSeaLevel();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setBoolean("Chested", this.isChested());
        compound.setBoolean("Saddled", this.isSaddled());
        compound.setInteger("Armor", this.getArmor());
        if (hippocampusInventory != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.hippocampusInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.hippocampusInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.setByte("Slot", (byte) i);
                    itemstack.writeToNBT(nbttagcompound);
                    nbttaglist.appendTag(nbttagcompound);
                }
            }
            compound.setTag("Items", nbttaglist);
        }
        if (!this.getCustomNameTag().isEmpty()) {
            compound.setString("CustomName", this.getCustomNameTag());
        }

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        this.setChested(compound.getBoolean("Chested"));
        this.setSaddled(compound.getBoolean("Saddled"));
        this.setArmor(compound.getInteger("Armor"));
        if (hippocampusInventory != null) {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initHippocampusInv();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                this.hippocampusInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        } else {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initHippocampusInv();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                this.initHippocampusInv();
                this.hippocampusInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
                //this.setArmorInSlot(j, this.getIntFromArmor(ItemStack.loadItemStackFromNBT(nbttagcompound)));
                ItemStack saddle = hippocampusInventory.getStackInSlot(0);
                ItemStack chest = hippocampusInventory.getStackInSlot(1);
                if (world.isRemote) {
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(hippocampusInventory.getStackInSlot(2))));
                }
            }
        }
    }


    public boolean isSaddled() {
        return this.dataManager.get(SADDLE);
    }

    public void setSaddled(boolean saddle) {
        this.dataManager.set(SADDLE, saddle);
    }

    public boolean isChested() {
        return this.dataManager.get(CHESTED);
    }

    public void setChested(boolean chested) {
        this.dataManager.set(CHESTED, chested);
        this.hasChestVarChanged = true;
    }

    public boolean isSitting() {
        if (world.isRemote) {
            boolean isSitting = ((Byte) this.dataManager.get(TAMED) & 1) != 0;
            this.isSitting = isSitting;
            return isSitting;
        }
        return isSitting;
    }

    public void setSitting(boolean sitting) {
        if (!world.isRemote) {
            this.isSitting = sitting;
        }
        byte b0 = (Byte) this.dataManager.get(TAMED);
        if (sitting) {
            this.dataManager.set(TAMED, (byte) (b0 | 1));
        } else {
            this.dataManager.set(TAMED, (byte) (b0 & -2));
        }
    }

    public int getArmor() {
        return this.dataManager.get(ARMOR);
    }

    public void setArmor(int armorType) {
        this.dataManager.set(ARMOR, armorType);
        double armorValue = 0;
        switch (armorType) {
            case 1:
                armorValue = 10;
                break;
            case 2:
                armorValue = 20;
                break;
            case 3:
                armorValue = 30;
        }
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(armorValue);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setVariant(this.getRNG().nextInt(6));
        return livingdata;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox().grow(2.0, 0.5, 2.0);
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{IAnimatedEntity.NO_ANIMATION, ANIMATION_SPEAK};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        if (ageable instanceof EntityHippocampus) {
            EntityHippocampus hippo = new EntityHippocampus(this.world);
            hippo.setVariant(this.getRNG().nextBoolean() ? this.getVariant() : ((EntityHippocampus) ageable).getVariant());
            return hippo;
        }
        return null;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isInWater() {
        return super.isInWater() || this.isInsideOfMaterial(Material.WATER) || this.isInsideOfMaterial(Material.CORAL);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        float f4;
        if (this.isSitting()) {
            super.travel(0, 0, 0);
            return;
        }
        if (this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase) this.getControllingPassenger();
            if (controller != null) {
                strafe = controller.moveStrafing * 0.5F;
                forward = controller.moveForward;
                if (forward <= 0.0F) {
                    forward *= 0.25F;
                }
                this.fallDistance = 0;
                if (this.isInWater()) {
                    this.moveRelative(strafe, vertical, forward, 1F);
                    f4 = 0.8F;
                    float d0 = 3;
                    if (!this.onGround) {
                        d0 *= 0.5F;
                    }
                    if (d0 > 0.0F) {
                        f4 += (0.54600006F - f4) * d0 / 3.0F;
                    }
                    //this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                    this.motionX *= f4;
                    this.motionX *= 0.900000011920929D;
                    this.motionY *= 0.900000011920929D;
                    this.motionY *= f4;
                    this.motionZ *= 0.900000011920929D;
                    this.motionZ *= f4;
                    motionY += 0.01185D;
                } else {
                    forward = controller.moveForward * 0.25F;
                    strafe = controller.moveStrafing * 0.125F;

                    this.setAIMoveSpeed(onGround ? (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() : 1);
                    super.travel(strafe, vertical, forward);
                    return;
                }
                this.setAIMoveSpeed(onGround ? (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() : 1);
                super.travel(strafe, vertical = 0, forward);
                this.prevLimbSwingAmount = this.limbSwingAmount;
                double deltaX = this.posX - this.prevPosX;
                double deltaZ = this.posZ - this.prevPosZ;
                double deltaY = this.posY - this.prevPosY;
                float delta = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 4.0F;
                if (delta > 1.0F) {
                    delta = 1.0F;
                }
                if(delta > 0.5D && this.isInWater() && this.isBeingRidden()){
                    this.doWaterSplashEffect();
                }
                this.limbSwingAmount += (delta - this.limbSwingAmount) * 0.4F;
                this.limbSwing += this.limbSwingAmount;
                return;
            }
        }
        if (this.isServerWorld()) {
            float f5;
            if (this.isInWater()) {
                this.moveRelative(strafe, vertical, forward, 0.1F);
                f4 = 0.6F;
                float d0 = (float) EnchantmentHelper.getDepthStriderModifier(this);
                if (d0 > 3.0F) {
                    d0 = 3.0F;
                }
                if (!this.onGround) {
                    d0 *= 0.5F;
                }
                if (d0 > 0.0F) {
                    f4 += (0.54600006F - f4) * d0 / 3.0F;
                }
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                this.motionX *= (double) f4;
                this.motionX *= 0.900000011920929D;
                this.motionY *= 0.900000011920929D;
                this.motionY *= (double) f4;
                this.motionZ *= 0.900000011920929D;
                this.motionZ *= (double) f4;
            } else {
                super.travel(strafe, vertical, forward);
            }
        }
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double deltaX = this.posX - this.prevPosX;
        double deltaZ = this.posZ - this.prevPosZ;
        double deltaY = this.posY - this.prevPosY;
        float delta = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 4.0F;
        if (delta > 1.0F) {
            delta = 1.0F;
        }
        this.limbSwingAmount += (delta - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;

    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.PRISMARINE_CRYSTALS;
    }

    public void playLivingSound() {
        if (this.getAnimation() == this.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
        }
        super.playLivingSound();
    }

    protected void playHurtSound(DamageSource source) {
        if (this.getAnimation() == this.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
        }
        super.playHurtSound(source);
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.PRISMARINE_CRYSTALS && this.getGrowingAge() == 0 && !isInLove()) {
            this.setSitting(false);
            this.setInLove(player);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return true;
        }
        if (stack.getItem() == Item.getItemFromBlock(Blocks.SPONGE) && stack.getMetadata() == 0) {
            if (!world.isRemote) {
                this.heal(5);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            else {
                for (int i = 0; i < 3; i++) {
                    ParticleHelper.spawnParticle(this.world, EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0, 0, 0, Item.getIdFromItem(stack.getItem()), 0);
                }
            }
            if (!this.isTamed() && this.getRNG().nextInt(3) == 0) {
                this.setTamedBy(player);
                if(this.world.isRemote) {
                    for (int i = 0; i < 6; i++) {
                        ParticleHelper.spawnParticle(this.world, EnumParticleTypes.HEART, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0, 0, 0);
                    }
                }
            }
            return true;

        }
        if (isOwner(player) && stack.getItem() == Items.PRISMARINE_CRYSTALS && this.getGrowingAge() == 0 && !isInLove()) {
            this.setSitting(false);
            this.setInLove(player);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return true;
        }
        if (isOwner(player) && stack.getItem() == Items.STICK) {
            this.setSitting(!this.isSitting());
            return true;
        }
        if(isOwner(player) && stack.isEmpty()) {
            if (player.isSneaking()) {
                this.openGUI(player);
                return true;
            } else if (this.isSaddled() && !this.isChild() && !player.isRiding()) {
                player.startRiding(this, true);
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    public void openGUI(EntityPlayer playerEntity) {
        if (!this.world.isRemote && (!this.isBeingRidden() || this.isPassenger(playerEntity))) {
            playerEntity.openGui(IceAndFire.INSTANCE, 5, this.world, this.getEntityId(), 0, 0);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateClientControls() {
        Minecraft mc = Minecraft.getMinecraft();
        if (this.isRidingPlayer(mc.player)) {
            byte previousState = getControlState();
            up(mc.gameSettings.keyBindJump.isKeyDown());
            dismount(mc.gameSettings.keyBindSneak.isKeyDown());
            down(ModKeys.dragon_down.isKeyDown());
            byte controlState = getControlState();
            if (controlState != previousState) {
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonControl(this.getEntityId(), controlState, posX, posY, posZ));
            }
        }
        if (this.getRidingEntity() != null && this.getRidingEntity() == mc.player) {
            byte previousState = getControlState();
            dismount(mc.gameSettings.keyBindSneak.isKeyDown());
            down(ModKeys.dragon_down.isKeyDown());
            byte controlState = getControlState();
            if (controlState != previousState) {
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonControl(this.getEntityId(), controlState, posX, posY, posZ));
            }
        }
    }

    public void up(boolean up) {
        setStateField(0, up);
    }

    public void down(boolean down) {
        setStateField(1, down);
    }

    public void dismount(boolean dismount) {
        setStateField(2, dismount);
    }

    public void refreshInventory() {
        ItemStack saddle = this.hippocampusInventory.getStackInSlot(0);
        ItemStack chest = this.hippocampusInventory.getStackInSlot(1);
        this.setSaddled(saddle.getItem() == Items.SADDLE && !saddle.isEmpty());
        this.setChested(chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty());
        this.setArmor(getIntFromArmor(this.hippocampusInventory.getStackInSlot(2)));
        if (this.world.isRemote) {
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle != null && saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest != null && chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(this.hippocampusInventory.getStackInSlot(2))));
        }
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return ModSounds.HIPPOCAMPUS_IDLE;
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return ModSounds.HIPPOCAMPUS_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return ModSounds.HIPPOCAMPUS_DIE;
    }

    class SwimmingMoveHelper extends EntityMoveHelper {
        private final EntityHippocampus hippo = EntityHippocampus.this;

        public SwimmingMoveHelper() {
            super(EntityHippocampus.this);
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == EntityMoveHelper.Action.MOVE_TO && !this.hippo.getNavigator().noPath() && !this.hippo.isBeingRidden()) {
                double distanceX = this.posX - this.hippo.posX;
                double distanceY = this.posY - this.hippo.posY;
                double distanceZ = this.posZ - this.hippo.posZ;
                double distance = Math.abs(distanceX * distanceX + distanceZ * distanceZ);
                double distanceWithY = (double) MathHelper.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
                distanceY = distanceY / distanceWithY;
                float angle = (float) (Math.atan2(distanceZ, distanceX) * 180.0D / Math.PI) - 90.0F;
                this.hippo.rotationYaw = this.limitAngle(this.hippo.rotationYaw, angle, 30.0F);
                this.hippo.setAIMoveSpeed((float) 1F);
                this.hippo.motionY += (double) this.hippo.getAIMoveSpeed() * distanceY * 0.1D;
                if (distance < (double) Math.max(1.0F, this.entity.width)) {
                    float f = this.hippo.rotationYaw * 0.017453292F;
                    this.hippo.motionX -= (double) (MathHelper.sin(f) * 0.35F);
                    this.hippo.motionZ += (double) (MathHelper.cos(f) * 0.35F);
                }
            } else if (this.action == EntityMoveHelper.Action.JUMPING) {
                this.entity.setAIMoveSpeed((float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
                if (this.entity.onGround) {
                    this.action = EntityMoveHelper.Action.WAIT;
                }
            } else {
                this.hippo.setAIMoveSpeed(0.0F);
            }
        }
    }

    public class HippocampusInventory extends ContainerHorseChest {

        public HippocampusInventory(String inventoryTitle, int slotCount, EntityHippocampus hippocampus) {
            super(inventoryTitle, slotCount);
            this.addInventoryChangeListener(new HippocampusInventoryListener(hippocampus));
        }


    }

    static class HippocampusInventoryListener implements IInventoryChangedListener {

        EntityHippocampus hippocampus;

        public HippocampusInventoryListener(EntityHippocampus hippocampus) {
            this.hippocampus = hippocampus;
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {
            hippocampus.refreshInventory();
        }
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (hippocampusInventory != null && !this.world.isRemote) {
            IEntityEffectCapability cap = InFCapabilities.getEntityEffectCapability(this);
            if(cap != null && cap.isStoned()) return;
            for (int i = 0; i < hippocampusInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = hippocampusInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }

    public void dropArmor(){
        IEntityEffectCapability cap = InFCapabilities.getEntityEffectCapability(this);
        if(cap != null && cap.isStoned()) return;
        if (hippocampusInventory != null && !this.world.isRemote) {
            for (int i = 0; i < hippocampusInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = hippocampusInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }
}
