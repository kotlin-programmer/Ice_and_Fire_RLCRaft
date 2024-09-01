package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.IceAndFireConfig;
import com.github.alexthe666.iceandfire.api.FoodUtils;
import com.github.alexthe666.iceandfire.api.IEntityEffectCapability;
import com.github.alexthe666.iceandfire.api.InFCapabilities;
import com.github.alexthe666.iceandfire.client.model.IFChainBuffer;
import com.github.alexthe666.iceandfire.client.model.util.LegSolverQuadruped;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateExperimentalGround;
import com.github.alexthe666.iceandfire.entity.explosion.BlockBreakExplosion;
import com.github.alexthe666.iceandfire.entity.util.*;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.github.alexthe666.iceandfire.enums.EnumDragonType;
import com.github.alexthe666.iceandfire.enums.EnumParticle;
import com.github.alexthe666.iceandfire.item.ItemDragonArmor;
import com.github.alexthe666.iceandfire.message.MessageDragonArmor;
import com.github.alexthe666.iceandfire.message.MessageDragonControl;
import com.github.alexthe666.iceandfire.message.MessageParticleFX;
import com.github.alexthe666.iceandfire.message.MessageUpdateRidingState;
import com.github.alexthe666.iceandfire.util.ParticleHelper;
import net.ilexiconn.llibrary.client.model.tools.ChainBuffer;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.ilexiconn.llibrary.server.entity.multipart.IMultipartEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class EntityDragonBase extends EntityTameable implements IMultipartEntity, IAnimatedEntity, IDragonFlute, IDeadMob, IVillagerFear, IAnimalFear, IDropArmor, ISyncMount {

    public static EntityEquipmentSlot[] ARMOR_SLOTS = { EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };
    private static final int FLIGHT_CHANCE_PER_TICK = 1500;
    private static final DataParameter<Integer> HUNGER = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> GENDER = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FIREBREATHING = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HOVERING = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FLYING = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> HEAD_ARMOR = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> NECK_ARMOR = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> BODY_ARMOR = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TAIL_ARMOR = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> MODEL_DEAD = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DEATH_STAGE = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> CONTROL_STATE = EntityDataManager.createKey(EntityDragonBase.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> TACKLE = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> AGINGDISABLED = EntityDataManager.<Boolean>createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.<Integer>createKey(EntityDragonBase.class, DataSerializers.VARINT);
    public static Animation ANIMATION_EAT;
    public static Animation ANIMATION_SPEAK;
    public static Animation ANIMATION_BITE;
    public static Animation ANIMATION_SHAKEPREY;
    public static Animation ANIMATION_WINGBLAST;
    public static Animation ANIMATION_ROAR;
    public static Animation ANIMATION_TAILWHACK;
    public EnumDragonType dragonType;
    public double minimumDamage;
    public double maximumDamage;
    public double minimumHealth;
    public double maximumHealth;
    public double minimumSpeed;
    public double maximumSpeed;
    public double minimumArmor;
    public double maximumArmor;
    public float sitProgress;
    public float sleepProgress;
    public float hoverProgress;
    public float flyProgress;
    public float fireBreathProgress;
    public float diveProgress;
    public float prevFireBreathProgress;
    public int fireStopTicks;
    public int flyTicks;
    public float modelDeadProgress;
    public float ridingProgress;
    public float tackleProgress;
    public ContainerHorseChest dragonInv;
    public boolean attackDecision;
    public int flightCycle;
    public BlockPos airTarget;
    public BlockPos homePos;
    public boolean hasHomePosition = false;
    @SideOnly(Side.CLIENT)
    public IFChainBuffer roll_buffer;
    @SideOnly(Side.CLIENT)
    public ReversedBuffer turn_buffer;
    @SideOnly(Side.CLIENT)
    public ChainBuffer tail_buffer;
    public int spacebarTicks;
    public float[][] growth_stages;
    public LegSolverQuadruped legSolver;
    private boolean isSleeping;
    private boolean isSitting;
    private boolean isHovering;
    private boolean isFlying;
    private boolean isBreathingFire;
    private boolean isTackling;
    private int fireTicks;
    private int hoverTicks;
    private boolean isModelDead;
    private int animationTick;
    private Animation currentAnimation;
    public int walkCycle;
    private int tacklingTicks;
    private int ticksStill;
    private float lastScale;
    public EntityDragonPart headPart;
    public EntityDragonPart neckPart;
    public EntityDragonPart rightWingUpperPart;
    public EntityDragonPart rightWingLowerPart;
    public EntityDragonPart leftWingUpperPart;
    public EntityDragonPart leftWingLowerPart;
    public EntityDragonPart tail1Part;
    public EntityDragonPart tail2Part;
    public EntityDragonPart tail3Part;
    public EntityDragonPart tail4Part;

    public EntityDragonBase(World world) {
        super(world);
    }
    public EntityDragonBase(World world, EnumDragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed) {
        super(world);
        this.dragonType = type;
        this.minimumDamage = minimumDamage;
        this.maximumDamage = maximumDamage;
        this.minimumHealth = minimumHealth;
        this.maximumHealth = maximumHealth;
        this.minimumSpeed = minimumSpeed;
        this.maximumSpeed = maximumSpeed;
        this.minimumArmor = 1D;
        this.maximumArmor = 20D;
        ANIMATION_EAT = Animation.create(20);
        updateAttributes();
        initDragonInv();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            roll_buffer = new IFChainBuffer();
            turn_buffer = new ReversedBuffer();
            tail_buffer = new ChainBuffer();
        }
        legSolver = new LegSolverQuadruped(0.2F, 1.2F, 1.0F);
        this.ignoreFrustumCheck = true;
        resetParts(1);
    }

    public void resetParts(float scale) {
        removeParts();
        headPart = new EntityDragonPart(this, 1.55F * scale, 0, 0.6F * scale, 0.5F * scale, 0.35F * scale, 1.5F);
        neckPart = new EntityDragonPart(this, 0.85F * scale, 0, 0.7F * scale, 0.5F * scale, 0.2F * scale, 1);
        rightWingUpperPart = new EntityDragonPart(this, 1F * scale, 90, 0.5F * scale, 0.85F * scale, 0.3F * scale, 0.5F);
        rightWingLowerPart = new EntityDragonPart(this, 1.4F * scale, 100, 0.3F * scale, 0.85F * scale, 0.2F * scale, 0.5F);
        leftWingUpperPart = new EntityDragonPart(this, 1F * scale, -90, 0.5F * scale, 0.85F * scale, 0.3F * scale, 0.5F);
        leftWingLowerPart = new EntityDragonPart(this, 1.4F * scale, -100, 0.3F * scale, 0.85F * scale, 0.2F * scale, 0.5F);
        tail1Part = new EntityDragonPart(this, -0.75F * scale, 0, 0.6F * scale, 0.35F * scale, 0.35F * scale, 1);
        tail2Part = new EntityDragonPart(this, -1.15F * scale, 0, 0.45F * scale, 0.35F * scale, 0.35F * scale, 1);
        tail3Part = new EntityDragonPart(this, -1.5F * scale, 0, 0.35F * scale, 0.35F * scale, 0.35F * scale, 1);
        tail4Part = new EntityDragonPart(this, -1.95F * scale, 0, 0.25F * scale, 0.45F * scale, 0.3F * scale, 1.5F);
    }

    public void removeParts() {
        if (headPart != null) {
            world.removeEntityDangerously(headPart);
            headPart = null;
        }
        if (neckPart != null) {
            world.removeEntityDangerously(neckPart);
            neckPart = null;
        }
        if (rightWingUpperPart != null) {
            world.removeEntityDangerously(rightWingUpperPart);
            rightWingUpperPart = null;
        }
        if (rightWingLowerPart != null) {
            world.removeEntityDangerously(rightWingLowerPart);
            rightWingLowerPart = null;
        }
        if (leftWingUpperPart != null) {
            world.removeEntityDangerously(leftWingUpperPart);
            leftWingUpperPart = null;
        }
        if (leftWingLowerPart != null) {
            world.removeEntityDangerously(leftWingLowerPart);
            leftWingLowerPart = null;
        }
        if (tail1Part != null) {
            world.removeEntityDangerously(tail1Part);
            tail1Part = null;
        }
        if (tail2Part != null) {
            world.removeEntityDangerously(tail2Part);
            tail2Part = null;
        }
        if (tail3Part != null) {
            world.removeEntityDangerously(tail3Part);
            tail3Part = null;
        }
        if (tail4Part != null) {
            world.removeEntityDangerously(tail4Part);
            tail4Part = null;
        }
    }

    public void updateParts() {
        if (headPart != null) {
            headPart.onUpdate();
        }
        if (neckPart != null) {
            neckPart.onUpdate();
        }
        if (rightWingUpperPart != null) {
            rightWingUpperPart.onUpdate();
        }
        if (rightWingLowerPart != null) {
            rightWingLowerPart.onUpdate();
        }
        if (leftWingUpperPart != null) {
            leftWingUpperPart.onUpdate();
        }
        if (leftWingLowerPart != null) {
            leftWingLowerPart.onUpdate();
        }
        if (tail1Part != null) {
            tail1Part.onUpdate();
        }
        if (tail2Part != null) {
            tail2Part.onUpdate();
        }
        if (tail3Part != null) {
            tail3Part.onUpdate();
        }
        if (tail4Part != null) {
            tail4Part.onUpdate();
        }
    }

    protected PathNavigate createNavigator(World worldIn) {
        return IceAndFireConfig.DRAGON_SETTINGS.experimentalPathFinder ? new PathNavigateExperimentalGround(this, worldIn) : super.createNavigator(worldIn);
    }

    public boolean canDestroyBlock(BlockPos pos) {
        float hardness = world.getBlockState(pos).getBlock().getBlockHardness(world.getBlockState(pos), world, pos);
        return world.getBlockState(pos).getBlock().canEntityDestroy(world.getBlockState(pos), world, pos, this) && hardness >= 0;
    }

    @Override
    public boolean isMobDead() {
        return this.isModelDead();
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return 10 * this.getDragonStage() / 5;
    }

    private void initDragonInv() {
        ContainerHorseChest animalchest = this.dragonInv;
        this.dragonInv = new ContainerHorseChest("dragonInventory", 4);
        this.dragonInv.setCustomName(this.getName());
        if (animalchest != null) {
            int i = Math.min(animalchest.getSizeInventory(), this.dragonInv.getSizeInventory());

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = animalchest.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    this.dragonInv.setInventorySlotContents(j, itemstack.copy());
                }
            }
        }
        if (world.isRemote) {
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.HEAD, this.getArmorOrdinal(this.dragonInv.getStackInSlot(0))));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.CHEST, this.getArmorOrdinal(this.dragonInv.getStackInSlot(1))));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.LEGS, this.getArmorOrdinal(this.dragonInv.getStackInSlot(2))));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.FEET, this.getArmorOrdinal(this.dragonInv.getStackInSlot(3))));
        }
    }

    public void updateDragonSlots() {
        int armorHead = getArmorOrdinal(this.dragonInv.getStackInSlot(0));
        int armorNeck = getArmorOrdinal(this.dragonInv.getStackInSlot(1));
        int armorBody = getArmorOrdinal(this.dragonInv.getStackInSlot(2));
        int armorTail = getArmorOrdinal(this.dragonInv.getStackInSlot(3));
        this.setArmorInSlot(EntityEquipmentSlot.HEAD, armorHead);
        this.setArmorInSlot(EntityEquipmentSlot.CHEST, armorNeck);
        this.setArmorInSlot(EntityEquipmentSlot.LEGS, armorBody);
        this.setArmorInSlot(EntityEquipmentSlot.FEET, armorTail);
        if (world.isRemote) {
            EntityDragonBase.this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.HEAD, armorHead));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.CHEST, armorNeck));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.LEGS, armorBody));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.FEET, armorTail));
        }
        double armorStep = (maximumArmor - minimumArmor) / (125);
        double baseArmor = minimumArmor + (armorStep * Math.min(this.getAgeInDays(), 125));
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(baseArmor + calculateArmorModifier());
    }

    public void openGUI(EntityPlayer playerEntity) {
        if (!this.world.isRemote && (!this.isBeingRidden() || this.isPassenger(playerEntity))) {
            playerEntity.openGui(IceAndFire.INSTANCE, 0, this.world, this.getEntityId(), 0, 0);
        }
    }

    public int getTalkInterval() {
        return 90;
    }

    protected void onDeathUpdate() {
        this.deathTime = 0;
        if (!this.isModelDead()) {
            if (!this.world.isRemote && this.recentlyHit > 0) {
                int i = this.getExperiencePoints(this.attackingPlayer);
                i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.attackingPlayer, i);
                while (i > 0) {
                    int j = EntityXPOrb.getXPSplit(i);
                    i -= j;
                    this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
                }
            }
        }
        this.setModelDead(true);

        if (this.getDeathStage() >= this.getAgeInDays() / 5) {
            this.setDead();
            for (int k = 0; k < 40; ++k) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                if (world.isRemote) {
                    ParticleHelper.spawnParticle(this.world, EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1);
                }
            }
            for (int k = 0; k < 3; ++k) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                if (dragonType == EnumDragonType.FIRE && world.isRemote) {
                    ParticleHelper.spawnParticle(this.world, EnumParticleTypes.FLAME, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1);
                } else if (dragonType == EnumDragonType.ICE && world.isRemote)  {
                    IceAndFire.PROXY.spawnParticle(EnumParticle.SNOWFLAKE, this.world, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1);
                } else if (dragonType == EnumDragonType.LIGHTNING && world.isRemote)  {
                    IceAndFire.PROXY.spawnParticle(EnumParticle.SPARK, this.world, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1);
                }
            }
        }
    }

    public void setDead() {
        removeParts();
        super.setDead();
    }

    protected int getExperiencePoints(EntityPlayer player) {
        switch (this.getDragonStage()) {
            case 2:
                return 20;
            case 3:
                return 50;
            case 4:
                return 100;
            case 5:
                return 150;
            default:
                return 5;
        }
    }

    public int getArmorOrdinal(ItemStack stack) {
        Item item = !stack.isEmpty() ? stack.getItem() : null;
        if (item instanceof ItemDragonArmor) {
            return ((ItemDragonArmor) item).type.ordinal() + 1;
        }
        return 0;
    }

    @Override
    public boolean isAIDisabled() {
        IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
        return this.isModelDead() || capability == null || capability.isStoned() || super.isAIDisabled();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HUNGER, 0);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(GENDER, Boolean.FALSE);
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(SLEEPING, Boolean.FALSE);
        this.dataManager.register(FIREBREATHING, Boolean.FALSE);
        this.dataManager.register(HOVERING, Boolean.FALSE);
        this.dataManager.register(FLYING, Boolean.FALSE);
        this.dataManager.register(HEAD_ARMOR, 0);
        this.dataManager.register(NECK_ARMOR, 0);
        this.dataManager.register(BODY_ARMOR, 0);
        this.dataManager.register(TAIL_ARMOR, 0);
        this.dataManager.register(DEATH_STAGE, 0);
        this.dataManager.register(MODEL_DEAD, Boolean.FALSE);
        this.dataManager.register(CONTROL_STATE, (byte) 0);
        this.dataManager.register(TACKLE, Boolean.FALSE);
        this.dataManager.register(AGINGDISABLED, Boolean.FALSE);
        this.dataManager.register(COMMAND, 0);
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

    public boolean strike() {
        return (dataManager.get(CONTROL_STATE) >> 3 & 1) == 1;
    }

    public boolean dismount() {
        return (dataManager.get(CONTROL_STATE) >> 4 & 1) == 1;
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

    public void strike(boolean strike) {
        setStateField(3, strike);
    }

    public void dismount(boolean dismount) {
        setStateField(4, dismount);
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

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
        if (command == 1) {
            this.setSitting(true);
        } else {
            this.setSitting(false);
        }
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Hunger", this.getHunger());
        compound.setInteger("AgeTicks", this.getAgeInTicks());
        compound.setBoolean("Gender", this.isMale());
        compound.setInteger("Variant", this.getVariant());
        compound.setBoolean("Sleeping", this.isSleeping());
        compound.setBoolean("FireBreathing", this.isBreathingFire());
        compound.setBoolean("AttackDecision", attackDecision);
        compound.setBoolean("Hovering", this.isHovering());
        compound.setBoolean("Flying", this.isFlying());
        compound.setInteger("ArmorHead", this.getArmorInSlot(EntityEquipmentSlot.HEAD));
        compound.setInteger("ArmorNeck", this.getArmorInSlot(EntityEquipmentSlot.CHEST));
        compound.setInteger("ArmorBody", this.getArmorInSlot(EntityEquipmentSlot.LEGS));
        compound.setInteger("ArmorTail", this.getArmorInSlot(EntityEquipmentSlot.FEET));
        compound.setInteger("DeathStage", this.getDeathStage());
        compound.setBoolean("ModelDead", this.isModelDead());
        compound.setFloat("DeadProg", this.modelDeadProgress);
        compound.setBoolean("Tackle", this.isTackling());
        if (dragonInv != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.dragonInv.getSizeInventory(); ++i) {
                ItemStack itemstack = this.dragonInv.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.setByte("Slot", (byte) i);
                    itemstack.writeToNBT(nbttagcompound);
                    nbttaglist.appendTag(nbttagcompound);
                }
            }
            compound.setTag("Items", nbttaglist);
        }
        if (this.getCustomNameTag() != null && !this.getCustomNameTag().isEmpty()) {
            compound.setString("CustomName", this.getCustomNameTag());
        }
        compound.setBoolean("HasHomePosition", this.hasHomePosition);
        if (homePos != null && this.hasHomePosition) {
            compound.setInteger("HomeAreaX", homePos.getX());
            compound.setInteger("HomeAreaY", homePos.getY());
            compound.setInteger("HomeAreaZ", homePos.getZ());
        }
        compound.setBoolean("AgingDisabled", this.isAgingDisabled());
        compound.setInteger("Command", this.getCommand());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHunger(compound.getInteger("Hunger"));
        this.setAgeInTicks(compound.getInteger("AgeTicks"));
        this.setGender(compound.getBoolean("Gender"));
        this.setVariant(compound.getInteger("Variant"));
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.setBreathingFire(compound.getBoolean("FireBreathing"));
        this.attackDecision = compound.getBoolean("AttackDecision");
        this.setHovering(compound.getBoolean("Hovering"));
        this.setFlying(compound.getBoolean("Flying"));
        this.setArmorInSlot(EntityEquipmentSlot.HEAD, compound.getInteger("ArmorHead"));
        this.setArmorInSlot(EntityEquipmentSlot.CHEST, compound.getInteger("ArmorNeck"));
        this.setArmorInSlot(EntityEquipmentSlot.LEGS, compound.getInteger("ArmorBody"));
        this.setArmorInSlot(EntityEquipmentSlot.FEET, compound.getInteger("ArmorTail"));
        if (dragonInv != null) {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initDragonInv();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int dragonInvSlot = nbttagcompound.getByte("Slot") & 255;
                if (dragonInvSlot <= 4) {
                    this.dragonInv.setInventorySlotContents(dragonInvSlot, new ItemStack(nbttagcompound));
                }
            }
        } else {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initDragonInv();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int dragonInvSlot = nbttagcompound.getByte("Slot") & 255;
                this.initDragonInv();
                this.dragonInv.setInventorySlotContents(dragonInvSlot, new ItemStack(nbttagcompound));
                this.setArmorInSlot(DragonUtils.getEquipmentSlotFromDragonInvSlot(dragonInvSlot), this.getArmorOrdinal(new ItemStack(nbttagcompound)));

                if (world.isRemote) {
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.HEAD, this.getArmorOrdinal(new ItemStack(nbttagcompound))));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.CHEST, this.getArmorOrdinal(new ItemStack(nbttagcompound))));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.LEGS, this.getArmorOrdinal(new ItemStack(nbttagcompound))));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), EntityEquipmentSlot.FEET, this.getArmorOrdinal(new ItemStack(nbttagcompound))));
                }
            }
        }

        this.setDeathStage(compound.getInteger("DeathStage"));
        this.setModelDead(compound.getBoolean("ModelDead"));
        this.modelDeadProgress = compound.getFloat("DeadProg");
        if (!compound.getString("CustomName").isEmpty()) {
            this.setCustomNameTag(compound.getString("CustomName"));
        }
        this.hasHomePosition = compound.getBoolean("HasHomePosition");
        if (hasHomePosition && compound.getInteger("HomeAreaX") != 0 && compound.getInteger("HomeAreaY") != 0 && compound.getInteger("HomeAreaZ") != 0) {
            homePos = new BlockPos(compound.getInteger("HomeAreaX"), compound.getInteger("HomeAreaY"), compound.getInteger("HomeAreaZ"));
        }
        this.setTackling(compound.getBoolean("Tackle"));
        this.setAgingDisabled(compound.getBoolean("AgingDisabled"));
        this.setCommand(compound.getInteger("Command"));
    }

    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer && this.getAttackTarget() != passenger) {
                EntityPlayer player = (EntityPlayer) passenger;
                if (this.isTamed() && this.getOwnerId() != null && this.getOwnerId().equals(player.getUniqueID())) {
                    return player;
                }
            } else if (passenger instanceof EntityLiving && DragonUtils.isDragonRider(passenger)) {
                return passenger;
            }
        }
        return null;
    }

    public boolean isPlayerControlled() {
        return getControllingPassenger() instanceof EntityPlayer;
    }

    public boolean isControllingPassenger(Entity entity) {
        return this.getControllingPassenger() != null && this.getControllingPassenger().getUniqueID().equals(entity.getUniqueID());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.min(2048, IceAndFireConfig.DRAGON_SETTINGS.dragonTargetSearchLength));
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);

    }

    protected void updateAttributes() {
        int armorHead = getArmorInSlot(EntityEquipmentSlot.HEAD);
        int armorNeck = getArmorInSlot(EntityEquipmentSlot.CHEST);
        int armorLegs = getArmorInSlot(EntityEquipmentSlot.LEGS);
        int armorFeet = getArmorInSlot(EntityEquipmentSlot.FEET);
        String armorResLoc = dragonType.getName() + "|" + armorHead + "|" + armorNeck + "|" + armorLegs + "|" + armorFeet;
        IceAndFire.PROXY.updateDragonArmorRender(armorResLoc);
        double healthStep = (maximumHealth - minimumHealth) / (125);
        double attackStep = (maximumDamage - minimumDamage) / (125);
        double speedStep = (maximumSpeed - minimumSpeed) / (125);
        double armorStep = (maximumArmor - minimumArmor) / (125);
        int age = Math.min(this.getAgeInDays(), 125);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.round(minimumHealth + (healthStep * age)));
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Math.round(minimumDamage + (attackStep * age)));
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(minimumSpeed + (speedStep * age));
        double baseArmor = minimumArmor + (armorStep * age);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(baseArmor + calculateArmorModifier());
    }

    public int getHunger() {
        return this.dataManager.get(HUNGER);
    }

    public void setHunger(int hunger) {
        this.dataManager.set(HUNGER, Math.min(100, hunger));
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public int getAgeInDays() {
        return this.dataManager.get(AGE_TICKS) / 24000;
    }

    public void setAgeInDays(int age) {
        this.dataManager.set(AGE_TICKS, age * 24000);
    }

    public int getAgeInTicks() {
        return this.dataManager.get(AGE_TICKS);
    }

    public void setAgeInTicks(int age) {
        this.dataManager.set(AGE_TICKS, age);
    }

    public int getDeathStage() {
        return this.dataManager.get(DEATH_STAGE);
    }

    public void setDeathStage(int stage) {
        this.dataManager.set(DEATH_STAGE, stage);
    }

    public boolean isMale() {
        return this.dataManager.get(GENDER);
    }

    public boolean isSkeletal() {
        return this.getDeathStage() >= (this.getAgeInDays() / 5) / 2;
    }

    public boolean isModelDead() {
        if (world.isRemote) {
            return this.isModelDead = this.dataManager.get(MODEL_DEAD);
        }
        return isModelDead;
    }

    public void setModelDead(boolean modeldead) {
        this.dataManager.set(MODEL_DEAD, modeldead);
        if (!world.isRemote) {
            this.isModelDead = modeldead;
        }
    }

    public boolean isHovering() {
        if (world.isRemote) {
            return this.isHovering = this.dataManager.get(HOVERING);
        }
        return isHovering;
    }

    public void setHovering(boolean hovering) {
        this.dataManager.set(HOVERING, hovering);
        if (!world.isRemote) {
            this.isHovering = hovering;
        }
    }

    public boolean isFlying() {
        if (world.isRemote) {
            return this.isFlying = this.dataManager.get(FLYING);
        }
        return isFlying;
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
        if (!world.isRemote) {
            this.isFlying = flying;
        }
    }

    public void setGender(boolean male) {
        this.dataManager.set(GENDER, male);
    }

    public boolean isSleeping() {
        if (world.isRemote) {
            boolean isSleeping = this.dataManager.get(SLEEPING);
            this.isSleeping = isSleeping;
            return isSleeping;
        }
        return isSleeping;
    }

    public void setSleeping(boolean sleeping) {
        this.dataManager.set(SLEEPING, sleeping);
        if (!world.isRemote) {
            this.isSleeping = sleeping;
        }
    }

    public boolean isBlinking() {
        return this.ticksExisted % 50 > 43;
    }

    public boolean isBreathingFire() {
        if (world.isRemote) {
            boolean breathing = this.dataManager.get(FIREBREATHING);
            this.isBreathingFire = breathing;
            return breathing;
        }
        return isBreathingFire;
    }

    public void setBreathingFire(boolean breathing) {
        this.dataManager.set(FIREBREATHING, breathing);
        if (!world.isRemote) {
            this.isBreathingFire = breathing;
        }
    }

    public void setTackling(boolean tackling) {
        this.dataManager.set(TACKLE, tackling);
        if (!world.isRemote) {
            this.isTackling = tackling;
        }
    }

    public void setAgingDisabled(boolean isAgingDisabled) {
        this.dataManager.set(AGINGDISABLED, isAgingDisabled);
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }

    @Override
    public boolean isSitting() {
        if (world.isRemote) {
            boolean isSitting = (this.dataManager.get(TAMED) & 1) != 0;
            this.isSitting = isSitting;
            return isSitting;
        }
        return isSitting;
    }

    @Override
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

    public int getArmorInSlot(EntityEquipmentSlot slot) {
        switch (slot) {
            default:
                return this.dataManager.get(HEAD_ARMOR);
            case CHEST:
                return this.dataManager.get(NECK_ARMOR);
            case LEGS:
                return this.dataManager.get(BODY_ARMOR);
            case FEET:
                return this.dataManager.get(TAIL_ARMOR);
        }
    }

    public void riderShootFire(Entity controller) {
    }

    @Override
    public void onKillEntity(EntityLivingBase entity) {
        super.onKillEntity(entity);
        this.setHunger(this.getHunger() + FoodUtils.getFoodPoints(entity));
    }

    public void setArmorInSlot(EntityEquipmentSlot slot, int armorType) {
        switch (slot) {
            default:
                this.dataManager.set(HEAD_ARMOR, armorType);
                break;
            case CHEST:
                this.dataManager.set(NECK_ARMOR, armorType);
                break;
            case LEGS:
                this.dataManager.set(BODY_ARMOR, armorType);
                break;
            case FEET:
                this.dataManager.set(TAIL_ARMOR, armorType);
                break;
        }
        if (world.isRemote) {
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonArmor(this.getEntityId(), slot, armorType));
        }
        double armorStep = (maximumArmor - minimumArmor) / (125);
        double baseArmor = minimumArmor + (armorStep * Math.min(this.getAgeInDays(), 125));
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(baseArmor + calculateArmorModifier());
    }

    private double calculateArmorModifier() {
        double val = 1D;
        for (EntityEquipmentSlot slot : EntityDragonBase.ARMOR_SLOTS) {
            switch (getArmorInSlot(slot)) {
                case 1:
                    val += 2D;
                    break;
                case 2:
                    val += 3D;
                    break;
                case 3:
                    val += 5D;
                    break;
                case 4:
                    val += 3D;
                    break;
                case 5:
                    val += 1D;
                    break;
            }
        }
        return val;
    }

    public boolean canMove() {
        IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
        if (capability != null && capability.isStoned()) {
            return false;
        }
        return !this.isSitting() && !this.isSleeping() && !this.isPlayerControlled() && !this.isModelDead() && sleepProgress == 0 && this.getAnimation() != ANIMATION_SHAKEPREY;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        int lastDeathStage = this.getAgeInDays() / 5;
        if (this.isModelDead() && this.getDeathStage() < lastDeathStage && player.capabilities.allowEdit) {
            if (!world.isRemote && !stack.isEmpty() && stack.getItem() == Items.GLASS_BOTTLE && this.getDeathStage() < lastDeathStage / 2 && IceAndFireConfig.DRAGON_SETTINGS.dragonDropBlood) {
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                this.setDeathStage(this.getDeathStage() + 1);
                player.inventory.addItemStackToInventory(new ItemStack(getBlood(), 1));
                return true;
            } else if (!world.isRemote && stack.isEmpty() && IceAndFireConfig.DRAGON_SETTINGS.dragonDropSkull) {
                if (this.getDeathStage() == lastDeathStage - 1) {
                    ItemStack skull = getSkull();
                    skull.setTagCompound(new NBTTagCompound());
                    skull.getTagCompound().setInteger("Stage", this.getDragonStage());
                    skull.getTagCompound().setInteger("DragonType", 0);
                    skull.getTagCompound().setInteger("DragonAge", this.getAgeInDays());
                    this.setDeathStage(this.getDeathStage() + 1);
                    if (!world.isRemote) {
                        this.entityDropItem(skull, 1);
                    }
                    this.setDead();
                } else if (this.getDeathStage() == (int) (lastDeathStage / 2) - 1 && IceAndFireConfig.DRAGON_SETTINGS.dragonDropHeart) {
                    ItemStack heart = new ItemStack(getHeart(), 1);
                    ItemStack egg = new ItemStack(this.getVariantEgg(this.rand.nextInt(4)), 1);
                    if (!world.isRemote) {
                        this.entityDropItem(heart, 1);
                        if (!this.isMale() && this.getDragonStage() > 3) {
                            this.entityDropItem(egg, 1);
                        }
                    }
                    this.setDeathStage(this.getDeathStage() + 1);
                } else {
                    this.setDeathStage(this.getDeathStage() + 1);
                    ItemStack drop = getRandomDrop();
                    if (!drop.isEmpty() && !world.isRemote) {
                        this.entityDropItem(drop, 1);
                    }
                }
            }
            return true;
        } else if (!this.isModelDead()) {
            if (this.isOwner(player)) {
                if (!stack.isEmpty()) {
                    if (this.isBreedingItem(stack) && this.isAdult()) {
                        this.setGrowingAge(0);
                        this.consumeItemFromStack(player, stack);
                        this.setInLove(player);
                        return true;
                    }
                    int itemFoodAmount = FoodUtils.getFoodPoints(stack, true, dragonType.isPiscivore());
                    if (itemFoodAmount > 0 && (this.getHunger() < 100 || this.getHealth() < this.getMaxHealth())) {
                        this.setHunger(this.getHunger() + itemFoodAmount);
                        this.setHealth(Math.min(this.getMaxHealth(), (int) (this.getHealth() + (itemFoodAmount / 10))));
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(stack.getItem());
                        this.eatFoodBonus(stack);
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        return true;
                    }
                    if (stack.getItem() == ModItems.dragon_meal) {
                        this.growDragon(1);
                        this.setHunger(this.getHunger() + 20);
                        this.heal(Math.min(this.getHealth(), (int) (this.getMaxHealth() / 2)));
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(stack.getItem());
                        this.spawnItemCrackParticles(Items.BONE);
                        this.spawnItemCrackParticles(Items.DYE);
                        this.eatFoodBonus(stack);
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        return true;
                    }
                    if (stack.getItem() == ModItems.sickly_dragon_meal && !this.isAgingDisabled()) {
                        this.setHunger(this.getHunger() + 20);
                        this.heal(this.getMaxHealth());
                        this.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(stack.getItem());
                        this.spawnItemCrackParticles(Items.BONE);
                        this.spawnItemCrackParticles(Items.DYE);
                        this.spawnItemCrackParticles(Items.POISONOUS_POTATO);
                        this.spawnItemCrackParticles(Items.POISONOUS_POTATO);
                        this.setAgingDisabled(true);
                        this.eatFoodBonus(stack);
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        return true;
                    }
                    if (stack.getItem() == ModItems.dragon_stick) {
                        if (player.isSneaking()) {
                            this.homePos = new BlockPos(this);
                            this.hasHomePosition = true;
                            player.sendStatusMessage(new TextComponentTranslation("dragon.command.new_home", homePos.getX(), homePos.getY(), homePos.getZ()), true);
                        } else {
                            this.playSound(SoundEvents.ENTITY_ZOMBIE_INFECT, this.getSoundVolume(), this.getSoundPitch());
                            this.setCommand(this.getCommand() + 1);
                            if (this.getCommand() > 1) {
                                this.setCommand(0);
                            }
                            player.sendStatusMessage(new TextComponentTranslation("dragon.command." + (this.getCommand() == 1 ? "sit" : "stand")), true);
                        }
                        return true;

                    }
                    IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
                    if (stack.getItem() == ModItems.dragon_horn && !world.isRemote && hand == EnumHand.MAIN_HAND && (capability == null || !capability.isStoned())) {
                        this.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 3, 1.25F);

                        NBTTagCompound tag = new NBTTagCompound();
                        this.writeEntityToNBT(tag);

                        ItemStack horn = getHorn();
                        horn.setTagCompound(tag);
                        player.setHeldItem(hand, horn);
                        this.setDead();
                        return true;
                    }
                } else {
                    if (!player.isSneaking() && !this.isDead) {
                        if (this.getDragonStage() < 2) {
                            this.startRiding(player, true);
                        }
                        if (this.getDragonStage() > 2 && !player.isRiding()) {
                            player.startRiding(this, true);
                            if (world.isRemote) {
                                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageUpdateRidingState(this.getEntityId(), true));
                            }
                            this.setSleeping(false);
                        }

                        if (this.getDragonStage() < 2) {
                            this.startRiding(player, true);
                        }
                        return true;
                    } else if (stack.isEmpty() && player.isSneaking()) {
                        this.openGUI(player);
                        return true;
                    }
                }
            } else {
                if (stack.getItem() == ModItems.dragon_collar) {
                    this.setTamedBy(player);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    return true;
                }
            }
        }
        //Don't call EntityAnimal::processInteract() due to custom breeding handling
        if (stack.getItem() == Items.SPAWN_EGG) {
            if (!this.world.isRemote) {
                Class<? extends Entity> oclass = EntityList.getClass(ItemMonsterPlacer.getNamedIdFrom(stack));

                if (oclass != null && this.getClass() == oclass) {
                    EntityAgeable entityageable = this.createChild(this);

                    if (entityageable != null) {
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
                        this.world.spawnEntity(entityageable);

                        if (stack.hasDisplayName()) {
                            entityageable.setCustomNameTag(stack.getDisplayName());
                        }

                        if (!player.capabilities.isCreativeMode) {
                            stack.shrink(1);
                        }
                    }
                }
            }
            return true;
        }
        else return false;
    }

    protected abstract ItemStack getSkull();

    protected abstract ItemStack getHorn();

    public abstract Item getBlood();

    public abstract Item getHeart();

    public abstract Item getFlesh();

    protected abstract int getBaseEggTypeValue();

    private ItemStack getRandomDrop() {
        ItemStack stack = getItemFromLootTable();
        if (stack.getItem() == ModItems.dragonbone) {
            this.playSound(SoundEvents.ENTITY_SKELETON_AMBIENT, 1, 1);
        } else {
            this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
        }
        return stack;
    }

    public abstract ResourceLocation getDeadLootTable();

    public ItemStack getItemFromLootTable() {
        LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(getDeadLootTable());
        LootContext.Builder lootcontext$builder = (new LootContext.Builder((WorldServer) this.world)).withLootedEntity(this).withDamageSource(DamageSource.GENERIC);
        if (this.attackingPlayer != null) {
            lootcontext$builder = lootcontext$builder.withPlayer(this.attackingPlayer).withLuck(this.attackingPlayer.getLuck());
        }
        List<ItemStack> loot = loottable.generateLootForPools(this.rand, lootcontext$builder.build());
        if (loot.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            return loot.get(0);
        }
    }

    public void eatFoodBonus(ItemStack stack) {

    }

    @Override
    protected boolean canDespawn() {
        return IceAndFireConfig.DRAGON_SETTINGS.canDragonsDespawn;
    }

    public void growDragon(int ageInDays) {
        if (this.isAgingDisabled()) {
            return;
        }
        this.setAgeInDays(this.getAgeInDays() + ageInDays);
        this.setScaleForAge(false);
        this.resetPositionToBB();
        if (this.getAgeInDays() % 25 == 0) {
            for (int i = 0; i < this.getRenderSize() * 4; i++) {
                double motionX = getRNG().nextGaussian() * 0.07D;
                double motionY = getRNG().nextGaussian() * 0.07D;
                double motionZ = getRNG().nextGaussian() * 0.07D;
                float f = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX) + this.getEntityBoundingBox().minX);
                float f1 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) + this.getEntityBoundingBox().minY);
                float f2 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ) + this.getEntityBoundingBox().minZ);
                if (world.isRemote) {
                    ParticleHelper.spawnParticle(this.world, EnumParticleTypes.VILLAGER_HAPPY, f, f1, f2, motionX, motionY, motionZ);
                }
            }
        }
        this.updateAttributes();
    }

    public void spawnItemCrackParticles(Item item) {
        for (int i = 0; i < 15; i++) {
            double motionX = getRNG().nextGaussian() * 0.07D;
            double motionY = getRNG().nextGaussian() * 0.07D;
            double motionZ = getRNG().nextGaussian() * 0.07D;
            Vec3d headPos = getHeadPosition();
            if (world.isRemote) {
                ParticleHelper.spawnParticle(this.world, EnumParticleTypes.ITEM_CRACK, headPos.x, headPos.y, headPos.z, motionX, motionY, motionZ, Item.getIdFromItem(item), 0);
            }
        }
    }

    protected boolean isTimeToWake() {
        return this.world.isDaytime();
    }

    private boolean isStuck() {
        return !this.isTamed() && (!this.getNavigator().noPath() && (this.getNavigator().getPath() == null || this.getNavigator().getPath().getFinalPathPoint() != null && this.getDistanceSq(new BlockPos(this.getNavigator().getPath().getFinalPathPoint().x, this.getNavigator().getPath().getFinalPathPoint().y, this.getNavigator().getPath().getFinalPathPoint().z)) > 15) || this.airTarget != null) && ticksStill > 80 && !this.isHovering() && canMove();
    }

    @Override
    public void setRevengeTarget(@Nullable EntityLivingBase livingBase) {
        if (livingBase != null && this.getClass().equals(livingBase.getClass())) {
            if (((EntityDragonBase) livingBase).isTamed() && !DragonUtils.hasSameOwner(livingBase, this)) {
                super.setRevengeTarget(livingBase);
            }
        } else {
            super.setRevengeTarget(livingBase);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.stepHeight = this.getDragonStage() * 0.5F;
        if (!this.isPlayerControlled() && this.isBeyondHeight() && !this.onGround) {
            this.motionY -= 0.1F;
        }
        if (!world.isRemote) {
            if (this.isSitting() && (this.getCommand() != 1 || this.getControllingPassenger() != null)) {
                this.setSitting(false);
            }
            if (!this.isSitting() && this.getCommand() == 1 && this.getControllingPassenger() == null) {
                this.setSitting(true);
            }
            if (this.isSitting()) {
                this.getNavigator().clearPath();
            }
            if (this.isInLove()) {
                this.world.setEntityState(this, (byte)18);
            }
            if ((int) this.prevPosX == (int) this.posX && (int) this.prevPosZ == (int) this.posZ) {
                this.ticksStill++;
            } else {
                ticksStill = 0;
            }
            if (this.getDragonStage() >= 3 && isStuck() && this.world.getGameRules().getBoolean("mobGriefing") && IceAndFireConfig.DRAGON_SETTINGS.dragonGriefing != 2) {
                if (this.getAnimation() == NO_ANIMATION && this.ticksExisted % 5 == 0) {
                    this.setAnimation(ANIMATION_TAILWHACK);
                }
                if (this.getAnimation() == ANIMATION_TAILWHACK && this.getAnimationTick() == 10) {
                    BlockBreakExplosion explosion = new BlockBreakExplosion(world, this, this.posX, this.posY, this.posZ, (4) * this.getDragonStage() - 2);
                    explosion.doExplosionA();
                    explosion.doExplosionB(true);
                    this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1, 1);
                }
            }
        }
        if (this.walkCycle < 39) {
            this.walkCycle++;
        } else {
            this.walkCycle = 0;
        }
        if (this.getAnimation() == ANIMATION_WINGBLAST && (this.getAnimationTick() == 17 || this.getAnimationTick() == 22 || this.getAnimationTick() == 28)) {
            this.spawnGroundEffects();
            if (!this.world.isRemote && this.getAttackTarget() != null) {
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), ((float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()) / 4);
                this.getAttackTarget().knockBack(this.getAttackTarget(), this.getDragonStage() * 0.6F, 1, 1);
                this.attackDecision = this.getRNG().nextBoolean();
            }
        }
        IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
        if (!this.world.isRemote) {
            if (this.isTackling() && !this.isFlying() && this.onGround) {
                tacklingTicks++;
                if (tacklingTicks == 40) {
                    tacklingTicks = 0;
                    this.setTackling(false);
                    this.setFlying(false);
                }
            }
            if (this.getRNG().nextInt(500) == 0 && !this.isModelDead() && !this.isSleeping()) {
                this.roar();
            }
            if (this.onGround && this.getNavigator().noPath() && this.getAttackTarget() != null && this.getAttackTarget().posY - 3 > this.posY && this.getRNG().nextInt(15) == 0 && this.canMove() && !this.isHovering() && !this.isFlying() && !this.isChild()) {
                this.setHovering(true);
                this.setSleeping(false);
                this.setSitting(false);
                this.hoverTicks = 0;
                this.flyTicks = 0;
            }
            if (this.isFlying() && this.getAttackTarget() != null && this.attackDecision && this.isDirectPathBetweenPoints(this.getPositionVector(), this.getAttackTarget().getPositionVector())) {
                this.setTackling(true);
            }
            if (this.isFlying() && this.getAttackTarget() != null && this.isTackling() && this.getEntityBoundingBox().expand(2.0D, 2.0D, 2.0D).intersects(this.getAttackTarget().getEntityBoundingBox())) {
                this.attackDecision = true;
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), this.getDragonStage() * 3);
                this.spawnGroundEffects();
                this.setFlying(false);
                this.setHovering(false);
            }
            if (this.isTackling() && this.getAttackTarget() == null) {
                this.setTackling(false);
            }
            if (capability != null && capability.isStoned()) {
                this.setFlying(false);
                this.setHovering(false);
                return;
            }
            if (this.isFlying() && this.ticksExisted % 40 == 0 || this.isFlying() && this.isSleeping()) {
                this.setFlying(true);
                this.setSleeping(false);
            }
            if (this.isModelDead() && (this.isFlying() || this.isHovering())) {
                this.setFlying(false);
                this.setHovering(false);
            }
            if (!this.canMove()) {
                this.getNavigator().clearPath();
            }
            if (!this.canMove() && this.getAttackTarget() != null) {
                this.setAttackTarget(null);
            }
            if (!this.isTamed()) {
                this.updateCheckPlayer();
            }
        }
        this.motionY = Math.min(this.motionY, 0.5);
        this.motionY = Math.max(this.motionY, -0.5);
        AnimationHandler.INSTANCE.updateAnimations(this);
        this.legSolver.update(this);
        if ((this.isFlying() || this.isHovering()) && !this.isModelDead()) {
            if (flightCycle < 58) {
                flightCycle += 2;
            } else {
                flightCycle = 0;
            }
            if (flightCycle == 2) {
                this.playSound(ModSounds.DRAGON_FLIGHT, this.getSoundVolume() * IceAndFireConfig.DRAGON_SETTINGS.dragonFlapNoiseDistance, getSoundPitch());
            }
            if (this.isModelDead() && flightCycle != 0) {
                flightCycle = 0;
            }
        }

        boolean sitting = isSitting() && !isModelDead() && !isSleeping() && !isHovering() && !isFlying();
        if (sitting && sitProgress < 20.0F) {
            sitProgress += 0.5F;
        } else if (!sitting && sitProgress > 0.0F) {
            sitProgress -= 0.5F;
        }
        boolean sleeping = isSleeping() && !isHovering() && !isFlying();
        if (sleeping && sleepProgress < 20.0F) {
            sleepProgress += 0.5F;
        } else if (!sleeping && sleepProgress > 0.0F) {
            sleepProgress -= 0.5F;
        }
        boolean fireBreathing = isBreathingFire();
        prevFireBreathProgress = fireBreathProgress;
        if (fireBreathing && fireBreathProgress < 5.0F) {
            fireBreathProgress += 0.5F;
        } else if (!fireBreathing && fireBreathProgress > 0.0F) {
            fireBreathProgress -= 0.5F;
        }
        boolean hovering = isHovering();
        if (hovering && hoverProgress < 20.0F) {
            hoverProgress += 0.5F;
        } else if (!hovering && hoverProgress > 0.0F) {
            hoverProgress -= 0.5F;
        }
        boolean tackling = isTackling();
        if (tackling && tackleProgress < 5F) {
            tackleProgress += 0.5F;
        } else if (!tackling && tackleProgress > 0.0F) {
            tackleProgress -= 1.5F;
        }
        boolean flying = !tackling && this.isFlying() || !this.onGround && !this.isHovering() && this.airTarget != null;
        if (flying && flyProgress < 20.0F) {
            flyProgress += 0.5F;
        } else if (!flying && flyProgress > 0.0F) {
            flyProgress -= 0.5F;
        }
        boolean isModelDead = isModelDead();
        if (isModelDead && modelDeadProgress < 20.0F) {
            modelDeadProgress += 0.5F;
        } else if (!isModelDead && modelDeadProgress > 0.0F) {
            modelDeadProgress -= 0.5F;
        }
        boolean riding = isRiding() && this.getRidingEntity() != null && this.getRidingEntity() instanceof EntityPlayer;
        if (riding && ridingProgress < 20.0F) {
            ridingProgress += 0.5F;
        } else if (!riding && ridingProgress > 0.0F) {
            ridingProgress -= 0.5F;
        }

        if (this.isModelDead()) {
            return;
        }
        if (!this.world.isRemote) {
            if (this.onGround && this.doesWantToLand() && (this.isFlying() || this.isHovering())) {
                this.airTarget = null;

                this.setFlying(false);
                this.setHovering(false);
            }
            if (this.isHovering()) {
                if (this.isSleeping()) {
                    this.setHovering(false);
                }
                this.hoverTicks++;
                if (this.doesWantToLand() && !this.onGround) {
                    this.motionY -= 0.25D;
                } else {
                    if (!this.isPlayerControlled() && !this.isBeyondHeight()) {
                        this.motionY += 0.08;
                    }
                    if (this.hoverTicks > 40) {
                        if (!this.isChild()) {
                            this.setFlying(true);
                        }
                        this.setHovering(false);
                        this.hoverTicks = 0;
                        this.flyTicks = 0;
                    }
                }
            }
            if (this.isSleeping()) {
                this.getNavigator().clearPath();
            }
            if (!isPlayerControlled()) {
                if (!this.isFlying() && !this.isHovering() && this.airTarget != null && this.onGround) {
                    this.airTarget = null;
                }
                if (this.isFlying() && this.airTarget == null && this.onGround) {
                    this.setFlying(false);
                    this.setHovering(false);
                }
                if (this.isFlying() && getAttackTarget() == null) {
                    flyAround();
                } else if (airTarget != null) {
                    flyTowardsTarget();
                }
            } else {
                this.airTarget = null;
            }

            if (this.onGround && flyTicks != 0) {
                flyTicks = 0;
            }
            if (this.isFlying() && this.doesWantToLand()) {
                this.setFlying(false);
                this.setHovering(!this.onGround);
                if (this.onGround) {
                    flyTicks = 0;
                }
            }
            if (this.isFlying()) {
                this.flyTicks++;
            }
            if ((this.isHovering() || this.isFlying()) && this.isSleeping()) {
                this.setFlying(false);
                this.setHovering(false);
            }
            if ((capability == null || !capability.isStoned()) && (this.getRNG().nextInt(FLIGHT_CHANCE_PER_TICK) == 0 && !this.isFlying() && !this.isChild() && !this.isHovering() && this.canMove() && this.onGround || this.posY < -1)) {
                this.setHovering(true);
                this.setSleeping(false);
                this.setSitting(false);
                this.hoverTicks = 0;
                this.flyTicks = 0;
            }
            if (this.getAttackTarget() != null && this.getAttackTarget().posY + 5 < this.posY && !this.isFlying() && !this.isChild() && !this.isHovering() && this.canMove() && this.onGround) {
                this.setHovering(true);
                this.setSleeping(false);
                this.setSitting(false);
                this.hoverTicks = 0;
                this.flyTicks = 0;
            }
            if (this.isInWater()
                    && this.getAttackTarget() != null
                    && this.getRNG().nextInt(15) == 0
                    && (capability == null || !capability.isStoned())
                    && this.canMove()
                    && !this.isHovering()
                    && !this.isFlying()
                    && !this.isChild()) {
                this.setHovering(true);
                this.setSleeping(false);
                this.setSitting(false);
                this.hoverTicks = 0;
                this.flyTicks = 0;
            }
            if (getAttackTarget() != null && this.isPlayerControlled()) {
                this.setAttackTarget(null);
            }
        }
        if (this.isPlayerControlled() && !this.onGround && (this.isFlying() || this.isHovering())) {
            this.motionY *= 0D;
        }
        if (!this.isAgingDisabled()) {
            this.setAgeInTicks(this.getAgeInTicks() + 1);
            if (this.getAgeInTicks() % 24000 == 0) {
                this.growDragon(0);
            }
        }
        if (this.getAgeInTicks() % IceAndFireConfig.DRAGON_SETTINGS.dragonHungerTickRate == 0) {
            if (this.getHunger() > 0) {
                this.setHunger(this.getHunger() - 1);
            }
        }
        if (!this.world.isRemote) {
            if (this.attackDecision && this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) > Math.min(this.getEntityBoundingBox().getAverageEdgeLength() * 5, 25) && !this.isChild()) {
                this.attackDecision = false;
            }
            if ((!this.attackDecision || this.getRNG().nextInt(750) == 0) && this.isChild()) {
                this.attackDecision = this.getRNG().nextBoolean();

                List<MessageParticleFX.Particle> particles = new ArrayList<>();
                Vec3d headPos = getHeadPosition();
                for (int i = 0; i < 5; i++) {
                    particles.add(MessageParticleFX.createParticle(headPos.x, headPos.y, headPos.z, 0, 0, 0));
                }
                EnumParticle particleType = EnumParticle.SMOKE_LARGE;
                if (this.dragonType == EnumDragonType.ICE) {
                    particleType = EnumParticle.DRAGON_ICE;
                } else if (this.dragonType == EnumDragonType.LIGHTNING) {
                    particleType = EnumParticle.SPARK;
                }
                IceAndFire.NETWORK_WRAPPER.sendToAllTracking(new MessageParticleFX(particleType, particles), this);

                if (this.dragonType == EnumDragonType.ICE) {
                    this.playSound(ModSounds.ICEDRAGON_BREATH_SHORT, 3, 1);
                } else if (this.dragonType == EnumDragonType.LIGHTNING) {
                    this.playSound(ModSounds.LIGHTNINGDRAGON_BREATH_SHORT, 3, 1);
                } else {
                    this.playSound(ModSounds.FIREDRAGON_BREATH_SHORT, 3, 1);
                }
            }
            if (this.isBreathingFire()) {
                this.fireTicks++;
                if (this.fireTicks > this.getDragonStage() * 25 || this.fireStopTicks <= 0 && this.isPlayerControlled()) {
                    this.setBreathingFire(false);
                    this.attackDecision = this.getRNG().nextBoolean();
                    this.fireTicks = 0;
                }
                if (this.fireStopTicks > 0 && this.isPlayerControlled()) {
                    this.fireStopTicks--;
                }
            }
            if (this.isFlying() && this.getAttackTarget() != null && this.getEntityBoundingBox().expand(3.0F, 3.0F, 3.0F).intersects(this.getAttackTarget().getEntityBoundingBox())) {
                this.attackEntityAsMob(this.getAttackTarget());
            }
            this.breakBlock();
        }
    }

    private boolean isBeyondHeight() {
        if (this.posY > this.world.getHeight()) {
            return true;
        }
       return this.posY > DragonUtils.getMaximumFlightHeightForPos(world, new BlockPos(this));
    }

    public void breakBlock() {
        if (IceAndFireConfig.DRAGON_SETTINGS.dragonGriefing != 2 && (!this.isTamed() || IceAndFireConfig.DRAGON_SETTINGS.tamedDragonGriefing)) {
            float hardness = IceAndFireConfig.DRAGON_SETTINGS.dragonGriefing == 1 || this.getDragonStage() <= 3 ? 1.6F : 5F;
            if (!isModelDead() && this.getDragonStage() >= 3 && (this.canMove() || this.isPlayerControlled())) {
                for (int a = (int) Math.round(this.getEntityBoundingBox().minX) - 1; a <= (int) Math.round(this.getEntityBoundingBox().maxX) + 1; a++) {
                    for (int b = (int) Math.round(this.getEntityBoundingBox().minY) + 1; (b <= (int) Math.round(this.getEntityBoundingBox().maxY) + 2) && (b <= 127); b++) {
                        for (int c = (int) Math.round(this.getEntityBoundingBox().minZ) - 1; c <= (int) Math.round(this.getEntityBoundingBox().maxZ) + 1; c++) {
                            IBlockState state = world.getBlockState(new BlockPos(a, b, c));
                            Block block = state.getBlock();
                            BlockPos pos = new BlockPos(a, b, c);
                            if (state.getMaterial() != Material.AIR && !(block instanceof BlockBush) && !(block instanceof BlockLiquid) && state.getBlockHardness(world, pos) < hardness && DragonUtils.canDragonBreak(world, state.getBlock(), pos) && this.canDestroyBlock(pos)) {
                                this.motionX *= 0.6D;
                                this.motionZ *= 0.6D;
                                DragonUtils.destroyBlock(world, new BlockPos(a, b, c), state);
                            }
                        }
                    }
                }
            }
        }
    }

    public void spawnGroundEffects() {
        for (int i = 0; i < this.getRenderSize(); i++) {
            for (int i1 = 0; i1 < 20; i1++) {
                double motionX = getRNG().nextGaussian() * 0.07D;
                double motionY = getRNG().nextGaussian() * 0.07D;
                double motionZ = getRNG().nextGaussian() * 0.07D;
                float radius = 0.75F * (0.7F * getRenderSize() / 3) * -3;
                float angle = (0.01745329251F * this.renderYawOffset) + i1 * 1F;
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraY = 0.8F;
                double extraZ = radius * MathHelper.cos(angle);
                BlockPos ground = getGround(new BlockPos(MathHelper.floor(this.posX + extraX), MathHelper.floor(this.posY + extraY) - 1, MathHelper.floor(this.posZ + extraZ)));
                IBlockState iblockstate = this.world.getBlockState(new BlockPos(ground));
                if (iblockstate.getMaterial() != Material.AIR) {
                    if (world.isRemote) {
                        ParticleHelper.spawnParticle(world, EnumParticleTypes.BLOCK_CRACK, true, this.posX + extraX, this.posY + extraY, this.posZ + extraZ, motionX, motionY, motionZ, Block.getStateId(iblockstate));
                    }
                }
            }
        }
    }

    private BlockPos getGround(BlockPos blockPos) {
        while (world.isAirBlock(blockPos) && blockPos.getY() > 1) {
            blockPos = blockPos.down();
        }
        return blockPos;
    }

    public void fall(float distance, float damageMultiplier) {

    }

    public boolean isActuallyBreathingFire() {
        return this.fireTicks > 20 && this.isBreathingFire();
    }

    public boolean doesWantToLand() {
        IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
        return this.flyTicks > 6000 || down() || flyTicks > 40 && this.flyProgress == 0 || capability != null && capability.isStoned();
    }

    public abstract String getVariantName(int variant);

    @Override
    public boolean shouldRiderSit() {
        return this.getControllingPassenger() != null;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if (this.isPassenger(passenger)) {
            if (this.getControllingPassenger() == null || !this.isControllingPassenger(passenger)) {
                updatePreyInMouth(passenger);
            } else {
                if (this.isModelDead()) {
                    passenger.dismountRidingEntity();
                }
                float speed_walk = 0.2F;
                float speed_idle = 0.05F;
                float speed_fly = 0.2F;
                float degree_walk = 0.5F;
                float degree_idle = 0.5F;
                float degree_fly = 0.5F;
                if (passenger instanceof EntityPlayer) {
                    this.renderYawOffset = this.rotationYaw;
                    this.rotationYaw = passenger.rotationYaw;
                }
                float hoverAddition = hoverProgress * -0.001F;
                float flyAddition = flyProgress * -0.0001F;
                float flyBody = Math.max(flyProgress, hoverProgress) * 0.0065F;
                float radius = 0.75F * ((0.3F - flyBody) * getRenderSize()) + ((this.getRenderSize() / 3) * flyAddition * 0.0065F);
                float angle = (0.01745329251F * this.renderYawOffset);
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraZ = radius * MathHelper.cos(angle);
                float bob0 = this.isFlying() || this.isHovering() ? (hoverProgress > 0 || flyProgress > 0 ? this.bob(-speed_fly, degree_fly * 5, false, this.ticksExisted, -0.0625F) : 0) : 0;
                float bob1 = this.bob(speed_walk * 2, degree_walk * 1.7F, false, this.limbSwing, this.limbSwingAmount * -0.0625F);
                float bob2 = this.bob(speed_idle, degree_idle * 1.3F, false, this.ticksExisted, -0.0625F);
                float extraAgeScale = (Math.max(0, this.getAgeInDays() - 75) / 75F) * 1.65F;

                double extraY_pre = 0.8F;
                double extraY = ((extraY_pre - (hoverAddition) + (flyAddition)) * (this.getRenderSize() / 3)) - (0.35D * (1 - (this.getRenderSize() / 30))) + bob0 + bob1 + bob2 + extraAgeScale;

                passenger.setPosition(this.posX + extraX, this.posY + extraY, this.posZ + extraZ);
            }
        }
    }

    private float bob(float speed, float degree, boolean bounce, float f, float f1) {
        float bob = (float) (Math.sin(f * speed) * f1 * degree - f1 * degree);
        if (bounce) {
            bob = (float) -Math.abs((Math.sin(f * speed) * f1 * degree));
        }
        return bob * this.getRenderSize() / 3;
    }

    private void updatePreyInMouth(Entity prey) {
        this.setAnimation(ANIMATION_SHAKEPREY);
        if (this.getAnimation() == ANIMATION_SHAKEPREY && this.getAnimationTick() > 55 && prey != null) {
            this.doBiteAttack(prey);
            prey.dismountRidingEntity();
        }
        renderYawOffset = rotationYaw;
        float modTick_0 = this.getAnimationTick() - 25;
        float modTick_1 = this.getAnimationTick() > 25 && this.getAnimationTick() < 55 ? 8 * MathHelper.clamp(MathHelper.sin((float) (Math.PI + modTick_0 * 0.25)), -0.8F, 0.8F) : 0;
        float modTick_2 = this.getAnimationTick() > 30 ? 10 : Math.max(0, this.getAnimationTick() - 20);
        float radius = 0.75F * (0.6F * getRenderSize() / 3) * -3;
        float angle = (0.01745329251F * this.renderYawOffset) + 3.15F + (modTick_1 * 2F) * 0.015F;
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        double extraY = modTick_2 == 0 ? 0 : 0.035F * ((getRenderSize() / 3) + (modTick_2 * 0.5 * (getRenderSize() / 3)));
        prey.setPosition(this.posX + extraX, this.posY + extraY, this.posZ + extraZ);
    }

    public int getDragonStage() {
        int age = this.getAgeInDays();
        if (age >= 100) {
            return 5;
        } else if (age >= 75) {
            return 4;
        } else if (age >= 50) {
            return 3;
        } else if (age >= 25) {
            return 2;
        } else {
            return 1;
        }
    }

    public boolean isTeen() {
        return getDragonStage() < 4 && getDragonStage() > 2;
    }

    public boolean isAdult() {
        return getDragonStage() >= 4;
    }

    public boolean isChild() {
        return getDragonStage() < 2;
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setGender(this.getRNG().nextBoolean());
        int age = this.getRNG().nextInt(80) + 1;
        this.growDragon(age);
        this.setVariant(new Random().nextInt(4));
        this.setSleeping(false);
        this.updateAttributes();
        double healthStep = (maximumHealth - minimumHealth) / (125);
        this.heal((Math.round(minimumHealth + (healthStep * age))));
        this.attackDecision = true;
        this.setHunger(50);
        return livingdata;
    }

    @Override
    public boolean getCanSpawnHere() {
        IBlockState state = this.world.getBlockState((new BlockPos(this)).down());
        return state.canEntitySpawn(this);
    }

    public boolean doBiteAttack(Entity entity) {
        float prevHealth = 0;
        if (entity instanceof EntityLivingBase) prevHealth = ((EntityLivingBase)entity).getHealth();
        boolean success = entity.attackEntityFrom(
                DamageSource.causeMobDamage(this),
                (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()
        );
        if (success && entity instanceof EntityLivingBase) {
            float changed = (float)IceAndFireConfig.DRAGON_SETTINGS.dragonBiteHeal * Math.max(0.0F, Math.min(40.0F, prevHealth - ((EntityLivingBase)entity).getHealth()));
            // Don't bother attempting to heal tiny amounts
            if (changed > 1.0F) this.heal(changed);
        }
        return success;
    }

    @Override
    public boolean attackEntityFrom(DamageSource dmg, float i) {
        if (this.isModelDead()) {
            return false;
        }

        if (this.isBeingRidden() && dmg.getTrueSource() != null && this.getControllingPassenger() != null && dmg.getTrueSource() == this.getControllingPassenger()) {
            return false;
        }

        if ((dmg.damageType.contains("arrow") || getRidingEntity() != null && dmg.getTrueSource() != null && dmg.getTrueSource().isEntityEqual(this.getRidingEntity())) && this.isRiding()) {
            return false;
        }

        if (dmg == DamageSource.IN_WALL || dmg == DamageSource.FALLING_BLOCK) {
            return false;
        }

        if (!world.isRemote && dmg.getTrueSource() != null && this.getRNG().nextInt(4) == 0) {
            this.roar();
        }

        if (i > 0) {
            if (this.isSleeping()) {
                this.setSleeping(false);
                if (!this.isTamed()) {
                    if (dmg.getTrueSource() instanceof EntityPlayer) {
                        this.setAttackTarget((EntityPlayer)dmg.getTrueSource());
                    }
                }
            }
        }
        return super.attackEntityFrom(dmg, i);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        updateParts();
        this.setScaleForAge(true);
        if (world.isRemote) {
            this.updateClientControls();
        }
        if (this.isModelDead()) {
            return;
        }
        if (this.up()) {
            if (!this.isFlying() && !this.isHovering()) {
                this.spacebarTicks += 2;
            }
            if (this.isFlying() || this.isHovering()) {
                this.motionY += 0.4D;
            }
        } else if (this.dismount()) {
            if (this.isFlying() || this.isHovering()) {
                this.motionY -= 0.4D;
                this.setFlying(false);
                this.setHovering(false);
            }
        }
        if (this.down() && (this.isFlying() || this.isHovering())) {
            this.motionY -= 0.4D;
        }
        if (!this.dismount() && (this.isFlying() || this.isHovering())) {
            this.motionY += 0.01D;
        }
        if (this.attack() && this.isPlayerControlled() && this.getDragonStage() > 1) {
            this.setBreathingFire(true);
            this.riderShootFire(this.getControllingPassenger());
            this.fireStopTicks = 10;
        }
        if (this.strike() && this.isPlayerControlled()) {
            EntityLivingBase target = DragonUtils.riderLookingAtEntity(this, (EntityPlayer) this.getControllingPassenger(), this.getDragonStage() + (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX));
            if (this.getAnimation() != ANIMATION_BITE) {
                this.setAnimation(ANIMATION_BITE);
            }
            if (target != null && !DragonUtils.hasSameOwner(this, target)) {
                this.doBiteAttack(target);
            }
        }
        if (this.isPlayerControlled() && this.getControllingPassenger().isSneaking()) {
            this.getControllingPassenger().setSneaking(false);
            this.getControllingPassenger().dismountRidingEntity();
            if (world.isRemote) {
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageUpdateRidingState(this.getEntityId(), false));
            }
        }
        if (this.isFlying() && !this.isHovering() && this.isPlayerControlled() && !this.onGround && Math.max(Math.abs(motionZ), Math.abs(motionX)) < 0.1F) {
            this.setHovering(true);
            this.setFlying(false);
        } else if (this.isHovering() && !this.isFlying() && this.isPlayerControlled() && !this.onGround && Math.max(Math.abs(motionZ), Math.abs(motionX)) > 0.1F) {
            this.setFlying(true);
            this.setHovering(false);
        }
        if (this.spacebarTicks > 0) {
            this.spacebarTicks--;
        }
        if (!world.isRemote && this.spacebarTicks > 20 && this.isPlayerControlled() && !this.isFlying() && !this.isHovering()) {
            this.setHovering(true);
        }
        if (world.isRemote && !this.isModelDead()) {
            roll_buffer.calculateChainFlapBuffer(50, 10, 4, this);
            turn_buffer.calculateChainSwingBuffer(50, 0, 4, this);
            tail_buffer.calculateChainSwingBuffer(90, 10, 2.5F, this);
        }
        if (this.getAttackTarget() != null && this.getRidingEntity() == null && this.getAttackTarget().isDead || this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityDragonBase && this.getAttackTarget().isDead) {
            this.setAttackTarget(null);
        }
        if (!this.world.isRemote) {
            IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
            if (capability != null && !capability.isStoned()) {
                if (!this.isInWater() && !this.isSleeping() && this.onGround && !this.isFlying() && !this.isHovering() && this.getAttackTarget() == null && !this.isTimeToWake() && this.getRNG().nextInt(250) == 0 && this.getAttackTarget() == null && this.getPassengers().isEmpty()) {
                    this.setSleeping(true);
                }
                if (this.isSleeping() && (this.isFlying() || this.isHovering() || this.isInWater() || (this.world.canBlockSeeSky(new BlockPos(this)) && this.isTimeToWake() && !this.isTamed() || this.isTimeToWake() && this.isTamed()) || this.getAttackTarget() != null || !this.getPassengers().isEmpty())) {
                    this.setSleeping(false);
                }
                if (this.isSitting() && this.getControllingPassenger() != null) {
                    this.setSitting(false);
                }
            }
        }
    }

    @Override
    public void setScaleForAge(boolean par1) {
        float scale = Math.min(this.getRenderSize() * 0.35F, 7F);
        this.setScale(scale);
        if (scale != lastScale) {
            resetParts(this.getRenderSize() / 3);
        }
        lastScale = scale;
    }

    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public float getRenderSize() {
        final int stage = this.getDragonStage() - 1;
        final float step = (growth_stages[stage][1] - growth_stages[stage][0]) / 25;
        if (this.getAgeInDays() > 125) {
            return growth_stages[stage][0] + (step * 25);
        }
        return growth_stages[stage][0] + (step * this.getAgeFactor());
    }

    private int getAgeFactor() {
        return (this.getDragonStage() > 1 ? this.getAgeInDays() - (25 * (this.getDragonStage() - 1)) : this.getAgeInDays());
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.isTackling()) {
            return false;
        }
        if (this.isModelDead()) {
            return false;
        }
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

        if (flag) {
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isRiding() && entity.isDead) {
            this.dismountRidingEntity();
        } else {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onUpdate();
            if (this.isRiding()) {
                this.updateRiding(entity);
            }
        }
    }

    public void updateRiding(Entity riding) {
        if (riding != null && riding.isPassenger(this) && riding instanceof EntityPlayer) {
            int i = riding.getPassengers().indexOf(this);
            float radius = (i == 2 ? 0F : 0.5F) + (((EntityPlayer) riding).isElytraFlying() ? 2 : 0);
            float angle = (0.01745329251F * ((EntityPlayer) riding).renderYawOffset) + (i == 1 ? 90 : i == 0 ? -90 : 0);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            double extraY = (riding.isSneaking() ? 1.2D : 1.4D) + (i == 2 ? 0.4D : 0D);
            this.rotationYaw = ((EntityPlayer) riding).rotationYawHead;
            this.rotationYawHead = ((EntityPlayer) riding).rotationYawHead;
            this.prevRotationYaw = ((EntityPlayer) riding).rotationYawHead;
            this.setPosition(riding.posX + extraX, riding.posY + extraY, riding.posZ + extraZ);
            if (this.getControlState() == 1 << 4 || ((EntityPlayer) riding).isElytraFlying()) {
                this.dismountRidingEntity();
            }
        }
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
        if (this.isModelDead()) {
            return this.NO_ANIMATION;
        }
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        if (this.isModelDead()) {
            return;
        }
        currentAnimation = animation;
    }

    public void playLivingSound() {
        if (!this.isSleeping() && !this.isModelDead()) {
            if (this.getAnimation() == this.NO_ANIMATION && !this.world.isRemote) {
                this.setAnimation(ANIMATION_SPEAK);
            }
            super.playLivingSound();
        }
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        if (!this.isModelDead()) {
            if (this.getAnimation() == this.NO_ANIMATION && !this.world.isRemote) {
                this.setAnimation(ANIMATION_SPEAK);
            }
            super.playHurtSound(source);
        }
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{IAnimatedEntity.NO_ANIMATION, EntityDragonBase.ANIMATION_EAT};
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal instanceof EntityDragonBase && otherAnimal != this && otherAnimal.getClass() == this.getClass()) {
            EntityDragonBase dragon = (EntityDragonBase) otherAnimal;
            if (!this.isAdult() || !this.isInLove() || this.isBeingRidden()) {
                return false;
            }
            if (!dragon.isAdult() || !dragon.isInLove() || dragon.isBeingRidden()) {
                return false;
            }
            return this.isMale() && !dragon.isMale() || !this.isMale() && dragon.isMale();
        }
        return false;
    }

    public EntityDragonEgg createEgg(EntityDragonBase mate) {
        int i = MathHelper.floor(this.posX);
        int j = MathHelper.floor(this.posY);
        int k = MathHelper.floor(this.posZ);
        BlockPos pos = new BlockPos(i, j, k);
        EntityDragonEgg dragon = new EntityDragonEgg(this.world);
        int rand = new Random().nextInt(100);
        int typeValue = this.getBaseEggTypeValue();
        if (rand >= 70) {
            typeValue += this.getVariant();
        } else if (rand >= 40) {
            typeValue += mate.getVariant();
        } else {
            typeValue += rand % 4;
        }
        dragon.setType(EnumDragonEgg.byMetadata(typeValue));
        dragon.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        return dragon;
    }

    public void flyAround() {
        if (airTarget != null) {
            if (doesWantToLand()) {
                airTarget = null;
            }
            flyTowardsTarget();
        }
    }

    public boolean isTargetBlocked(Vec3d target) {
        if (target != null) {
            RayTraceResult rayTrace = world.rayTraceBlocks(new Vec3d(this.getPosition()), target, false);
            if (rayTrace != null && rayTrace.hitVec != null) {
                BlockPos pos = new BlockPos(rayTrace.hitVec);
                if (!world.isAirBlock(pos) || world.getBlockState(pos).getMaterial() == Material.WATER && dragonType == EnumDragonType.ICE) {
                    return true;
                }
                return rayTrace.typeOfHit != RayTraceResult.Type.BLOCK;
            }
        }
        return false;
    }

    public void flyTowardsTarget() {
        if (airTarget == null) {
            return;
        }
        int maximumDragonFlightHeight = DragonUtils.getMaximumFlightHeightForPos(world, new BlockPos(airTarget));
        if (airTarget.getY() > maximumDragonFlightHeight) {
            airTarget = new BlockPos(airTarget.getX(), maximumDragonFlightHeight, airTarget.getZ());
        }
        if (isTargetInAir() && this.isFlying() && this.getDistanceSquared(new Vec3d(airTarget.getX(), this.posY, airTarget.getZ())) > 3) {
            double y = this.attackDecision ? airTarget.getY() : this.posY;

            double targetX = airTarget.getX() + 0.5D - posX;
            double targetY = Math.min(y, maximumDragonFlightHeight) + 1D - posY;
            double targetZ = airTarget.getZ() + 0.5D - posZ;
            motionX += (Math.signum(targetX) * 0.5D - motionX) * 0.100000000372529 * getFlySpeed();
            motionY += (Math.signum(targetY) * 0.5D - motionY) * 0.100000000372529 * getFlySpeed();
            motionZ += (Math.signum(targetZ) * 0.5D - motionZ) * 0.100000000372529 * getFlySpeed();
            moveForward = 0.5F;
            double d0 = airTarget.getX() + 0.5D - this.posX;
            double d2 = airTarget.getZ() + 0.5D - this.posZ;
            double d1 = y + 0.5D - this.posY;
            double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            float f1 = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
            this.rotationPitch = this.updateRotation(this.rotationPitch, f1, 30F);
            this.rotationYaw = this.updateRotation(this.rotationYaw, f, 30F);
        } else {
            this.airTarget = null;
        }
        if (airTarget != null) {
             if (!isFlying()) {
                this.setFlying(true);
                this.setHovering(false);
                flyTicks = 0;
                hoverTicks = 0;
            }
             if (this.getDistanceSquared(new Vec3d(airTarget.getX(), this.posY, airTarget.getZ())) < 3 && this.doesWantToLand()) {
                setFlying(false);
                setHovering(true);
            }
        }
    }

    private double getFlySpeed() {
        return (2 + ((double) this.getAgeInDays() / 125) * 2) * (this.isTackling() ? 2 : 1);
    }

    private boolean isTackling() {
        if (world.isRemote) {
            boolean tackling = this.dataManager.get(TACKLE);
            this.isTackling = tackling;
            return tackling;
        }
        return isTackling;
    }

    private boolean isAgingDisabled() {
        return this.dataManager.get(AGINGDISABLED);
    }

    protected boolean isTargetInAir() {
        return this.airTarget != null && isPosInAir(this.airTarget);
    }

    public boolean isPosInAir(BlockPos pos) {
        if (world.getBlockState(pos).getMaterial() == Material.AIR) {
            return true;
        }
        if (this instanceof EntityIceDragon) {
            return world.getBlockState(pos).getMaterial() == Material.WATER;
        }
        return false;
    }

    private float updateRotation(float angle, float targetAngle, float maxIncrease) {
        float f = MathHelper.wrapDegrees(targetAngle - angle);

        if (f > maxIncrease) {
            f = maxIncrease;
        }

        if (f < -maxIncrease) {
            f = -maxIncrease;
        }

        return angle + f;
    }

    public float getDistanceSquared(Vec3d vec3d) {
        float f = (float) (this.posX - vec3d.x);
        float f1 = (float) (this.posY - vec3d.y);
        float f2 = (float) (this.posZ - vec3d.z);
        return f * f + f1 * f1 + f2 * f2;
    }


    public boolean replaceItemInInventory(int inventorySlot, @Nullable ItemStack itemStackIn) {
        int j = inventorySlot - 500 + 2;
        if (j >= 0 && j < this.dragonInv.getSizeInventory()) {
            this.dragonInv.setInventorySlotContents(j, itemStackIn);
            return true;
        } else {
            return false;
        }
    }

    public abstract Item getVariantScale(int variant);

    public abstract Item getVariantEgg(int variant);

    @SideOnly(Side.CLIENT)
    protected void updateClientControls() {
        Minecraft mc = Minecraft.getMinecraft();
        if (this.isControllingPassenger(mc.player)) {
            byte previousState = getControlState();
            up(mc.gameSettings.keyBindJump.isKeyDown());
            down(ModKeys.dragon_down.isKeyDown());
            attack(ModKeys.dragon_fireAttack.isKeyDown());
            strike(ModKeys.dragon_strike.isKeyDown());
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

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return !isModelDead();
    }

    @Override
    public boolean canBeSteered() {
        return true;
    }

    @Override
    public void travel(float strafe, float forward, float vertical) {
        if (this.getAnimation() == ANIMATION_SHAKEPREY || !this.canMove() && !this.isBeingRidden()) {
            strafe = 0;
            forward = 0;
            vertical = 0;
            super.travel(strafe, forward, vertical);
            return;
        }
        if (this.isPlayerControlled() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase) this.getControllingPassenger();
            if (controller != null && controller != this.getAttackTarget()) {
                this.rotationYaw = controller.rotationYaw;
                this.prevRotationYaw = this.rotationYaw;
                this.rotationPitch = controller.rotationPitch * 0.5F;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.renderYawOffset = this.rotationYaw;
                this.rotationYawHead = this.renderYawOffset;
                strafe = controller.moveStrafing * 0.5F;
                forward = controller.moveForward;
                if (forward <= 0.0F) {
                    forward *= 0.25F;
                }
                if (this.isFlying() || this.isHovering()) {
                    motionX *= 1.06;
                    motionZ *= 1.06;
                }
                jumpMovementFactor = 0.05F;
                this.setAIMoveSpeed(onGround ? (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() : (float) getFlySpeed());
                super.travel(strafe, 0, forward);
                return;
            }
        }
        super.travel(strafe, forward, vertical);
    }

    public void updateCheckPlayer() {
        double checklength = this.getEntityBoundingBox().getAverageEdgeLength() * 3;
        EntityPlayer player = world.getClosestPlayerToEntity(this, checklength);
        if (this.isSleeping()) {
            if (player != null && !this.isOwner(player) && !player.isCreative()) {
                this.setSleeping(false);
                this.setSitting(false);
                this.setAttackTarget(player);
            }
        }
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    public boolean isDirectPathBetweenPoints(Vec3d vec1, Vec3d vec2) {
        RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec1, new Vec3d(vec2.x, vec2.y + (double) this.height * 0.5D, vec2.z), false, true, false);
        return movingobjectposition == null || movingobjectposition.typeOfHit != RayTraceResult.Type.BLOCK;
    }

    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (dragonInv != null && !this.world.isRemote) {
            IEntityEffectCapability cap = InFCapabilities.getEntityEffectCapability(this);
            if (cap != null && cap.isStoned()) return;
            for (int i = 0; i < dragonInv.getSizeInventory(); ++i) {
                ItemStack itemstack = dragonInv.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }

    @Override
    public void onHearFlute(EntityPlayer player) {
        if (this.isTamed() && this.isOwner(player)) {
            if (this.isFlying() || this.isHovering()) {
                this.setFlying(false);
                this.setHovering(false);
            }
        }
    }

    public abstract SoundEvent getRoarSound();

    public void roar() {
        if (EntityGorgon.isStoneMob(this)) {
            return;
        }
        if (this.getAnimation() != ANIMATION_ROAR) {
            this.setAnimation(ANIMATION_ROAR);
            this.playSound(this.getRoarSound(), this.getSoundVolume() + 2 + Math.max(0, this.getDragonStage() - 3), this.getSoundPitch());
        }
        if (this.getDragonStage() > 3) {
            int size = (this.getDragonStage() - 3) * 30;
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(size, size, size));
            for (Entity entity : entities) {
                boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                if (entity instanceof EntityLivingBase && !isStrongerDragon) {
                    EntityLivingBase living = (EntityLivingBase) entity;
                    if (this.isOwner(living) || this.isOwnersPet(living)) {
                        living.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * size));
                    } else {
                        if (living.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() != ModItems.earplugs) {
                            living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 30 * size));
                        }
                    }
                }
            }
        }
    }

    private boolean isOwnersPet(EntityLivingBase living) {
        return this.isTamed() && this.getOwner() != null && living instanceof EntityTameable && ((EntityTameable) living).getOwner() != null && this.getOwner().isEntityEqual(((EntityTameable) living).getOwner());
    }

    public boolean isDirectPathBetweenPoints(Entity entity, Vec3d vec1, Vec3d vec2) {
        RayTraceResult movingobjectposition = entity.world.rayTraceBlocks(vec1, new Vec3d(vec2.x, vec2.y + (double) entity.height * 0.5D, vec2.z), false, true, false);
        return movingobjectposition == null || movingobjectposition.typeOfHit != RayTraceResult.Type.BLOCK;
    }

    public boolean shouldRenderEyes() {
        return !this.isSleeping() && !this.isModelDead() && !this.isBlinking() && !EntityGorgon.isStoneMob(this);
    }

    @Override
    public boolean shouldAnimalsFear(Entity entity) {
        return DragonUtils.canTameDragonAttack(this, entity);
    }

    public void dropArmor() {
        IEntityEffectCapability cap = InFCapabilities.getEntityEffectCapability(this);
        if (cap != null && cap.isStoned()) return;
        if (dragonInv != null && !this.world.isRemote) {
            for (int i = 0; i < dragonInv.getSizeInventory(); ++i) {
                ItemStack itemstack = dragonInv.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }

    public Vec3d getHeadPosition() {
        float deadProg = this.modelDeadProgress * -0.02F;
        float hoverProg = this.hoverProgress * 0.03F;
        float flyProg = this.flyProgress * 0.01F;
        float sitProg = this.sitProgress * 0.015F;
        float sleepProg = this.sleepProgress * -0.025F;
        final float flightXz = 1.0F + flyProg + hoverProg;
        final float xzMod = 1.7F * getRenderSize() * 0.3F * flightXz + getRenderSize() * hoverProg * -0.45F;
        final float headPosX = (float) (posX + (xzMod) * Math.cos((rotationYaw + 90) * Math.PI / 180));
        final float headPosY = (float) (posY + (0.7F + sitProg + hoverProg + deadProg + sleepProg + flyProg) * getRenderSize() * 0.3F);
        final float headPosZ = (float) (posZ + (xzMod) * Math.sin((rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(headPosX, headPosY, headPosZ);
    }

    public boolean writeToNBTOptional(NBTTagCompound compound) {
        String s = this.getEntityString();
        compound.setString("id", s);
        this.writeToNBT(compound);
        return true;
    }

    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        if (soundIn == SoundEvents.ENTITY_GENERIC_EAT || soundIn == this.getAmbientSound() || soundIn == this.getHurtSound(null) || soundIn == this.getDeathSound() || soundIn == this.getRoarSound()) {
            if (!this.isSilent()) {
                Vec3d headPos = getHeadPosition();
                this.world.playSound(null, headPos.x, headPos.y, headPos.z, soundIn, this.getSoundCategory(), volume, pitch);
            }
        } else {
            super.playSound(soundIn, volume, pitch);
        }
    }

    public boolean canDismount() {
        return currentAnimation != ANIMATION_SHAKEPREY || animationTick > 60;
    }
}