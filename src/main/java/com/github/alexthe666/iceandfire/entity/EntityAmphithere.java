package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.IceAndFireConfig;
import com.github.alexthe666.iceandfire.client.model.IFChainBuffer;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.*;
import com.github.alexthe666.iceandfire.entity.util.*;
import com.github.alexthe666.iceandfire.message.MessageDragonControl;
import com.github.alexthe666.iceandfire.message.MessageUpdateRidingState;
import com.github.alexthe666.iceandfire.util.ParticleHelper;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityAmphithere extends EntityTameable implements IAnimatedEntity, IPhasesThroughBlock, IFlapable, IDragonFlute, ISyncMount {

    private int animationTick;
    private Animation currentAnimation;
    private static final DataParameter<Integer> VARIANT = EntityDataManager.<Integer>createKey(EntityAmphithere.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FLYING = EntityDataManager.<Boolean>createKey(EntityAmphithere.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> FLAP_TICKS = EntityDataManager.<Integer>createKey(EntityAmphithere.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> CONTROL_STATE = EntityDataManager.createKey(EntityAmphithere.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.<Integer>createKey(EntityAmphithere.class, DataSerializers.VARINT);
    public float flapProgress;
    private int flapTicks = 0;
    public float groundProgress = 0;
    public float sitProgress = 0;
    public float diveProgress = 0;
    private int flightCooldown = 0;
    private int ticksInAir = 0;
    private boolean isFlying;
    @SideOnly(Side.CLIENT)
    public IFChainBuffer roll_buffer;
    @SideOnly(Side.CLIENT)
    public IFChainBuffer tail_buffer;
    @SideOnly(Side.CLIENT)
    public IFChainBuffer pitch_buffer;
    protected FlightBehavior flightBehavior = FlightBehavior.WANDER;
    private boolean changedFlightBehavior = false;
    @Nullable
    public BlockPos orbitPos = null;
    public float orbitRadius = 0.0F;
    private int ticksStill = 0;
    private int ridingTime = 0;
    private boolean hasInitiatedFlight = false;
    public boolean isFallen;
    public static Animation ANIMATION_BITE = Animation.create(15);
    public static Animation ANIMATION_BITE_RIDER = Animation.create(15);
    public static Animation ANIMATION_WING_BLAST = Animation.create(30);
    public static Animation ANIMATION_TAIL_WHIP = Animation.create(30);
    public static Animation ANIMATION_SPEAK = Animation.create(10);
    private boolean isSitting;
    public BlockPos homePos;
    public boolean hasHomePosition = false;
    protected int ticksCircling = 0;
    public static final ResourceLocation LOOT = LootTableList.register(new ResourceLocation("iceandfire", "amphithere"));

    public EntityAmphithere(World worldIn) {
        super(worldIn);
        this.setSize(2.5F, 1.25F);
        this.stepHeight = 1;
        if (FMLCommonHandler.instance().getSide().isClient()) {
            roll_buffer = new IFChainBuffer();
            pitch_buffer = new IFChainBuffer();
            tail_buffer = new IFChainBuffer();
        }
        updateNavigator(true);

    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        if (this.isFlying()) {
            if (world.isAirBlock(pos)) {
                return 10F;
            } else {
                return 0F;
            }
        } else {
            return super.getBlockPathWeight(pos);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        boolean flag = itemstack.getItem() == Items.NAME_TAG || itemstack.getItem() == Items.LEAD;
        if (flag) {
            itemstack.interactWithEntity(player, this, hand);
            return true;
        }
        if (itemstack.getItem() == Items.COOKIE) {
            if (this.getGrowingAge() == 0 && !isInLove()) {
                if (!this.world.isRemote) {
                    this.aiSit.setSitting(false);
                }
                this.setInLove(player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
            }
            return true;
        }
        if (itemstack.getItem() == Items.DYE && itemstack.getItemDamage() == EnumDyeColor.BROWN.getDyeDamage()) {
            this.heal(5);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
            if (!player.isCreative()) {
                itemstack.shrink(1);
            }
            return true;
        }
        if (!super.processInteract(player, hand)) {
            if (itemstack.getItem() == ModItems.dragon_stick && this.isOwner(player)) {
                if (player.isSneaking()) {
                    this.homePos = new BlockPos(this);
                    this.hasHomePosition = true;
                    player.sendStatusMessage(new TextComponentTranslation("amphithere.command.new_home", homePos.getX(), homePos.getY(), homePos.getZ()), true);
                    return true;
                }
                return true;
            }
            if (player.isSneaking() && this.isOwner(player)) {
                if (player.getHeldItem(hand).isEmpty()) {
                    int command = this.getCommand();
                    if (command < 2) {
                        this.setCommand(command + 1);
                    } else {
                        this.setCommand(0);
                    }
                    player.sendStatusMessage(new TextComponentTranslation("amphithere.command." + this.getCommand()), true);
                    this.playSound(SoundEvents.ENTITY_ZOMBIE_INFECT, 1, 1);
                    return true;
                }
                return true;
            } else if ((!this.isTamed() || this.isOwner(player)) && !this.isChild()) {
                player.startRiding(this, true);
                if (world.isRemote) {
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageUpdateRidingState(this.getEntityId(), true));
                }
                return true;
            }

        }
        return true;
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, this.aiSit = new EntityAISit(this));
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(1, new AmphithereAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(2, new AmphithereAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(3, new AmphithereAIFleePlayer(this, 32.0F, 0.8D, 1.8D));
        this.tasks.addTask(3, new AIFlyWander());
        this.tasks.addTask(3, new AIFlyCircle());
        this.tasks.addTask(3, new AILandWander(this, 1.0D));
        this.tasks.addTask(4, new EntityAIWatchClosestIgnoreRider(this, EntityLivingBase.class, 6.0F));
        this.tasks.addTask(4, new EntityAIMate(this, 1.0D));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new AmphithereAIHurtByTarget(this, false, new Class[0]));
    }

    public boolean isStill() {
        return Math.abs(this.motionX) < 0.05 && Math.abs(this.motionZ) < 0.05;
    }

    private void updateNavigator(boolean isOnGround) {
        if (isOnGround) {
            if (this.moveHelper instanceof GroundMoveHelper) {
                return;
            }
            this.moveHelper = new GroundMoveHelper(this);
            this.navigator = new PathNavigateClimber(this, world);
            return;
        }
        if (this.moveHelper instanceof FlyMoveHelper) {
            return;
        }
        this.moveHelper = new FlyMoveHelper(this);
        this.navigator = new PathNavigateFlyingCreature(this, world);
    }

    public boolean onLeaves() {
        IBlockState state = world.getBlockState(this.getPosition().down());
        return state.getBlock().isLeaves(state, world, this.getPosition().down());
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (source.getTrueSource() instanceof EntityPlayer) {
            if (!world.isRemote && !this.isTamed()  && this.isFlying() && !isOnGround() && this.getPassengers().isEmpty() && source.isProjectile()) {
                this.isFallen = true;
            }
            if (this.isTamed() && this.isRidingPlayer((EntityPlayer) source.getTrueSource())) {
                return false;
            }
        } else if (source.getTrueSource() == this.getControllingPassenger()) {
            return false;
        }
        return super.attackEntityFrom(source, damage);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if (this.isPassenger(passenger) && this.isTamed()) {
            this.rotationYaw = passenger.rotationYaw;
        }
        if (!this.world.isRemote && !this.isTamed() && passenger instanceof EntityPlayer && this.getAnimation() == NO_ANIMATION && rand.nextInt(15) == 0) {
            this.setAnimation(ANIMATION_BITE_RIDER);
        }
        if (!this.world.isRemote && this.getAnimation() == ANIMATION_BITE_RIDER && this.getAnimationTick() == 6 && !this.isTamed()) {
            passenger.attackEntityFrom(DamageSource.causeMobDamage(this), (float)IceAndFireConfig.ENTITY_SETTINGS.amphithereTameDamage);
        }
        float pitch_forward = 0;
        if (this.rotationPitch > 0 && this.isFlying()) {
            pitch_forward = (rotationPitch / 45F) * 0.45F;
        }
        float scaled_ground = this.groundProgress * 0.1F;
        float radius = (this.isTamed() ? 0.5F : 0.3F) - scaled_ground * 0.5F + pitch_forward;
        float angle = (0.01745329251F * this.renderYawOffset);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        passenger.setPosition(this.posX + extraX, this.posY + 0.7F - scaled_ground * 0.14F + pitch_forward, this.posZ + extraZ);

    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.COOKIE;
    }


    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL && this.getAttackTarget() != null) {
            this.setAttackTarget(null);
        }
        if (this.isInWater() && this.isJumping) {
            this.motionY += 0.1D;
        }
        if (this.isChild() && this.getAttackTarget() != null) {
            this.setAttackTarget(null);
        }
        if (this.isInLove()) {
            this.setFlying(false);
        }
        if (this.isSitting() && this.getAttackTarget() != null) {
            this.setAttackTarget(null);
        }
        boolean flapping = this.isFlapping();
        boolean flying = !isRiding() && (isFlying() && !isOnGround() || !isOnGround() && !onLeaves());
        boolean diving = flying && this.motionY <= -0.1F || this.isFallen;
        boolean sitting = isSitting() && !isFlying();
        boolean notGrounded = isFlying() || this.getAnimation() == ANIMATION_WING_BLAST;
        if (!world.isRemote) {
            if (isSitting() && (this.getCommand() != 1 || this.getControllingPassenger() != null)) {
                this.aiSit.setSitting(false);
            }
            if (!isSitting() && this.getCommand() == 1 && this.getControllingPassenger() == null) {
                this.aiSit.setSitting(true);
            }
            if (this.isSitting()) {
                getNavigator().clearPath();
                getMoveHelper().action = EntityMoveHelper.Action.WAIT;
            }
            if (this.flightBehavior == FlightBehavior.CIRCLE) {
                ticksCircling++;
            } else {
                ticksCircling = 0;
            }
        }
        if (flying) {
            ticksInAir++;
        } else {
            ticksInAir = 0;
        }
        if (sitting && sitProgress < 20.0F) {
            sitProgress += 0.5F;
        } else if (!sitting && sitProgress > 0.0F) {
            sitProgress -= 0.5F;
        }
        if (flightCooldown > 0) {
            flightCooldown--;
        }
        if (this.getRider() != null && !this.isTamed()) {
            ridingTime++;
        }
        if (this.getRider() == null) {
            ridingTime = 0;
        }
        if (!this.isTamed() && ridingTime > IceAndFireConfig.ENTITY_SETTINGS.amphithereTameTime && this.getRider() != null && this.getRider() instanceof EntityPlayer) {
            this.world.setEntityState(this, (byte) 45);
            this.setTamedBy((EntityPlayer) this.getRider());
        }
        if (world.isRemote) {
            this.updateClientControls();
        }
        if (isStill()) {
            this.ticksStill++;
        } else {
            this.ticksStill = 0;
        }
        if (isFlying && this.onGround) {
            this.setFlying(false);
        }
        if (!this.isFlying() && !this.isChild() && !this.isTamed() && ((isOnGround() && this.rand.nextInt(200) == 0 && flightCooldown == 0 && this.getRider() == null && !this.isAIDisabled() && canMove()) || this.posY < -1)) {
            this.motionY += 0.5F;
            this.hasInitiatedFlight = true;
        }
        if (notGrounded && groundProgress > 0.0F) {
            groundProgress -= 2F;
        } else if (!notGrounded && groundProgress < 20.0F) {
            groundProgress += 2F;
        }
        if (diving && diveProgress < 20.0F) {
            diveProgress += 1F;
        } else if (!diving && diveProgress > 0.0F) {
            diveProgress -= 1F;
        }
        if (this.isFallen && this.flightBehavior != FlightBehavior.NONE) {
            this.flightBehavior = FlightBehavior.NONE;
        }
        if (flying) {
            Entity controllingPassenger = this.getControllingPassenger();
            if (controllingPassenger instanceof EntityPlayer) {
                if (isFlying()) {
                    float pitch = this.getControllingPassenger().rotationPitch;

                    this.rotationPitch = pitch / 2;

                    if (pitch > 25 && this.motionY > -1.0F) {
                        if (this.motionY > 0) {
                            this.motionY = 0;
                        }
                        this.motionY -= 0.1D;
                    }
                    if (pitch < -25 && this.motionY < 1.0F) {
                        if (this.motionY < 0) {
                            this.motionY = 0;
                        }
                        this.motionY += 0.1D;
                    }
                }
            } else if (this.isFallen) {
                this.motionY -= 0.2F;
                this.rotationPitch = Math.max(this.rotationPitch + 5, 75);
            } else if (this.flightBehavior == FlightBehavior.NONE) {
                this.motionY -= 0.3F;
            }
            if (!isFlying() && (hasInitiatedFlight || ticksInAir > 25)) {
                setFlying(true);
            }
            updateNavigator(false);
        } else {
            if (this.isFallen) {
                setFlying(false);
                if (!isTamed()) {
                    flightCooldown = 12000;
                }
                this.isFallen = false;
            }
            updateNavigator(true);
        }
        if ((this.hasHomePosition || this.getCommand() == 2) && this.flightBehavior == FlightBehavior.WANDER) {
            this.flightBehavior = FlightBehavior.CIRCLE;
        }
        if (flapping && flapProgress < 10.0F) {
            flapProgress += 1F;
        } else if (!flapping && flapProgress > 0.0F) {
            flapProgress -= 1F;
        }
        if (flapTicks > 0) {
            flapTicks--;
        }
        renderYawOffset = rotationYaw;
        if (world.isRemote) {
            if (!isOnGround()) {
                roll_buffer.calculateChainFlapBuffer(this.isBeingRidden() ? 55 : 90, 3, 10F, 0.5F, this);
                pitch_buffer.calculateChainWaveBuffer(90, 10, 10F, 0.5F, this);
            }
            tail_buffer.calculateChainSwingBuffer(70, 20, 5F, this);
        }
        if (changedFlightBehavior) {
            changedFlightBehavior = false;
        }
        if (!flapping && flying && (this.motionY > 0.15F || this.motionY > 0 && this.ticksExisted % 200 == 0)) {
            flapWings();
        }
        AnimationHandler.INSTANCE.updateAnimations(this);
    }

    @Override
    public boolean canBeSteered() {
        return true;
    }

    @Override
    public boolean hasNoGravity() {
        return isFlying();
    }

    @Override
    public boolean isFlapping() {
        return flapTicks > 0;
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
        if (!this.world.isRemote) {
            this.aiSit.setSitting(command == 1);
        }
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void flapWings() {
        this.flapTicks = 20;
    }

    public boolean isSitting() {
        if (world.isRemote) {
            this.isSitting = (this.dataManager.get(TAMED) & 1) != 0;
            return this.isSitting;
        }
        return this.isSitting;
    }

    public void setSitting(boolean sitting) {
        if (!world.isRemote) {
            this.isSitting = sitting;
        }
        byte b0 = this.dataManager.get(TAMED);
        if (sitting) {
            this.dataManager.set(TAMED, (byte) (b0 | 1));
        } else {
            this.dataManager.set(TAMED, (byte) (b0 & -2));
        }
    }

    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                if (this.getAttackTarget() != passenger) {
                    EntityPlayer player = (EntityPlayer) passenger;
                    if (this.isTamed() && this.getOwnerId() != null && this.getOwnerId().equals(player.getUniqueID())) {
                        return player;
                    }
                }
            } else {
                return passenger;
            }
        }
        return null;
    }

    public boolean isPlayerControlled() {
        return this.getControllingPassenger() instanceof EntityPlayer;
    }

    @Nullable
    public Entity getRider() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                return passenger;
            }
        }
        return null;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(IceAndFireConfig.ENTITY_SETTINGS.amphithereMaxHealth);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(IceAndFireConfig.ENTITY_SETTINGS.amphithereAttackStrength);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(FLYING, false);
        this.dataManager.register(FLAP_TICKS, 0);
        this.dataManager.register(CONTROL_STATE, (byte) 0);
        this.dataManager.register(COMMAND, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setBoolean("Flying", this.isFlying());
        compound.setInteger("FlightCooldown", flightCooldown);
        compound.setInteger("RidingTime", ridingTime);
        compound.setBoolean("HasHomePosition", this.hasHomePosition);
        if (homePos != null && this.hasHomePosition) {
            compound.setInteger("HomeAreaX", homePos.getX());
            compound.setInteger("HomeAreaY", homePos.getY());
            compound.setInteger("HomeAreaZ", homePos.getZ());
        }
        compound.setInteger("Command", this.getCommand());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        this.setFlying(compound.getBoolean("Flying"));
        this.flightCooldown = compound.getInteger("FlightCooldown");
        this.ridingTime = compound.getInteger("RidingTime");
        this.hasHomePosition = compound.getBoolean("HasHomePosition");
        if (this.hasHomePosition && compound.getInteger("HomeAreaX") != 0 && compound.getInteger("HomeAreaY") != 0 && compound.getInteger("HomeAreaZ") != 0) {
           this.homePos = new BlockPos(compound.getInteger("HomeAreaX"), compound.getInteger("HomeAreaY"), compound.getInteger("HomeAreaZ"));
        }
        this.setCommand(compound.getInteger("Command"));
    }

    public boolean isRidingPlayer(EntityPlayer player) {
        return this.getControllingPassenger() instanceof EntityPlayer
                && this.getControllingPassenger().getUniqueID().equals(player.getUniqueID())
                && this.isTamed();
    }

    @Override
    public boolean getCanSpawnHere() {
        int i = MathHelper.floor(this.posX);
        int j = MathHelper.floor(this.getEntityBoundingBox().minY);
        int k = MathHelper.floor(this.posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        return this.world.canBlockSeeSky(blockpos.up());
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.getAnimation() == ANIMATION_BITE && this.getAttackTarget() != null && this.getAnimationTick() == 7) {
            double dist = this.getDistanceSq(this.getAttackTarget());
            if (dist < 10) {
                this.getAttackTarget().knockBack(this, 0.6F, MathHelper.sin(this.rotationYaw * 0.017453292F), -MathHelper.cos(this.rotationYaw * 0.017453292F));
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
            }
        }
        if (this.getAnimation() == ANIMATION_WING_BLAST && this.getAnimationTick() == 5) {
            this.playSound(ModSounds.AMPHITHERE_GUST, 1, 1);
        }
        if ((this.getAnimation() == ANIMATION_BITE || this.getAnimation() == ANIMATION_BITE_RIDER) && this.getAnimationTick() == 1) {
            this.playSound(ModSounds.AMPHITHERE_BITE, 1, 1);
        }
        if (this.getAnimation() == ANIMATION_WING_BLAST && this.getAttackTarget() != null && this.getAnimationTick() > 5 && this.getAnimationTick() < 22) {
            double dist = this.getDistanceSq(this.getAttackTarget());
            if (dist < 25) {
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), ((float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() / 2));
                this.getAttackTarget().isAirBorne = true;
                float f = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D);
                this.getAttackTarget().motionX /= 2.0D;
                this.getAttackTarget().motionZ /= 2.0D;
                this.getAttackTarget().motionX -= 0.5 / (double) f;
                this.getAttackTarget().motionZ -= 0.5 / (double) f;

                if (this.getAttackTarget().onGround) {
                    this.getAttackTarget().motionY /= 2.0D;
                    this.getAttackTarget().motionY += 4;

                    if (this.getAttackTarget().motionY > 0.4000000059604645D) {
                        this.getAttackTarget().motionY = 0.4000000059604645D;
                    }
                }
            }
        }
        if (this.getAnimation() == ANIMATION_TAIL_WHIP && this.getAttackTarget() != null && this.getAnimationTick() == 7) {
            double dist = this.getDistanceSq(this.getAttackTarget());
            if (dist < 10) {
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
                this.getAttackTarget().isAirBorne = true;
                float f = MathHelper.sqrt(0.5 * 0.5 + 0.5 * 0.5);
                this.getAttackTarget().motionX /= 2.0D;
                this.getAttackTarget().motionZ /= 2.0D;
                this.getAttackTarget().motionX -= 0.5 / (double) f * 4;
                this.getAttackTarget().motionZ -= 0.5 / (double) f * 4;

                if (this.getAttackTarget().onGround) {
                    this.getAttackTarget().motionY /= 2.0D;
                    this.getAttackTarget().motionY += 4;

                    if (this.getAttackTarget().motionY > 0.4000000059604645D) {
                        this.getAttackTarget().motionY = 0.4000000059604645D;
                    }
                }
            }
        }
        if (world.isRemote) {
            this.updateClientControls();
        }
        if (this.up()) {
            if (!this.isFlying()) {
                this.motionY += 1F;
                hasInitiatedFlight = true;
            }
        }
        if (this.dismount()) {
            if (this.isFlying()) {
                if (isOnGround()) {
                    this.setFlying(false);
                } else {
                    this.setCommand(2);
                }
            }
        }
        if (this.getRider() != null && this.getRider().isSneaking()) {
            this.getRider().setSneaking(false);
            this.getRider().dismountRidingEntity();
            if (world.isRemote) {
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageUpdateRidingState(this.getEntityId(), false));
            }
        }
        if (this.attack() && this.getControllingPassenger() instanceof EntityPlayer) {
            EntityLivingBase target = DragonUtils.riderLookingAtEntity(this, (EntityPlayer) this.getControllingPassenger(), 2.5D);
            if (this.getAnimation() != ANIMATION_BITE) {
                this.setAnimation(ANIMATION_BITE);
            }
            if (target != null) {
                target.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
            }
        }
        if (this.getAttackTarget() != null) {
            if (this.isOwner(this.getAttackTarget()) || DragonUtils.isControllingPassenger(this, this.getAttackTarget())) {
                this.setAttackTarget(null);
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() != ANIMATION_BITE && this.getAnimation() != ANIMATION_TAIL_WHIP && this.getAnimation() != ANIMATION_WING_BLAST && !this.isPlayerControlled()) {
            if (rand.nextBoolean()) {
                this.setAnimation(ANIMATION_BITE);
            } else {
                this.setAnimation(this.getRNG().nextBoolean() || this.isFlying() ? ANIMATION_WING_BLAST : ANIMATION_TAIL_WHIP);
            }
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected void updateClientControls() {
        Minecraft mc = Minecraft.getMinecraft();
        if (this.isRidingPlayer(mc.player)) {
            byte previousState = getControlState();
            up(mc.gameSettings.keyBindJump.isKeyDown());
            down(ModKeys.dragon_down.isKeyDown());
            attack(ModKeys.dragon_strike.isKeyDown());
            dismount(mc.gameSettings.keyBindSneak.isKeyDown());
            byte controlState = getControlState();
            if (controlState != previousState) {
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonControl(this.getEntityId(), controlState, posX, posY, posZ));
            }
        }
        if (this.getRidingEntity() != null && this.getRidingEntity() == mc.player) {
            byte previousState = getControlState();
            dismount(mc.gameSettings.keyBindSneak.isKeyDown());
            byte controlState = getControlState();
            if (controlState != previousState) {
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonControl(this.getEntityId(), controlState, posX, posY, posZ));
            }
        }
    }

    public boolean isFlying() {
        if (world.isRemote) {
            return this.isFlying = this.dataManager.get(FLYING);
        }
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
        if (!world.isRemote) {
            this.isFlying = flying;
        }
        hasInitiatedFlight = false;
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean up() {
        return (dataManager.get(CONTROL_STATE) & 1) == 1;
    }

    public boolean down() {
        return (dataManager.get(CONTROL_STATE) >> 1 & 1) == 1;
    }

    public boolean attack() {
        return (dataManager.get(CONTROL_STATE) >> 2 & 1) == 1;
    }

    public boolean dismount() {
        return (dataManager.get(CONTROL_STATE) >> 3 & 1) == 1;
    }

    public void up(boolean up) {
        setStateField(0, up);
    }

    public void down(boolean down) {
        setStateField(1, down);
    }

    public void attack(boolean attack) {
        setStateField(2, attack);
    }

    public void dismount(boolean dismount) {
        setStateField(3, dismount);
    }

    private void setStateField(int i, boolean newState) {
        byte prevState = dataManager.get(CONTROL_STATE);
        if (newState) {
            dataManager.set(CONTROL_STATE, (byte) (prevState | (1 << i)));
        } else {
            dataManager.set(CONTROL_STATE, (byte) (prevState & ~(1 << i)));
        }
    }

    private byte getControlState() {
        return dataManager.get(CONTROL_STATE);
    }

    public void setControlState(byte state) {
        dataManager.set(CONTROL_STATE, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox().grow(5.0, 3.0, 5.0);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.AMPHITHERE_IDLE;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.AMPHITHERE_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return ModSounds.AMPHITHERE_DIE;
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        this.animationTick = tick;
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
        return new Animation[]{ANIMATION_BITE, ANIMATION_BITE_RIDER, ANIMATION_WING_BLAST, ANIMATION_TAIL_WHIP, ANIMATION_SPEAK};
    }

    @Override
    public void playLivingSound() {
        if (this.getAnimation() == this.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
        }
        super.playLivingSound();
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        if (this.getAnimation() == this.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
        }
        super.playHurtSound(source);
    }

    public boolean isBlinking() {
        return this.ticksExisted % 50 > 40;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityAmphithere amphithere = new EntityAmphithere(world);
        amphithere.setVariant(this.getRNG().nextInt(5));
        return amphithere;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 10;
    }


    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setVariant(this.getRNG().nextInt(5));
        return livingdata;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }


    public BlockPos getPositionRelativeToGround(double x, double z) {
        BlockPos pos = new BlockPos(x, this.posY, z);

        pos = getPositionWithAdjustedHeight(pos);

        int minHeightRelativeToGround = 6 + this.rand.nextInt(6);
        for (int relativeY = 0; relativeY < minHeightRelativeToGround; relativeY++) {
            if (!this.world.isAirBlock(pos.down(relativeY))) {
                return pos.up(minHeightRelativeToGround - relativeY);
            }
        }

        return pos;
    }

    public BlockPos getPositionWithAdjustedHeight(BlockPos pos) {
        int allowableHeightFromGround = IceAndFireConfig.ENTITY_SETTINGS.maxAmphithereFlight - this.world.getSeaLevel();
        BlockPos groundPos = this.world.getHeight(pos);
        int maxFlightHeight = Math.max(IceAndFireConfig.ENTITY_SETTINGS.maxAmphithereFlight, groundPos.getY() + allowableHeightFromGround);
        return new BlockPos(pos.getX(), Math.min(pos.getY(), maxFlightHeight), pos.getY());
    }

    public BlockPos getPositionInOrbit(BlockPos orbit) {
        float possibleOrbitRadius = (this.orbitRadius + 10.0F);
        float radius = 10;
        if (this.getCommand() == 2) {
            if (this.getOwner() != null) {
                orbit = this.getOwner().getPosition().up(7);
                radius = 5;
            }
        } else if (this.hasHomePosition) {
            orbit = this.homePos.up(30);
            radius = 30;
        }
        float angle = (0.01745329251F * possibleOrbitRadius);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(orbit.getX() + extraX, orbit.getY(), orbit.getZ() + extraZ);
        this.orbitRadius = possibleOrbitRadius;
        return radialPos;
    }

    @Override
    public boolean canPhaseThroughBlock(IBlockState state, World world, BlockPos pos) {
        return state.getMaterial() == Material.LEAVES;
    }

    @Override
    public void travel(float strafe, float forward, float vertical) {
        if (!this.canMove() && !this.isBeingRidden()) {
            strafe = 0;
            forward = 0;
            vertical = 0;
            super.travel(strafe, vertical, forward);
            return;
        }
        if (this.isPlayerControlled() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase) this.getControllingPassenger();
            if (controller != null) {
                strafe = controller.moveStrafing * 0.5F;
                forward = controller.moveForward;
                if (!isOnGround()) {
                    strafe = 0;
                    forward = 1.5F;
                }
                if (forward <= 0.0F) {
                    forward *= 0.25F;
                }
                if (this.isFlying()) {
                    motionX *= 1.06;
                    motionZ *= 1.06;
                }
                jumpMovementFactor = 0.05F;
                this.setAIMoveSpeed(isOnGround() ? (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() : 2);
                super.travel(strafe, 0, forward);
                return;
            }
        }
        super.travel(strafe, forward, vertical);
    }

    public boolean canMove() {
        return !this.isPlayerControlled() && sitProgress == 0 && !this.isSitting();
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 45) {
            this.playEffect();
        } else {
            super.handleStatusUpdate(id);
        }
    }

    private void playEffect() {
        EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;

        for (int i = 0; i < 7; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            ParticleHelper.spawnParticle(this.world, enumparticletypes, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
        }
    }

    @Override
    public void onHearFlute(EntityPlayer player) {
        if (!isOnGround() && this.isTamed()) {
            this.isFallen = true;
        }
    }

    public boolean isOnGround() {
        return this.onGround || !world.isAirBlock(this.getPosition().down());
    }

    public enum FlightBehavior {
        CIRCLE,
        WANDER,
        NONE;
    }

    class AILandWander extends EntityAIWander {

        public AILandWander(EntityAmphithere amphithere, double speed) {
            super(amphithere, speed, 10);
        }

        @Override
        public boolean shouldExecute() {
            return isOnGround() && EntityAmphithere.this.canMove() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return isOnGround() && EntityAmphithere.this.canMove() && super.shouldContinueExecuting();
        }

    }

    class AIFlyWander extends EntityAIBase {
        BlockPos target;

        public AIFlyWander() {
            this.setMutexBits(0);
        }

        public boolean shouldExecute() {
            if (EntityAmphithere.this.flightBehavior != FlightBehavior.WANDER || !EntityAmphithere.this.canMove()) {
                return false;
            }
            if (EntityAmphithere.this.isFlying()) {
                target = EntityAmphithere.this.getPositionRelativeToGround(EntityAmphithere.this.posX + EntityAmphithere.this.rand.nextInt(30) - 15, EntityAmphithere.this.posZ + EntityAmphithere.this.rand.nextInt(30) - 15);
                EntityAmphithere.this.orbitPos = null;
                return (!EntityAmphithere.this.getMoveHelper().isUpdating() || EntityAmphithere.this.ticksStill >= 50);
            } else {
                return false;
            }
        }

        protected boolean isDirectPathBetweenPoints(Entity e) {
            RayTraceResult rayTrace = world.rayTraceBlocks(e.getPositionVector(), new Vec3d(target).add(0.5, 0.5, 0.5), false);
            if (rayTrace != null && rayTrace.hitVec != null) {
                BlockPos sidePos = rayTrace.getBlockPos();
                BlockPos pos = new BlockPos(rayTrace.hitVec);
                if (world.isAirBlock(pos) || world.isAirBlock(sidePos) || world.getBlockState(pos).getMaterial() == Material.LEAVES || world.getBlockState(sidePos).getMaterial() == Material.LEAVES) {
                    return true;
                } else {
                    return rayTrace.typeOfHit != RayTraceResult.Type.MISS;
                }
            }
            return true;
        }

        public boolean shouldContinueExecuting() {
            return false;
        }

        public void updateTask() {
            if (!isDirectPathBetweenPoints(EntityAmphithere.this)) {
                target = EntityAmphithere.this.getPositionRelativeToGround(EntityAmphithere.this.posX + EntityAmphithere.this.rand.nextInt(30) - 15, EntityAmphithere.this.posZ + EntityAmphithere.this.rand.nextInt(30) - 15);
            }
            if (world.isAirBlock(target)) {
                getMoveHelper().setMoveTo((double) target.getX() + 0.5D, (double) target.getY() + 0.5D, (double) target.getZ() + 0.5D, 0.25D);
                if (EntityAmphithere.this.getAttackTarget() == null) {
                    EntityAmphithere.this.getLookHelper().setLookPosition((double) target.getX() + 0.5D, (double) target.getY() + 0.5D, (double) target.getZ() + 0.5D, 180.0F, 20.0F);
                }
            }
        }
    }

    class AIFlyCircle extends EntityAIBase {
        BlockPos target;

        public AIFlyCircle() {
            this.setMutexBits(0);
        }

        public boolean shouldExecute() {
            if (EntityAmphithere.this.flightBehavior != FlightBehavior.CIRCLE || !EntityAmphithere.this.canMove()) {
                return false;
            }
            if (EntityAmphithere.this.isFlying()) {
                EntityAmphithere.this.orbitPos = EntityAmphithere.this.getPositionRelativeToGround(EntityAmphithere.this.posX + EntityAmphithere.this.rand.nextInt(30) - 15, EntityAmphithere.this.posZ + EntityAmphithere.this.rand.nextInt(30) - 15);
                target = EntityAmphithere.this.getPositionInOrbit(EntityAmphithere.this.orbitPos);
                return true;
            } else {
                return false;
            }
        }

        private boolean isDirectPathBetweenPoints() {
            RayTraceResult rayTrace = world.rayTraceBlocks(EntityAmphithere.this.getPositionVector(), new Vec3d(target).add(0.5, 0.5, 0.5), false);
            if (rayTrace != null && rayTrace.hitVec != null) {
                BlockPos sidePos = rayTrace.getBlockPos();
                BlockPos pos = new BlockPos(rayTrace.hitVec);
                if (world.isAirBlock(pos) || world.isAirBlock(sidePos) || world.getBlockState(pos).getMaterial() == Material.LEAVES || world.getBlockState(sidePos).getMaterial() == Material.LEAVES) {
                    return true;
                } else {
                    return rayTrace.typeOfHit != RayTraceResult.Type.MISS;
                }
            }
            return true;
        }

        public boolean shouldContinueExecuting() {
            return false;
        }

        public void updateTask() {
            if (!isDirectPathBetweenPoints()) {
                target = EntityAmphithere.this.getPositionInOrbit(EntityAmphithere.this.orbitPos);
            }
            if (world.isAirBlock(target)) {
                getMoveHelper().setMoveTo((double) target.getX() + 0.5D, (double) target.getY() + 0.5D, (double) target.getZ() + 0.5D, 0.25D);
                if (EntityAmphithere.this.getAttackTarget() == null) {
                    EntityAmphithere.this.getLookHelper().setLookPosition((double) target.getX() + 0.5D, (double) target.getY() + 0.5D, (double) target.getZ() + 0.5D, 180.0F, 20.0F);
                }
            }
        }
    }

    class GroundMoveHelper extends EntityMoveHelper {
        public GroundMoveHelper(EntityAmphithere entity) {
            super(entity);
        }

        @Override
        public void onUpdateMoveHelper() {
            if (!EntityAmphithere.this.canMove()) {
                return;
            }
            super.onUpdateMoveHelper();
        }
    }

    class FlyMoveHelper extends EntityMoveHelper {
        public FlyMoveHelper(EntityAmphithere entity) {
            super(entity);
            this.speed = IceAndFireConfig.ENTITY_SETTINGS.amphithereFlightSpeed;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (!EntityAmphithere.this.canMove()) {
                return;
            }
            if (this.action == EntityMoveHelper.Action.MOVE_TO) {
                double d0 = this.posX - EntityAmphithere.this.posX;
                double d1 = this.posY - EntityAmphithere.this.posY;
                double d2 = this.posZ - EntityAmphithere.this.posZ;
                double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                if (d3 < 6 && EntityAmphithere.this.getAttackTarget() == null) {
                    if (!EntityAmphithere.this.changedFlightBehavior && EntityAmphithere.this.flightBehavior == FlightBehavior.WANDER && EntityAmphithere.this.rand.nextInt(30) == 0) {
                        EntityAmphithere.this.flightBehavior = FlightBehavior.CIRCLE;
                        EntityAmphithere.this.changedFlightBehavior = true;
                    }
                    if (!EntityAmphithere.this.changedFlightBehavior && EntityAmphithere.this.flightBehavior == FlightBehavior.CIRCLE && EntityAmphithere.this.rand.nextInt(5) == 0 && ticksCircling > 150) {
                        EntityAmphithere.this.flightBehavior = FlightBehavior.WANDER;
                        EntityAmphithere.this.changedFlightBehavior = true;
                    }
                    if (EntityAmphithere.this.hasHomePosition && EntityAmphithere.this.flightBehavior != FlightBehavior.NONE || EntityAmphithere.this.getCommand() == 2) {
                        EntityAmphithere.this.flightBehavior = FlightBehavior.CIRCLE;
                    }
                }
                if (d3 < 1 && EntityAmphithere.this.getAttackTarget() == null) {
                    this.action = EntityMoveHelper.Action.WAIT;
                    EntityAmphithere.this.motionX *= 0.5D;
                    EntityAmphithere.this.motionY *= 0.5D;
                    EntityAmphithere.this.motionZ *= 0.5D;
                } else {
                    EntityAmphithere.this.motionX += d0 / d3 * 0.5D * this.speed;
                    EntityAmphithere.this.motionY += d1 / d3 * 0.5D * this.speed;
                    EntityAmphithere.this.motionZ += d2 / d3 * 0.5D * this.speed;
                    EntityAmphithere.this.rotationPitch = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
                    if (EntityAmphithere.this.getAttackTarget() == null) {
                        EntityAmphithere.this.rotationYaw = -((float) MathHelper.atan2(EntityAmphithere.this.motionX, EntityAmphithere.this.motionZ)) * (180F / (float) Math.PI);
                    } else {
                        double d4 = EntityAmphithere.this.getAttackTarget().posX - EntityAmphithere.this.posX;
                        double d5 = EntityAmphithere.this.getAttackTarget().posZ - EntityAmphithere.this.posZ;
                        EntityAmphithere.this.rotationYaw = -((float) MathHelper.atan2(d4, d5)) * (180F / (float) Math.PI);
                    }
                    EntityAmphithere.this.renderYawOffset = EntityAmphithere.this.rotationYaw;
                }
            }
        }
    }
}
