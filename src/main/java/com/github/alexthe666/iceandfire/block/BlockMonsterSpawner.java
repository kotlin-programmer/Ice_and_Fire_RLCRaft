package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityMonsterSpawner;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

public class BlockMonsterSpawner extends BlockMobSpawner {

    public BlockMonsterSpawner() {
        super();
        this.setHardness(9.0F);
        this.setResistance(10000F);
        this.setTranslationKey("iceandfire.monster_spawner");
        this.setSoundType(SoundType.METAL);
        this.setRegistryName(IceAndFire.MODID, "monster_spawner");
        GameRegistry.registerTileEntity(TileEntityMonsterSpawner.class, "monster_spawner");
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMonsterSpawner();
    }
}
