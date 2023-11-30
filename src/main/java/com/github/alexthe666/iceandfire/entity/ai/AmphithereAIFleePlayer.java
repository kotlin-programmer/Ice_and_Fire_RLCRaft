package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.Vec3d;

public class AmphithereAIFleePlayer extends EntityAIBase {
    protected EntityAmphithere entity;
    private final double farSpeed;
    private final double nearSpeed;
    protected EntityPlayer closestLivingEntity;
    private final float avoidDistance;
    private Path path;

    public AmphithereAIFleePlayer(EntityAmphithere entityIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        this.entity = entityIn;
        this.avoidDistance = avoidDistanceIn;
        this.farSpeed = farSpeedIn;
        this.nearSpeed = nearSpeedIn;
        this.setMutexBits(1);
    }


    public boolean shouldExecute() {
        if (this.entity.isFlying() || this.entity.isTamed() || !this.entity.canMove()) {
            return false;
        }
        List<EntityPlayer> list = this.entity.world.getEntitiesWithinAABB(EntityPlayer.class, this.entity.getEntityBoundingBox().grow(this.avoidDistance, 6D, this.avoidDistance), EntitySelectors.CAN_AI_TARGET);
        if (list.isEmpty()) {
            return false;
        }
        this.closestLivingEntity = list.get(0);
        Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 20, 7, new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));
        if (vec3d == null) {
            return false;
        }
        if (this.closestLivingEntity.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < this.closestLivingEntity.getDistanceSq(this.entity)) {
            return false;
        }
        this.path = this.entity.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
        return this.path != null;
    }

    public boolean shouldContinueExecuting() {
        if (this.entity.isFlying() || this.entity.isTamed() || !this.entity.canMove()) {
            return false;
        }
        return !this.entity.getNavigator().noPath();
    }

    public void startExecuting() {
        this.entity.getNavigator().setPath(this.path, this.farSpeed);
    }

    public void resetTask() {
        this.closestLivingEntity = null;
    }

    public void updateTask() {
        if (this.entity.getDistanceSq(this.closestLivingEntity) < 49.0D) {
            this.entity.getNavigator().setSpeed(this.nearSpeed);
        } else {
            this.entity.getNavigator().setSpeed(this.farSpeed);
        }
    }
}