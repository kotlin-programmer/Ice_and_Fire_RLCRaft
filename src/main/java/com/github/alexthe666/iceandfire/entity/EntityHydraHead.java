package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.util.EntityMultipartPart;
import com.github.alexthe666.iceandfire.enums.EnumParticle;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityHydraHead extends EntityMultipartPart {
    public int headIndex;
    public EntityHydra hydra;
    private final boolean neck;

    public EntityHydraHead(EntityHydra entity, float radius, float angle, float y, float width, float height, float damageMulti, int headIndex, boolean neck) {
        super(entity, radius, angle, y, width, height, damageMulti);
        this.headIndex = headIndex;
        this.neck = neck;
        this.hydra = entity;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(hydra != null && hydra.getSeveredHead() != -1 && this.neck && !EntityGorgon.isStoneMob(hydra)){
            if(hydra.getSeveredHead() == headIndex){
                if (this.world.isRemote) {
                    for (int k = 0; k < 5; ++k) {
                        double d2 = 0.4;
                        double d0 = 0.1;
                        double d1 = 0.1;
                        IceAndFire.PROXY.spawnParticle(EnumParticle.BLOOD, this.world, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY - 0.5D, this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d2, d0, d1);
                    }
                }
            }
        }
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (this.parent instanceof EntityHydra) {
            ((EntityHydra) this.parent).triggerHeadFlags(headIndex);
        }
        return this.parent.attackEntityFrom(source, damage);
    }
}
