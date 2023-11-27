package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.IceAndFireConfig;
import com.github.alexthe666.iceandfire.api.IEntityEffectCapability;
import com.github.alexthe666.iceandfire.api.InFCapabilities;
import com.github.alexthe666.iceandfire.client.model.IFChainBuffer;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.*;
import com.github.alexthe666.iceandfire.entity.util.*;
import com.github.alexthe666.iceandfire.enums.EnumHippogryphTypes;
import com.github.alexthe666.iceandfire.message.MessageDragonControl;
import com.github.alexthe666.iceandfire.message.MessageHippogryphArmor;
import com.github.alexthe666.iceandfire.util.ParticleHelper;
import com.google.common.base.Predicate;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityHippogryph extends EntityTameable implements IAnimatedEntity, IDragonFlute, IVillagerFear, IAnimalFear, IDropArmor {

	public static final ResourceLocation LOOT = LootTableList.register(new ResourceLocation("iceandfire", "hippogryph"));
	private static final int FLIGHT_CHANCE_PER_TICK = 1200;
	private static final DataParameter<Integer> VARIANT = EntityDataManager.<Integer>createKey(EntityHippogryph.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> SADDLE = EntityDataManager.<Boolean>createKey(EntityHippogryph.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> ARMOR = EntityDataManager.<Integer>createKey(EntityHippogryph.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> CHESTED = EntityDataManager.<Boolean>createKey(EntityHippogryph.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> HOVERING = EntityDataManager.<Boolean>createKey(EntityHippogryph.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> FLYING = EntityDataManager.<Boolean>createKey(EntityHippogryph.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Byte> CONTROL_STATE = EntityDataManager.createKey(EntityHippogryph.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> COMMAND = EntityDataManager.<Integer>createKey(EntityHippogryph.class, DataSerializers.VARINT);
	public static Animation ANIMATION_EAT;
	public static Animation ANIMATION_SPEAK;
	public static Animation ANIMATION_SCRATCH;
	public static Animation ANIMATION_BITE;
	public HippogryphInventory hippogryphInventory;
	@SideOnly(Side.CLIENT)
	public IFChainBuffer roll_buffer;
	public float sitProgress;
	public float hoverProgress;
	public float flyProgress;
	public int spacebarTicks;
	public BlockPos airTarget;
	public int airBorneCounter;
	private boolean isSitting;
	private boolean isHovering;
	private boolean isFlying;
	private int animationTick;
	private Animation currentAnimation;
	private int flyTicks;
	private int hoverTicks;
	private boolean hasChestVarChanged = false;
	public BlockPos homePos;
	public boolean hasHomePosition = false;

	public EntityHippogryph(World worldIn) {
		super(worldIn);
		ANIMATION_EAT = Animation.create(25);
		ANIMATION_SPEAK = Animation.create(15);
		ANIMATION_SCRATCH = Animation.create(25);
		ANIMATION_BITE = Animation.create(20);
		initHippogryphInv();
		if (FMLCommonHandler.instance().getSide().isClient()) {
			roll_buffer = new IFChainBuffer();
		}
		this.setSize(1.7F, 1.6F);
		this.stepHeight = 1;
	}

	protected int getExperiencePoints(EntityPlayer player) {
		return 10;
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, this.aiSit = new EntityAISit(this));
		this.tasks.addTask(3, new HippogryphAIAttackMelee(this, 1.5D, true));
		this.tasks.addTask(4, new HippogryphAIMate(this, 1.0D));
		this.tasks.addTask(5, new EntityAITempt(this, 1.0D, Items.RABBIT, false));
		this.tasks.addTask(5, new EntityAITempt(this, 1.0D, Items.COOKED_RABBIT, false));
		this.tasks.addTask(6, new HippogryphAIAirTarget(this));
		this.tasks.addTask(7, new HippogryphAIWander(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLivingBase.class, 6.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(4, new HippogryphAITargetItems<>(this, false));
		this.targetTasks.addTask(5, new HippogryphAITarget<>(this, EntityLivingBase.class, false, new Predicate<Entity>() {
			@Override
			public boolean apply(@Nullable Entity entity) {
				return entity instanceof EntityLivingBase && !(entity instanceof AbstractHorse) && DragonUtils.isAlive((EntityLivingBase) entity);
			}
		}));

	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VARIANT, 0);
		this.dataManager.register(ARMOR, 0);
		this.dataManager.register(SADDLE, Boolean.FALSE);
		this.dataManager.register(CHESTED, Boolean.FALSE);
		this.dataManager.register(HOVERING, Boolean.FALSE);
		this.dataManager.register(FLYING, Boolean.FALSE);
		this.dataManager.register(CONTROL_STATE, (byte) 0);
		this.dataManager.register(COMMAND, 0);

	}

	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
	{
	}

	public boolean canBeSteered() {
		return true;
	}

	public void updatePassenger(Entity passenger) {
		super.updatePassenger(passenger);
		if (this.isPassenger(passenger)) {
			renderYawOffset = rotationYaw;
			this.rotationYaw = passenger.rotationYaw;
		}
		passenger.setPosition(this.posX, this.posY + 1.05F, this.posZ);
	}

	private void initHippogryphInv() {
		HippogryphInventory animalchest = this.hippogryphInventory;
		this.hippogryphInventory = new HippogryphInventory("hippogryphInventory", 18, this);
		this.hippogryphInventory.setCustomName(this.getName());
		if (animalchest != null) {
			int i = Math.min(animalchest.getSizeInventory(), this.hippogryphInventory.getSizeInventory());
			for (int j = 0; j < i; ++j) {
				ItemStack itemstack = animalchest.getStackInSlot(j);
				if (!itemstack.isEmpty()) {
					this.hippogryphInventory.setInventorySlotContents(j, itemstack.copy());
				}
			}

			if (world.isRemote) {
				ItemStack saddle = animalchest.getStackInSlot(0);
				ItemStack chest = animalchest.getStackInSlot(1);
				IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle != null && saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
				IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest != null && chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
				IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(animalchest.getStackInSlot(2))));
			}
		}
	}

	@Nullable
	public Entity getControllingPassenger() {
		for (Entity passenger : this.getPassengers()) {
			if (passenger instanceof EntityPlayer && this.getAttackTarget() != passenger) {
				EntityPlayer player = (EntityPlayer) passenger;
				if (this.isTamed() && this.getOwnerId() != null && this.getOwnerId().equals(player.getUniqueID())) {
					return player;
				}
			}
		}
		return null;
	}

	public int getIntFromArmor(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() == ModItems.iron_hippogryph_armor) {
			return 1;
		}
		if (!stack.isEmpty() && stack.getItem() == ModItems.gold_hippogryph_armor) {
			return 2;
		}
		if (!stack.isEmpty() && stack.getItem() == ModItems.diamond_hippogryph_armor) {
			return 3;
		}
		return 0;
	}

	public boolean isBlinking() {
		return this.ticksExisted % 50 > 43;
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.getItem() == Items.RABBIT_STEW;
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		String s = TextFormatting.getTextWithoutFormattingCodes(player.getName());
		boolean isDev = s.equals("Alexthe666") || s.equals("Raptorfarian");
		if (this.isTamed() && this.isOwner(player)) {
			if (stack.getItem() == Items.DYE && stack.getMetadata() == 1 && this.getEnumVariant() != EnumHippogryphTypes.ALEX && isDev) {
				this.setEnumVariant(EnumHippogryphTypes.ALEX);
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				this.playSound(SoundEvents.ENTITY_ZOMBIE_INFECT, 1, 1);
				if(this.world.isRemote) {
					for (int i = 0; i < 20; i++) {
						ParticleHelper.spawnParticle(this.world, EnumParticleTypes.REDSTONE, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0, 0, 0);
					}
				}
				return true;
			}
			if (stack.getItem() == Items.DYE && stack.getMetadata() == 7 && this.getEnumVariant() != EnumHippogryphTypes.RAPTOR && isDev) {
				this.setEnumVariant(EnumHippogryphTypes.RAPTOR);
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				this.playSound(SoundEvents.ENTITY_ZOMBIE_INFECT, 1, 1);
				if(this.world.isRemote) {
					for (int i = 0; i < 20; i++) {
						ParticleHelper.spawnParticle(this.world, EnumParticleTypes.CLOUD, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0, 0, 0);
					}
				}
				return true;
			}
			if (this.isBreedingItem(stack) && this.getGrowingAge() == 0 && !isInLove()) {
				this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
				if(!player.isCreative()) stack.shrink(1);
				this.setInLove(player);
				return true;
			}
			if (this.isBreedingItem(stack) && this.isChild()) {
				this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
				if(!player.isCreative()) stack.shrink(1);
				this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
				return true;
			}
			if (stack.getItem() == Items.STICK) {
				if(player.isSneaking()){
					BlockPos pos = new BlockPos(this);
					this.homePos = pos;
					this.hasHomePosition = true;
					player.sendStatusMessage(new TextComponentTranslation("hippogryph.command.new_home", homePos.getX(), homePos.getY(), homePos.getZ()), true);
					return true;
				}else{
					this.setCommand(this.getCommand() + 1);
					if (this.getCommand() > 1) {
						this.setCommand(0);
					}
					player.sendStatusMessage(new TextComponentTranslation("hippogryph.command." + (this.getCommand() == 1 ? "sit" : "stand")), true);

				}
				return true;
			}
			if (stack.getItem() == Items.SPECKLED_MELON && this.getEnumVariant() != EnumHippogryphTypes.DODO) {
				this.setEnumVariant(EnumHippogryphTypes.DODO);
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				this.playSound(SoundEvents.ENTITY_ZOMBIE_INFECT, 1, 1);
				if(this.world.isRemote) {
					for (int i = 0; i < 20; i++) {
						ParticleHelper.spawnParticle(this.world, EnumParticleTypes.ENCHANTMENT_TABLE, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0, 0, 0);
					}
				}
				return true;
			}
			if (stack.getItem() instanceof ItemFood && ((ItemFood) stack.getItem()).isWolfsFavoriteMeat() && this.getHealth() < this.getMaxHealth()) {
				this.heal(5);
				this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);
				if(this.world.isRemote) {
					for (int i = 0; i < 3; i++) {
						ParticleHelper.spawnParticle(this.world, EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0, 0, 0, Item.getIdFromItem(stack.getItem()), 0);
					}
				}
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				return true;
			}
			if(stack.isEmpty()) {
				if (player.isSneaking()) {
					this.openGUI(player);
					return true;
				} else if (this.isSaddled() && !this.isChild() && !player.isRiding()) {
					player.startRiding(this, true);
					return true;
				}
			}
		}
		//Don't call EntityAnimal::processInteract() due to custom breeding handling
		if(stack.getItem() == Items.SPAWN_EGG) {
			if(!this.world.isRemote) {
				Class <? extends Entity > oclass = EntityList.getClass(ItemMonsterPlacer.getNamedIdFrom(stack));
				if(oclass != null && this.getClass() == oclass) {
					EntityAgeable entityageable = this.createChild(this);

					if(entityageable != null) {
						entityageable.setGrowingAge(-24000);
						entityageable.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
						this.world.spawnEntity(entityageable);

						if(stack.hasDisplayName()) {
							entityageable.setCustomNameTag(stack.getDisplayName());
						}

						if(!player.capabilities.isCreativeMode) {
							stack.shrink(1);
						}
					}
				}
			}
			return true;
		}
		else return false;
	}


	public void openGUI(EntityPlayer playerEntity) {
		if (!this.world.isRemote && (!this.isBeingRidden() || this.isPassenger(playerEntity))) {
			playerEntity.openGui(IceAndFire.INSTANCE, 4, this.world, this.getEntityId(), 0, 0);
		}
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

	public byte getControlState() {
		return dataManager.get(CONTROL_STATE);
	}

	public void setControlState(byte state) {
		dataManager.set(CONTROL_STATE, state);
	}

	public void setCommand(int command) {
		this.dataManager.set(COMMAND, command);
		this.setSitting(command == 1);
	}

	public int getCommand() {
		return this.dataManager.get(COMMAND).intValue();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("Variant", this.getVariant());
		compound.setBoolean("Chested", this.isChested());
		compound.setBoolean("Saddled", this.isSaddled());
		compound.setBoolean("Hovering", this.isHovering());
		compound.setBoolean("Flying", this.isFlying());
		compound.setInteger("Armor", this.getArmor());
		if (hippogryphInventory != null) {
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < this.hippogryphInventory.getSizeInventory(); ++i) {
				ItemStack itemstack = this.hippogryphInventory.getStackInSlot(i);
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
		this.setChested(compound.getBoolean("Chested"));
		this.setSaddled(compound.getBoolean("Saddled"));
		this.setHovering(compound.getBoolean("Hovering"));
		this.setFlying(compound.getBoolean("Flying"));
		this.setArmor(compound.getInteger("Armor"));
		if (hippogryphInventory != null) {
			NBTTagList nbttaglist = compound.getTagList("Items", 10);
			this.initHippogryphInv();
			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound.getByte("Slot") & 255;
				this.hippogryphInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
			}
		} else {
			NBTTagList nbttaglist = compound.getTagList("Items", 10);
			this.initHippogryphInv();
			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound.getByte("Slot") & 255;
				this.initHippogryphInv();
				this.hippogryphInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
				//this.setArmorInSlot(j, this.getIntFromArmor(ItemStack.loadItemStackFromNBT(nbttagcompound)));
				ItemStack saddle = hippogryphInventory.getStackInSlot(0);
				ItemStack chest = hippogryphInventory.getStackInSlot(1);
				if (world.isRemote) {
					IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
					IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
					IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(hippogryphInventory.getStackInSlot(2))));
				}
			}
		}
		this.hasHomePosition = compound.getBoolean("HasHomePosition");
		if (hasHomePosition && compound.getInteger("HomeAreaX") != 0 && compound.getInteger("HomeAreaY") != 0 && compound.getInteger("HomeAreaZ") != 0) {
			homePos = new BlockPos(compound.getInteger("HomeAreaX"), compound.getInteger("HomeAreaY"), compound.getInteger("HomeAreaZ"));
		}
		this.setCommand(compound.getInteger("Command"));
	}

	public int getVariant() {
		return this.dataManager.get(VARIANT);
	}

	public EnumHippogryphTypes getEnumVariant() {
		return EnumHippogryphTypes.values()[this.getVariant()];
	}

	public void setEnumVariant(EnumHippogryphTypes variant) {
		this.setVariant(variant.ordinal());
	}

	public void setVariant(int variant) {
		this.dataManager.set(VARIANT, variant);
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
		byte b0 = this.dataManager.get(TAMED);
		if (sitting) {
			this.dataManager.set(TAMED, (byte) (b0 | 1));
		} else {
			this.dataManager.set(TAMED, (byte) (b0 & -2));
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

	public int getArmor() {
		return this.dataManager.get(ARMOR);
	}

	public void setArmor(int armorType) {
		this.dataManager.set(ARMOR, armorType);
		double armorValue = 0;
		switch(armorType){
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

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
	}

	public boolean canMove() {
		IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
		if(capability != null && capability.isStoned()){
			return false;
		}
		return !this.isSitting() && this.getControllingPassenger() == null && sitProgress == 0;
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		this.setEnumVariant(EnumHippogryphTypes.getBiomeType(world.getBiome(this.getPosition())));
		return livingdata;
	}

	@Override
	public boolean attackEntityFrom(DamageSource dmg, float i) {
		if (this.isBeingRidden() && dmg.getTrueSource() != null && this.getControllingPassenger() != null && dmg.getTrueSource() == this.getControllingPassenger()) {
			return false;
		}
		return super.attackEntityFrom(dmg, i);
	}

	@Nullable
	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return this.getEntityBoundingBox().grow(2.0, 1.0, 2.0).offset(0.0, 0.5, 1.0);
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

	@Nullable
	protected SoundEvent getAmbientSound() {
		return ModSounds.HIPPOGRYPH_IDLE;
	}

	@Nullable
	protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
		return ModSounds.HIPPOGRYPH_HURT;
	}

	@Nullable
	protected SoundEvent getDeathSound() {
		return ModSounds.HIPPOGRYPH_DIE;
	}

	@Override
	public Animation[] getAnimations() {
		return new Animation[]{IAnimatedEntity.NO_ANIMATION, EntityHippogryph.ANIMATION_EAT, EntityHippogryph.ANIMATION_BITE, EntityHippogryph.ANIMATION_SPEAK, EntityHippogryph.ANIMATION_SCRATCH};
	}

	public boolean isRidingPlayer(EntityPlayer player) {
		return this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer && this.getControllingPassenger().getUniqueID().equals(player.getUniqueID());
	}

	@Override
	public boolean shouldDismountInWater(Entity rider) {
		return true;
	}

	public boolean isDirectPathBetweenPoints(Vec3d vec1, Vec3d vec2) {
		RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec1, new Vec3d(vec2.x, vec2.y + (double) this.height * 0.5D, vec2.z), false, true, false);
		return movingobjectposition == null || movingobjectposition.typeOfHit != RayTraceResult.Type.BLOCK;
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

	@Override
		public void travel(float strafe, float forward, float vertical) {
			if (!this.canMove() && !this.isBeingRidden()) {
				strafe = 0;
				forward = 0;
				super.travel(strafe, forward, vertical);
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
					if (this.isFlying() || this.isHovering()) {
						motionX *= 1.06;
						motionZ *= 1.06;
					}
					jumpMovementFactor = 0.05F;
					this.setAIMoveSpeed(onGround ? (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() : 2);
					super.travel(strafe, vertical = 0, forward);
					return;
				}
			}
		super.travel(strafe, forward, vertical);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (this.getAnimation() != ANIMATION_SCRATCH && this.getAnimation() != ANIMATION_BITE) {
			this.setAnimation(this.getRNG().nextBoolean() ? ANIMATION_SCRATCH : ANIMATION_BITE);
		} else {
			return true;
		}
		return false;
	}

	public EntityItem createEgg(EntityHippogryph partner) {
		int i = MathHelper.floor(this.posX);
		int j = MathHelper.floor(this.posY);
		int k = MathHelper.floor(this.posZ);
		ItemStack stack = new ItemStack(ModItems.hippogryph_egg);
		EntityItem egg = new EntityItem(this.world, i, j, k, stack);
		return egg;
	}

	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			if (this.isSitting() && (this.getCommand() != 1 || this.getControllingPassenger() != null)) {
				this.setSitting(false);
			}
			if (!this.isSitting() && this.getCommand() == 1 && this.getControllingPassenger() == null) {
				this.setSitting(true);
			}
			if(this.isSitting()){
				this.getNavigator().clearPath();
			}
			if (this.rand.nextInt(900) == 0 && this.deathTime == 0) {
				this.heal(1.0F);
			}
		}
		if (this.getAnimation() == ANIMATION_BITE && this.getAttackTarget() != null && this.getAnimationTick() == 6) {
			double dist = this.getDistanceSq(this.getAttackTarget());
			if (dist < 8) {
				this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
			}
		}
		if (this.getAnimation() == ANIMATION_SCRATCH && this.getAttackTarget() != null && this.getAnimationTick() == 6) {
			double dist = this.getDistanceSq(this.getAttackTarget());
			if (dist < 8) {
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
				}}
		}
		if(!world.isRemote && this.onGround && this.getNavigator().noPath() && this.getAttackTarget() != null && this.getAttackTarget().posY - 3 > this.posY && this.getRNG().nextInt(15) == 0 && this.canMove() && !this.isHovering() && !this.isFlying()){
			this.setHovering(true);
			this.hoverTicks = 0;
			this.flyTicks = 0;
		}
		IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(this);
		if (capability != null && capability.isStoned()) {
			this.setFlying(false);
			this.setHovering(false);
		}
		if (!this.onGround) {
			airBorneCounter++;
		} else {
			airBorneCounter = 0;
		}
		if (hasChestVarChanged && hippogryphInventory != null && !this.isChested()) {
			for (int i = 3; i < 18; i++) {
				if (!hippogryphInventory.getStackInSlot(i).isEmpty()) {
					if (!world.isRemote) {
						this.entityDropItem(hippogryphInventory.getStackInSlot(i), 1);
					}
					hippogryphInventory.removeStackFromSlot(i);
				}
			}
			hasChestVarChanged = false;
		}
		if (!this.onGround && this.airTarget != null) {
			this.setFlying(true);
		}
		if (this.isFlying() && this.ticksExisted % 40 == 0 || this.isFlying() && this.isSitting()) {
			this.setFlying(true);
		}
		if (!this.canMove() && this.getAttackTarget() != null) {
			this.setAttackTarget(null);
		}
		if (!this.canMove()) {
			this.getNavigator().clearPath();

		}
		if (this.getControllingPassenger() != null) {
			if (motionY > 0.5 && (this.isFlying() || this.isHovering())) {
				this.motionY = 0.5;
			}
			if (motionY < -0.5) {
				this.motionY = -0.5;
			}
		} else {
			if (motionY > 0.8) {
				this.motionY = 0.5;
			}
			if (motionY < -0.8) {
				this.motionY = -0.8;
			}
		}
		AnimationHandler.INSTANCE.updateAnimations(this);

		boolean sitting = isSitting() && !isHovering() && !isFlying();
		if (sitting && sitProgress < 20.0F) {
			sitProgress += 0.5F;
		} else if (!sitting && sitProgress > 0.0F) {
			sitProgress -= 0.5F;
		}

		boolean hovering = isHovering();
		if (hovering && hoverProgress < 20.0F) {
			hoverProgress += 0.5F;
		} else if (!hovering && hoverProgress > 0.0F) {
			hoverProgress -= 0.5F;
		}
		boolean flying = this.isFlying() || !this.isHovering() && airBorneCounter > 50;
		if (flying && flyProgress < 20.0F) {
			flyProgress += 0.5F;
		} else if (!flying && flyProgress > 0.0F) {
			flyProgress -= 0.5F;
		}
		if ((flying || hovering) && ticksExisted % 20 == 0 && !this.onGround) {
			this.playSound(SoundEvents.ENTITY_ENDERDRAGON_FLAP, this.getSoundVolume() * ((float)IceAndFireConfig.DRAGON_SETTINGS.dragonFlapNoiseDistance / 2F), 0.6F + this.rand.nextFloat() * 0.6F * this.getSoundPitch());
		}
		if (this.onGround && this.doesWantToLand() && (this.isFlying() || this.isHovering())) {
			this.setFlying(false);
			this.setHovering(false);
		}
		if (this.getControllingPassenger() != null && !this.onGround && (this.isFlying() || this.isHovering())) {
			this.motionY *= 0D;
		}
		if (this.isHovering()) {
			if (this.isSitting()) {
				this.setHovering(false);
			}
			this.hoverTicks++;
			if (this.doesWantToLand()) {
				this.motionY -= 0.25D;
			} else {
				if (this.getControllingPassenger() == null) {
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
		if (this.isSitting()) {
			this.getNavigator().clearPath();
		}
		if (!this.isFlying() && !this.isHovering() && this.airTarget != null && this.onGround) {
			this.airTarget = null;
		}
		if (this.isFlying() && this.airTarget == null && this.onGround && this.getControllingPassenger() == null) {
			this.setFlying(false);
		}

		if (this.isFlying() && getAttackTarget() == null) {
			flyAround();
		} else if (getAttackTarget() != null) {
			flyTowardsTarget();
		}
		if (this.onGround && flyTicks != 0) {
			flyTicks = 0;
		}
		if (this.isFlying() && this.doesWantToLand() && this.getControllingPassenger() == null) {
			this.setFlying(false);
			this.setHovering(false);
			if (this.onGround) {
				flyTicks = 0;
				this.setFlying(false);
			}
		}
		if (this.isFlying()) {
			this.flyTicks++;
		}
		if ((this.isHovering() || this.isFlying()) && this.isSitting()) {
			this.setFlying(false);
			this.setHovering(false);
		}
		if ((capability == null || !capability.isStoned()) && (!world.isRemote && this.getRNG().nextInt(FLIGHT_CHANCE_PER_TICK) == 0 && !this.isSitting() && !this.isFlying() && this.getPassengers().isEmpty() && !this.isChild() && !this.isHovering() && !this.isSitting() && this.canMove() && this.onGround || this.posY < -1)) {
			this.setHovering(true);
			this.hoverTicks = 0;
			this.flyTicks = 0;
		}
		if (getAttackTarget() != null && !this.getPassengers().isEmpty() && this.getOwner() != null && this.getPassengers().contains(this.getOwner())) {
			this.setAttackTarget(null);
		}
	}

	public boolean doesWantToLand() {
		return this.flyTicks > 2000 || down() || flyTicks > 40 && this.flyProgress == 0;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (world.isRemote) {
			this.updateClientControls();
		}
		if (this.up()) {
			if (this.airBorneCounter == 0) {
				this.motionY += 1;
			}
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
		if(this.down() && (this.isFlying() || this.isHovering())){
			this.motionY -= 0.4D;
		}
		if (!this.dismount() && (this.isFlying() || this.isHovering())) {
			this.motionY += 0.01D;
		}

		if (this.attack() && this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer) {

			EntityLivingBase target = DragonUtils.riderLookingAtEntity(this, (EntityPlayer) this.getControllingPassenger(), 3);
			if (this.getAnimation() != this.ANIMATION_BITE && this.getAnimation() != this.ANIMATION_SCRATCH) {
				this.setAnimation(this.getRNG().nextBoolean() ? this.ANIMATION_SCRATCH : this.ANIMATION_BITE);
			}
			if (target != null) {
				target.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
			}
		}
		if (this.getControllingPassenger() != null && this.getControllingPassenger().isSneaking()) {
			this.getControllingPassenger().dismountRidingEntity();
		}
		if (this.isFlying() && !this.isHovering() && this.getControllingPassenger() != null && !this.onGround && Math.max(Math.abs(motionZ), Math.abs(motionX)) < 0.1F) {
			this.setHovering(true);
			this.setFlying(false);
		}
		if (this.isHovering() && !this.isFlying() && this.getControllingPassenger() != null && !this.onGround && Math.max(Math.abs(motionZ), Math.abs(motionX)) > 0.1F) {
			this.setFlying(true);
			this.setHovering(false);
		}
		if (this.spacebarTicks > 0) {
			this.spacebarTicks--;
		}
		if (this.spacebarTicks > 20 && this.getOwner() != null && this.getPassengers().contains(this.getOwner()) && !this.isFlying() && !this.isHovering()) {
			this.setHovering(true);
		}
		if (world.isRemote) {
			roll_buffer.calculateChainFlapBuffer(35, 8, 6, this);
		}
		if (this.getAttackTarget() != null && this.getRidingEntity() == null && this.getAttackTarget().isDead || this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityDragonBase && ((EntityDragonBase) this.getAttackTarget()).isDead) {
			this.setAttackTarget(null);
		}
	}

	public void flyAround() {
		if (airTarget != null && this.isFlying()) {
			if (!isTargetInAir() || flyTicks > 6000 || !this.isFlying()) {
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
				if (!world.isAirBlock(pos)) {
					return true;
				}
			}
		}
		return false;
	}

	public void flyTowardsTarget() {
		if (airTarget != null && isTargetInAir() && this.isFlying() && this.getDistanceSquared(new Vec3d(airTarget.getX(), this.posY, airTarget.getZ())) > 3) {
			double targetX = airTarget.getX() + 0.5D - posX;
			double targetY = Math.min(airTarget.getY(), 256) + 1D - posY;
			double targetZ = airTarget.getZ() + 0.5D - posZ;
			motionX += (Math.signum(targetX) * 0.5D - motionX) * 0.100000000372529 * 2;
			motionY += (Math.signum(targetY) * 0.5D - motionY) * 0.100000000372529 * 2;
			motionZ += (Math.signum(targetZ) * 0.5D - motionZ) * 0.100000000372529 * 2;
			float angle = (float) (Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
			float rotation = MathHelper.wrapDegrees(angle - rotationYaw);
			moveForward = 0.5F;
			prevRotationYaw = rotationYaw;
			rotationYaw += rotation;
			if (!this.isFlying()) {
				this.setFlying(true);
			}
		} else {
			this.airTarget = null;
		}
		if (airTarget != null && this.isFlying() && this.doesWantToLand()) {
			this.setFlying(false);
			this.setHovering(false);
		}
	}

	protected boolean isTargetInAir() {
		return airTarget != null && ((world.getBlockState(airTarget).getMaterial() == Material.AIR) || world.getBlockState(airTarget).getMaterial() == Material.AIR);
	}

	public float getDistanceSquared(Vec3d vec3d) {
		float f = (float) (this.posX - vec3d.x);
		float f1 = (float) (this.posY - vec3d.y);
		float f2 = (float) (this.posZ - vec3d.z);
		return f * f + f1 * f1 + f2 * f2;
	}

	public boolean replaceItemInInventory(int inventorySlot, @Nullable ItemStack itemStackIn) {
		int j = inventorySlot - 500 + 2;
		if (j >= 0 && j < this.hippogryphInventory.getSizeInventory()) {
			this.hippogryphInventory.setInventorySlotContents(j, itemStackIn);
			return true;
		} else {
			return false;
		}
	}

	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (hippogryphInventory != null && !this.world.isRemote) {
			IEntityEffectCapability cap = InFCapabilities.getEntityEffectCapability(this);
			if(cap != null && cap.isStoned()) return;
			for (int i = 0; i < hippogryphInventory.getSizeInventory(); ++i) {
				ItemStack itemstack = hippogryphInventory.getStackInSlot(i);
				if (!itemstack.isEmpty()) {
					this.entityDropItem(itemstack, 0.0F);
				}
			}
		}
	}

	public void refreshInventory() {
		ItemStack saddle = this.hippogryphInventory.getStackInSlot(0);
		ItemStack chest = this.hippogryphInventory.getStackInSlot(1);
		this.setSaddled(saddle.getItem() == Items.SADDLE && !saddle.isEmpty());
		this.setChested(chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty());
		this.setArmor(getIntFromArmor(this.hippogryphInventory.getStackInSlot(2)));
		if (this.world.isRemote) {
			IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
			IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
			IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(this.hippogryphInventory.getStackInSlot(2))));
		}
	}

	@Override
	public void onHearFlute(EntityPlayer player) {
		if (this.isTamed() && this.isOwner(player)) {
			if (this.isFlying() || this.isHovering()) {
				this.airTarget = null;
				this.setFlying(false);
				this.setHovering(false);
			}
		}
	}

	@Nullable
	protected ResourceLocation getLootTable() {
		return LOOT;
	}

	public static class HippogryphInventory extends ContainerHorseChest {

		public HippogryphInventory(String inventoryTitle, int slotCount, EntityHippogryph hippogryph) {
			super(inventoryTitle, slotCount);
			this.addInventoryChangeListener(new HippogryphInventoryListener(hippogryph));
		}


	}

	static class HippogryphInventoryListener implements IInventoryChangedListener {

		EntityHippogryph hippogryph;

		public HippogryphInventoryListener(EntityHippogryph hippogryph) {
			this.hippogryph = hippogryph;
		}

		@Override
		public void onInventoryChanged(IInventory invBasic) {
			hippogryph.refreshInventory();
		}
	}

	@Override
	public boolean shouldAnimalsFear(Entity entity) {
		return DragonUtils.canTameDragonAttack(this, entity);
	}

	public void dropArmor(){
		IEntityEffectCapability cap = InFCapabilities.getEntityEffectCapability(this);
		if(cap != null && cap.isStoned()) return;
		if (hippogryphInventory != null && !this.world.isRemote) {
			for (int i = 0; i < hippogryphInventory.getSizeInventory(); ++i) {
				ItemStack itemstack = hippogryphInventory.getStackInSlot(i);
				if (!itemstack.isEmpty()) {
					this.entityDropItem(itemstack, 0.0F);
				}
			}
		}
	}

}
