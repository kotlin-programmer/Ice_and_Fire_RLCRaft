package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.enums.EnumParticle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityDreadSpawner extends TileEntitySpawnerBase {
    private final SpawnerBaseLogic spawnerLogic = new SpawnerBaseLogic() {

        @Override
        public void broadcastEvent(int id) {
            TileEntityDreadSpawner.this.world.addBlockEvent(TileEntityDreadSpawner.this.pos, ModBlocks.dread_spawner, id, 0);
        }

        @Override
        public World getSpawnerWorld() {
            return TileEntityDreadSpawner.this.world;
        }

        @Override
        public BlockPos getSpawnerPosition() {
            return TileEntityDreadSpawner.this.pos;
        }

        @Override
        public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
            super.setNextSpawnData(p_184993_1_);
            if (this.getSpawnerWorld() != null) {
                IBlockState iblockstate = this.getSpawnerWorld().getBlockState(this.getSpawnerPosition());
                this.getSpawnerWorld().notifyBlockUpdate(TileEntityDreadSpawner.this.pos, iblockstate, iblockstate, 4);
            }
        }

        @Override
        public EnumParticle getParticle() {
            if (this.getRequiredSpawnCount() > 0) {
                return EnumParticle.REDSTONE;
            }
            return EnumParticle.DREAD_TORCH;
        }
    };

    @Override
    public SpawnerBaseLogic getSpawnerBaseLogic() {
        return this.spawnerLogic;
    }
}