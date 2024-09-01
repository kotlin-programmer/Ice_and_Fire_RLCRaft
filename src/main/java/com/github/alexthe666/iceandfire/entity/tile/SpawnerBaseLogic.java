package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.enums.EnumParticle;
import com.github.alexthe666.iceandfire.message.MessageUpdateSpawner;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public abstract class SpawnerBaseLogic extends MobSpawnerBaseLogic {

    private final List<WeightedSpawnerEntity> potentialSpawns = Lists.newArrayList();
    private int spawnDelay = 20;
    private WeightedSpawnerEntity spawnData = new WeightedSpawnerEntity();
    private double mobRotation;
    private double prevMobRotation;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    public Entity cachedEntity;
    private int maxNearbyEntities = 6;
    private int activatingRangeFromPlayer = 16;
    private int spawnRange = 4;
    private int requiredSpawnCount = 0;
    private boolean dirty = false;

    @Nullable
    private ResourceLocation getEntityId() {
        String s = this.spawnData.getNbt().getString("id");
        return StringUtils.isNullOrEmpty(s) ? null : new ResourceLocation(s);
    }

    @Override
    public void setEntityId(@Nullable ResourceLocation id) {
        if (id != null) {
            this.spawnData.getNbt().setString("id", id.toString());
        }
    }

    /**
     * Returns true if there's a player close enough to this mob spawner to activate it.
     */
    private boolean isActivated() {
        BlockPos blockpos = this.getSpawnerPosition();
        return this.getSpawnerWorld().isAnyPlayerWithinRangeAt((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.5D, (double) blockpos.getZ() + 0.5D, this.activatingRangeFromPlayer);
    }

    @Override
    public void updateSpawner() {
        if (!this.isActivated()) {
            this.prevMobRotation = this.mobRotation;
        } else {
            BlockPos blockPos = this.getSpawnerPosition();

            if (this.getSpawnerWorld().isRemote) {
                double d3 = (float) blockPos.getX() + this.getSpawnerWorld().rand.nextFloat();
                double d4 = (float) blockPos.getY() + this.getSpawnerWorld().rand.nextFloat();
                double d5 = (float) blockPos.getZ() + this.getSpawnerWorld().rand.nextFloat();
                this.getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D);
                IceAndFire.PROXY.spawnParticle(getParticle(), this.getSpawnerWorld(), d3, d4, d5, 0.0D, 0.0D, 0.0D);
                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }

                this.prevMobRotation = this.mobRotation;
                this.mobRotation = (this.mobRotation + (double) (1000.0F / ((float) this.spawnDelay + 200.0F))) % 360.0D;
            } else {
                if (this.spawnDelay == -1) {
                    this.resetTimer();
                }

                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                    return;
                }

                boolean flag = false;

                for (int i = 0; i < this.spawnCount; ++i) {
                    NBTTagCompound tagCompound = this.spawnData.getNbt();
                    NBTTagList tagList = tagCompound.getTagList("Pos", 6);
                    World world = this.getSpawnerWorld();
                    int j = tagList.tagCount();
                    double d0 = j >= 1 ? tagList.getDoubleAt(0) : (double) blockPos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) this.spawnRange + 0.5D;
                    double d1 = j >= 2 ? tagList.getDoubleAt(1) : (double) (blockPos.getY() + world.rand.nextInt(3) - 1);
                    double d2 = j >= 3 ? tagList.getDoubleAt(2) : (double) blockPos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) this.spawnRange + 0.5D;
                    Entity entity = AnvilChunkLoader.readWorldEntityPos(tagCompound, world, d0, d1, d2, false);

                    if (entity == null) {
                        return;
                    }

                    int k = world.getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)).grow(this.spawnRange)).size();

                    if (k >= this.maxNearbyEntities) {
                        this.resetTimer();
                        return;
                    }

                    EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving) entity : null;
                    entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, world.rand.nextFloat() * 360.0F, 0.0F);

                    if (entityliving == null || net.minecraftforge.event.ForgeEventFactory.canEntitySpawnSpawner(entityliving, getSpawnerWorld(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, this)) {
                        if (this.spawnData.getNbt().getSize() == 1 && this.spawnData.getNbt().hasKey("id", 8) && entity instanceof EntityLiving) {
                            if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityliving, this.getSpawnerWorld(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, this))
                                ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), null);
                        }

                        if (this.requiredSpawnCount > 0) {
                            this.requiredSpawnCount--;

                            if (this.requiredSpawnCount == 0) {
                                IceAndFire.NETWORK_WRAPPER.sendToAll(new MessageUpdateSpawner(blockPos.toLong(), this.requiredSpawnCount));
                            }
                        }

                        AnvilChunkLoader.spawnEntity(entity, world);
                        world.playEvent(2004, blockPos, 0);

                        if (entityliving != null) {
                            entityliving.spawnExplosionParticle();
                        }

                        flag = true;
                    }
                }

                if (flag) {
                    this.resetTimer();
                }
            }
        }
    }

    private void resetTimer() {
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            int i = this.maxSpawnDelay - this.minSpawnDelay;
            this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(i);
        }

        if (!this.potentialSpawns.isEmpty()) {
            this.setNextSpawnData(WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.potentialSpawns));
        }

        this.broadcastEvent(1);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.spawnDelay = nbt.getShort("Delay");
        this.requiredSpawnCount = nbt.getShort("RequiredSpawnCount");
        this.potentialSpawns.clear();

        if (nbt.hasKey("SpawnPotentials", 9)) {
            NBTTagList nbttaglist = nbt.getTagList("SpawnPotentials", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                this.potentialSpawns.add(new WeightedSpawnerEntity(nbttaglist.getCompoundTagAt(i)));
            }
        }

        if (nbt.hasKey("SpawnData", 10)) {
            this.setNextSpawnData(new WeightedSpawnerEntity(1, nbt.getCompoundTag("SpawnData")));
        } else if (!this.potentialSpawns.isEmpty()) {
            this.setNextSpawnData(WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.potentialSpawns));
        }

        if (nbt.hasKey("MinSpawnDelay", 99)) {
            this.minSpawnDelay = nbt.getShort("MinSpawnDelay");
            this.maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
            this.spawnCount = nbt.getShort("SpawnCount");
        }

        if (nbt.hasKey("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
            this.activatingRangeFromPlayer = nbt.getShort("RequiredPlayerRange");
        }

        if (nbt.hasKey("SpawnRange", 99)) {
            this.spawnRange = nbt.getShort("SpawnRange");
        }

        if (this.getSpawnerWorld() != null) {
            this.cachedEntity = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound p_189530_1_) {
        ResourceLocation resourcelocation = this.getEntityId();

        if (resourcelocation == null) {
            return p_189530_1_;
        } else {
            p_189530_1_.setShort("Delay", (short) this.spawnDelay);
            p_189530_1_.setShort("RequiredSpawnCount", (short) this.requiredSpawnCount);
            p_189530_1_.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
            p_189530_1_.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
            p_189530_1_.setShort("SpawnCount", (short) this.spawnCount);
            p_189530_1_.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
            p_189530_1_.setShort("RequiredPlayerRange", (short) this.activatingRangeFromPlayer);
            p_189530_1_.setShort("SpawnRange", (short) this.spawnRange);
            p_189530_1_.setTag("SpawnData", this.spawnData.getNbt().copy());
            NBTTagList nbttaglist = new NBTTagList();

            if (this.potentialSpawns.isEmpty()) {
                nbttaglist.appendTag(this.spawnData.toCompoundTag());
            } else {
                for (WeightedSpawnerEntity weightedspawnerentity : this.potentialSpawns) {
                    nbttaglist.appendTag(weightedspawnerentity.toCompoundTag());
                }
            }

            p_189530_1_.setTag("SpawnPotentials", nbttaglist);
            return p_189530_1_;
        }
    }

    /**
     * Sets the delay to minDelay if parameter given is 1, else return false.
     */
    @Override
    public boolean setDelayToMin(int id) {
        if (id == 1 && this.getSpawnerWorld().isRemote) {
            this.spawnDelay = this.minSpawnDelay;
            return true;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Entity getCachedEntity() {
        if (this.cachedEntity == null || dirty) {
            this.cachedEntity = AnvilChunkLoader.readWorldEntity(this.spawnData.getNbt(), this.getSpawnerWorld(), false);

            if (this.spawnData.getNbt().getSize() == 1 && this.spawnData.getNbt().hasKey("id", 8) && this.cachedEntity instanceof EntityLiving) {
                ((EntityLiving) this.cachedEntity).onInitialSpawn(this.getSpawnerWorld().getDifficultyForLocation(new BlockPos(this.cachedEntity)), null);
            }

            if (this.cachedEntity instanceof EntityDragonBase) {
                EntityDragonBase dragon = (EntityDragonBase) this.cachedEntity;
                dragon.setAgeInDays(90);
                dragon.setAgingDisabled(true);
                dragon.setScaleForAge(false);
                dragon.setHealth(dragon.getMaxHealth());
            } else if (this.cachedEntity instanceof EntityMyrmexBase) {
                EntityMyrmexBase myrmex = (EntityMyrmexBase) this.cachedEntity;
                myrmex.setGrowthStage(2);
                myrmex.setScaleForAge(false);
            } else if (this.cachedEntity instanceof EntitySeaSerpent) {
                EntitySeaSerpent seaSerpent = (EntitySeaSerpent) this.cachedEntity;
                seaSerpent.setSeaSerpentScale(1.5F);
                seaSerpent.setScaleForAge(false);
            }

            this.dirty = false;
        }

        return this.cachedEntity;
    }

    public void setDirty() {
        dirty = true;
    }

    public int getRequiredSpawnCount() {
        return this.requiredSpawnCount;
    }

    public void setRequiredSpawnCount(int count) {
        this.requiredSpawnCount = count;
    }

    @Override
    public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
        this.spawnData = p_184993_1_;
    }

    @Override
    public abstract void broadcastEvent(int id);

    @Override
    public abstract World getSpawnerWorld();

    @Override
    public abstract BlockPos getSpawnerPosition();

    public abstract EnumParticle getParticle();

    @SideOnly(Side.CLIENT)
    public double getMobRotation() {
        return this.mobRotation;
    }

    @SideOnly(Side.CLIENT)
    public double getPrevMobRotation() {
        return this.prevMobRotation;
    }
}
