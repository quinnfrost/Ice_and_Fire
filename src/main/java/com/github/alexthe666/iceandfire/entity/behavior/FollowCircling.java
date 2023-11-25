package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonFlightUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

public class FollowCircling<E extends TamableAnimal & IBehaviorApplicable> extends FlightFollowing<E>{
    public FollowCircling() {
        super();
    }

    @Override
    protected Vec3 getCenter(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (pEntity.isTame()) {
            LivingEntity owner = pEntity.getOwner();
            if (owner.level.dimension().equals(pEntity.level.dimension())) {
                return owner.position();
            }
        }
        return null;
    }

    @Override
    protected Vec3 getNextPosition(E pEntity, Vec3 center, float radius) {
        Vec3 vecToEscorter = pEntity.position().subtract(center);

        float nextPosDirectionXZ = (float) Math.PI / 2.0f;
        double distant = vecToEscorter.horizontalDistance();
        if (distant > radius) {
            nextPosDirectionXZ += (float) Math.PI / 8.0f;
        }
        nextPosDirectionXZ *= (CLOCKWISE ? -1 : 1);

        float nextPosDirectionY = 0;
        if (pEntity.getY() < this.getPreferredEscortFlightLevel(pEntity)) {
            nextPosDirectionY = (float) Math.PI / 4.0f;
        }

        Vec3 normalVec = vecToEscorter.normalize()
                .yRot(nextPosDirectionXZ)
                .xRot(nextPosDirectionY);
        Vec3 wantedPosition = pEntity.position().add(normalVec.scale(10)).with(Direction.Axis.Y, this.getPreferredEscortFlightLevel(pEntity));

        return wantedPosition;
    }

    protected float getPreferredEscortFlightLevel(E entity) {
        return (float) (entity.getBbHeight() * 5 + DragonFlightUtils.getGround(entity.level, entity.position()).y);
    }
}
