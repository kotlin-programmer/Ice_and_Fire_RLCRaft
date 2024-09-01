package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityMonsterSpawner;
import com.github.alexthe666.iceandfire.entity.tile.TileEntitySpawnerBase;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageUpdateSpawner extends AbstractMessage<MessageUpdateSpawner> {

	public long blockPos;
	public int requiredSpawnCount;

	public MessageUpdateSpawner(long blockPos, int requiredSpawnCount) {
		this.blockPos = blockPos;
		this.requiredSpawnCount = requiredSpawnCount;
	}

	public MessageUpdateSpawner() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		blockPos = buf.readLong();
		requiredSpawnCount = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(blockPos);
		buf.writeInt(requiredSpawnCount);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onClientReceived(Minecraft client, MessageUpdateSpawner message, EntityPlayer player, MessageContext messageContext) {
		BlockPos pos = BlockPos.fromLong(message.blockPos);
		TileEntity te = client.world.getTileEntity(pos);
		if (te instanceof TileEntitySpawnerBase) {
			TileEntitySpawnerBase spawner = (TileEntitySpawnerBase) te;
			spawner.setRequiredSpawnCount(requiredSpawnCount);
		}
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageUpdateSpawner message, EntityPlayer player, MessageContext messageContext) {

	}
}