package com.github.alexthe666.iceandfire.entity.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;

import javax.annotation.Nullable;

public abstract class TileEntitySpawnerBase extends TileEntityMobSpawner {

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.getSpawnerBaseLogic().readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.getSpawnerBaseLogic().writeToNBT(compound);
        return compound;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    @Override
    public void update() {
        this.getSpawnerBaseLogic().updateSpawner();
    }

    /**
     * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
     * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
     */
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbttagcompound = this.writeToNBT(new NBTTagCompound());
        nbttagcompound.removeTag("SpawnPotentials");
        return nbttagcompound;
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        return this.getSpawnerBaseLogic().setDelayToMin(id) || super.receiveClientEvent(id, type);
    }

    @Override
    public boolean onlyOpsCanSetNbt() {
        return true;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.getSpawnerBaseLogic().setDirty();
    }

    public int getRequiredSpawnCount() {
        return this.getSpawnerBaseLogic().getRequiredSpawnCount();
    }

    public void setRequiredSpawnCount(int count) {
        this.getSpawnerBaseLogic().setRequiredSpawnCount(count);
    }

    public abstract SpawnerBaseLogic getSpawnerBaseLogic();
}
