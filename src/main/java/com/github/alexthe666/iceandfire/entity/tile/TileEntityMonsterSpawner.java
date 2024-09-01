package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.enums.EnumParticle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityMonsterSpawner extends TileEntitySpawnerBase {
    private final SpawnerBaseLogic spawnerLogic = new SpawnerBaseLogic() {
        @Override
        public void broadcastEvent(int id) {
            TileEntityMonsterSpawner.this.world.addBlockEvent(TileEntityMonsterSpawner.this.pos, ModBlocks.monster_spawner, id, 0);
        }

        @Override
        public World getSpawnerWorld() {
            return TileEntityMonsterSpawner.this.world;
        }

        @Override
        public BlockPos getSpawnerPosition() {
            return TileEntityMonsterSpawner.this.pos;
        }

        @Override
        public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
            super.setNextSpawnData(p_184993_1_);
            if (this.getSpawnerWorld() != null) {
                IBlockState iblockstate = this.getSpawnerWorld().getBlockState(this.getSpawnerPosition());
                this.getSpawnerWorld().notifyBlockUpdate(TileEntityMonsterSpawner.this.pos, iblockstate, iblockstate, 4);
            }
        }

        @Override
        public EnumParticle getParticle() {
            if (this.getRequiredSpawnCount() > 0) {
                return EnumParticle.REDSTONE;
            }
            return EnumParticle.FLAME;
        }
    };

    @Override
    public SpawnerBaseLogic getSpawnerBaseLogic() {
        return this.spawnerLogic;
    }
}