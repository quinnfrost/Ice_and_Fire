package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.util.IAFMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.Optional;

public class CustomMoveController {
    public static class BasicMoveControl extends MoveControl {
        Vec3 lastLookVec;
        int additionalOffsetX = 0;
        int additionalOffsetZ = 0;
        float stuckPenalty = 1;

        public BasicMoveControl(Mob pMob) {
            super(pMob);
        }

        @Override
        public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
            if (pX != this.wantedX || pY != this.wantedY || pZ != this.wantedZ) {
                this.additionalOffsetX = 0;
                this.additionalOffsetZ = 0;
                stuckPenalty = 0;
            }
            super.setWantedPosition(pX, pY, pZ, pSpeed);
        }

        @Override
        public void tick() {
            if (this.operation == Operation.STRAFE) {
                float f = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
                float f1 = (float)this.speedModifier * f;
                float f2 = this.strafeForwards;
                float f3 = this.strafeRight;
                float f4 = Mth.sqrt(f2 * f2 + f3 * f3);
                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }

                f4 = f1 / f4;
                f2 *= f4;
                f3 *= f4;
                float f5 = Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F));
                float f6 = Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F));
                float f7 = f2 * f6 - f3 * f5;
                float f8 = f3 * f6 + f2 * f5;
                if (!this.isWalkable(f7, f8)) {
                    this.strafeForwards = 1.0F;
                    this.strafeRight = 0.0F;
                }

                this.mob.setSpeed(f1);
                this.mob.setZza(this.strafeForwards);
                this.mob.setXxa(this.strafeRight);
                this.operation = Operation.WAIT;
            } else if (this.operation == Operation.MOVE_TO) {



                this.operation = Operation.WAIT;
                double d0 = this.wantedX - this.mob.getX();
                double d1 = this.wantedZ - this.mob.getZ();
                double d2 = this.wantedY - this.mob.getY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;
                if (d3 < (double)2.5000003E-7F) {
                    this.mob.setZza(0.0F);

                    this.additionalOffsetX = 0;
                    this.additionalOffsetZ = 0;
                    return;
                }

//                if (this.mob.horizontalCollision) {
//                    Vec3 diff = (new Vec3(wantedX, wantedY, wantedZ)).subtract(this.mob.getPosition(1.0f));
//
////                    Direction direction = Direction.getNearest(this.mob.getLookAngle().x(), this.mob.getLookAngle().y(), this.mob.getLookAngle().z());
////                    switch (direction) {
////                        // Z-
////                        case NORTH -> {
//////                        this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(Mth.sign(diff.x) * 0.1f, 0, 0));
//////                            this.mob.setXxa(-Mth.sign(diff.x) * 0.1f);
//////                            d1 *= stuckPenalty;
////                        }
////                        // Z+
////                        case SOUTH -> {
//////                        this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(Mth.sign(diff.x) * 0.1f, 0, 0));
//////                            this.mob.setXxa(-Mth.sign(diff.x) * 0.1f);
//////                            d1 *= stuckPenalty;
////                        }
////                        // X-
////                        case WEST -> {
//////                        this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0, Mth.sign(diff.z) * 0.1f));
//////                            this.mob.setXxa(-Mth.sign(diff.z) * 0.1f);
//////                            d0 *= stuckPenalty;
////                        }
////                        // X+
////                        case EAST -> {
//////                        this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0, Mth.sign(diff.z) * 0.1f));
//////                            this.mob.setXxa(-Mth.sign(diff.z) * 0.1f);
//////                            d0 *= stuckPenalty;
////                        }
////                        default -> {
////
////                        }
////                    }
//
//                    Mob ourMob = this.mob;
//
//                    stuckPenalty = 0;
//
//                    Optional<Direction> directionOptional = DragonBehaviorUtils.getCollisionDirection(this.mob, this.mob.getLookAngle());
//                    if (directionOptional.isPresent()) {
//                        Direction collisionDirection = directionOptional.get();
//                        switch (collisionDirection) {
//
//                            case DOWN -> {
//                            }
//                            case UP -> {
//                            }
//                            // Z-
//                            case NORTH -> {
//                                this.additionalOffsetZ = Mth.ceil(this.mob.getBbWidth());
//                            }
//                            // Z+
//                            case SOUTH -> {
//                                this.additionalOffsetZ = -Mth.ceil(this.mob.getBbWidth());
//                            }
//                            // X-
//                            case WEST -> {
//                                this.additionalOffsetX = Mth.ceil(this.mob.getBbWidth());
//                            }
//                            // X+
//                            case EAST -> {
//                                this.additionalOffsetX = -Mth.ceil(this.mob.getBbWidth());
//                            }
//                        }
//                    }
//                } else {
//                    stuckPenalty = Math.min(1.0f, stuckPenalty + 0.1f);
//                }
//                this.wantedX += this.additionalOffsetX;
//                this.wantedZ += this.additionalOffsetZ;




                float f9 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                BlockPos blockpos = this.mob.blockPosition();
                BlockState blockstate = this.mob.level.getBlockState(blockpos);
                VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
                if (d2 > (double)this.mob.getStepHeight() && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double)blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                    this.mob.getJumpControl().jump();
                    this.operation = Operation.JUMPING;
                }
            } else if (this.operation == Operation.JUMPING) {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.mob.isOnGround()) {
                    this.operation = Operation.WAIT;
                }
            } else {
                this.mob.setZza(0.0F);

                this.additionalOffsetX = 0;
                this.additionalOffsetZ = 0;
            }
        }

        private boolean isWalkable(float pRelativeX, float pRelativeZ) {
            PathNavigation pathnavigation = this.mob.getNavigation();
            if (pathnavigation != null) {
                NodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
                if (nodeevaluator != null && nodeevaluator.getBlockPathType(this.mob.level, Mth.floor(this.mob.getX() + (double)pRelativeX), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + (double)pRelativeZ)) != BlockPathTypes.WALKABLE) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class BasicHoverMoveControl extends MoveControl {
        public BasicHoverMoveControl(Mob hippogryph) {
            super(hippogryph);
            this.speedModifier = 1.75F;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
//                if (mob.horizontalCollision) {
//                    mob.setYRot(mob.getYRot() + 180.0F);
//                    BlockPos target = DragonUtils.getBlockInViewHippogryph(mob, 180);
//                    this.speedModifier = 0.1F;
//                    if (target != null) {
//                        this.wantedX = target.getX() + 0.5F;
//                        this.wantedY = target.getY() + 0.5F;
//                        this.wantedZ = target.getZ() + 0.5F;
//                    }
//                }
                double d0 = this.wantedX - mob.getX();
                double d1 = this.wantedY - mob.getY();
                double d2 = this.wantedZ - mob.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                d3 = Math.sqrt(d3);

                if (mob.getBoundingBox().contains(this.wantedX, this.wantedY, this.wantedZ) /* d3 < (double)2.5000003E-7F  mob.getBoundingBox().getSize() */) {
                    this.operation = MoveControl.Operation.WAIT;
                    mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
                } else {
                    mob.setDeltaMovement(mob.getDeltaMovement().add(d0 / d3 * 0.1D * this.speedModifier, d1 / d3 * 0.1D * this.speedModifier, d2 / d3 * 0.1D * this.speedModifier));

                    if (mob.getTarget() == null) {
                        mob.setYRot(-((float) Mth.atan2(mob.getDeltaMovement().x, mob.getDeltaMovement().z)) * (180F / (float) Math.PI));
                        mob.yBodyRot = mob.getYRot();
                    } else {
                        double d4 = mob.getTarget().getX() - mob.getX();
                        double d5 = mob.getTarget().getZ() - mob.getZ();
                        mob.setYRot(-((float) Mth.atan2(d4, d5)) * (180F / (float) Math.PI));
                        mob.yBodyRot = mob.getYRot();
                    }
                }
            }
        }
    }

    public static class GroundMoveHelper extends MoveControl {
        public GroundMoveHelper(Mob LivingEntityIn) {
            super(LivingEntityIn);
        }

        public float distance(float rotateAngleFrom, float rotateAngleTo) {
            return (float) IAFMath.atan2_accurate(Mth.sin(rotateAngleTo - rotateAngleFrom), Mth.cos(rotateAngleTo - rotateAngleFrom));
        }

        @Override
        public void tick() {
            if (this.operation == Operation.STRAFE) {
                float f = (float) this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
                float f1 = (float) this.speedModifier * f;
                float f2 = this.strafeForwards;
                float f3 = this.strafeRight;
                float f4 = Mth.sqrt(f2 * f2 + f3 * f3);

                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }

                f4 = f1 / f4;
                f2 = f2 * f4;
                f3 = f3 * f4;
                float f5 = Mth.sin(this.mob.getYRot() * 0.017453292F);
                float f6 = Mth.cos(this.mob.getYRot() * 0.017453292F);
                float f7 = f2 * f6 - f3 * f5;
                float f8 = f3 * f6 + f2 * f5;
                PathNavigation pathnavigate = this.mob.getNavigation();
                if (pathnavigate != null) {
                    NodeEvaluator nodeprocessor = pathnavigate.getNodeEvaluator();
                    if (nodeprocessor != null && nodeprocessor.getBlockPathType(this.mob.level, Mth.floor(this.mob.getX() + (double) f7), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() + (double) f8)) != BlockPathTypes.WALKABLE) {
                        this.strafeForwards = 1.0F;
                        this.strafeRight = 0.0F;
                        f1 = f;
                    }
                }
                this.mob.setSpeed(f1);
                this.mob.setZza(this.strafeForwards);
                this.mob.setXxa(this.strafeRight);
                this.operation = Operation.WAIT;
            } else if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                EntityDragonBase dragonBase = (EntityDragonBase) mob;
                double d0 = this.getWantedX() - this.mob.getX();
                double d1 = this.getWantedZ() - this.mob.getZ();
                double d2 = this.getWantedY() - this.mob.getY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;

                if (d3 < 2.500000277905201E-7D) {
                    this.mob.setZza(0.0F);
                    return;
                }
                float targetDegree = (float) (Mth.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
                float changeRange = 70F;
                if (Math.ceil(dragonBase.getBbWidth()) > 2F) {
                    float ageMod = 1F - Math.min(dragonBase.getAgeInDays(), 125) / 125F;
                    changeRange = 5 + ageMod * 10;
                }
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetDegree, changeRange));
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (d2 > (double) this.mob.maxUpStep && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getBbWidth() / 2)) {
                    this.mob.getJumpControl().jump();
                    this.operation = Operation.JUMPING;
                }
            } else if (this.operation == Operation.JUMPING) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));

                if (this.mob.isOnGround()) {
                    this.operation = Operation.WAIT;
                }
            } else {
                this.mob.setZza(0.0F);
            }
        }

    }


    public static class FlightMoveHelper extends MoveControl {

        private EntityDragonBase dragon;

        /*
        Detouring state
        0: no detour
        1: climbing
        2: flying over the terrain
         */
        private int detourState;
        private Vec3 detourTarget;

        public FlightMoveHelper(EntityDragonBase dragonBase) {
            super(dragonBase);
            this.dragon = dragonBase;

            detourState = 0;
            detourTarget = null;
        }

        public void tick() {
            if (dragon.flightManager.getFlightTarget() == null) {
                return;
            }

//            ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl());
//            EnumCommandSettingType.CommandStatus commandStatus = cap.getCommandStatus();
//            IafAdvancedDragonFlightManager dragonFlightManager = (IafAdvancedDragonFlightManager) dragon.flightManager;
//            Vec3 flightTarget = dragonFlightManager.getFlightTarget();

            if (dragon.horizontalCollision) {
                if (dragon.position().y < IafConfig.maxDragonFlight) {
                    dragon.setDeltaMovement(new Vec3(dragon.getDeltaMovement().scale(-0.5d).x, 0.2, dragon.getDeltaMovement().scale(-0.5d).y));
                    return;
                }
            }
            // Todo: 用AbstractPathJob#isPassableBB代替
            if (dragon.verticalCollision && !DragonFlightUtils.isAreaPassable(
                    dragon.level,
                    dragon.blockPosition().above((int) Math.ceil(dragon.getBoundingBox().getYsize())),
                    (int) dragon.getBoundingBox().getSize())) {
                dragon.setDeltaMovement(new Vec3(dragon.getDeltaMovement().scale(1d).x, -0.2, dragon.getDeltaMovement().scale(1d).y));
            }

//            float distToX = (float) (flightTarget.x - dragon.getX());
//            float distToY = (float) (flightTarget.y - dragon.getY());
//            float distToZ = (float) (flightTarget.z - dragon.getZ());
            float distToX = (float) (this.getWantedX() - dragon.getX());
            float distToY = (float) (this.getWantedY() - dragon.getY());
            float distToZ = (float) (this.getWantedZ() - dragon.getZ());

            // Following logic makes dragon actually fly to the target, it's not touched except the name
            // The shortest possible distance to the target plane (parallel to y)
            double xzPlaneDist = Mth.sqrt(distToX * distToX + distToZ * distToZ);
            // f = 1 - |0.7 * Y| / sqrt(X^2+Y^2)
            double yDistMod = 1.0D - (double) Mth.abs(distToY * 0.7F) / xzPlaneDist;
            distToX = (float) ((double) distToX * yDistMod);
            distToZ = (float) ((double) distToZ * yDistMod);
            xzPlaneDist = Mth.sqrt(distToX * distToX + distToZ * distToZ);
            double distToTarget = Mth.sqrt(distToX * distToX + distToZ * distToZ + distToY * distToY);
            if (distToTarget > 1.0F) {
                float oldYaw = dragon.getYRot();
                // Theta = atan2(y,x) - the angle of (x,y)
                float targetYaw = (float) Mth.atan2(distToZ, distToX);
                float currentYawTurn = Mth.wrapDegrees(dragon.getYRot() + 90);
                // Radian to degree
                float targetYawDegrees = Mth.wrapDegrees(targetYaw * 57.295776F);
                dragon.setYRot(IafDragonFlightManager.approachDegrees(currentYawTurn, targetYawDegrees, dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getTarget() != null ? 10 : 4.0F) - 90.0F);
                dragon.yBodyRot = dragon.getYRot();
                if (IafDragonFlightManager.degreesDifferenceAbs(oldYaw, dragon.getYRot()) < 3.0F) {
                    speedModifier = IafDragonFlightManager.approach((float) speedModifier, 1.8F, 0.005F * (1.8F / (float) speedModifier));
                } else {
                    speedModifier = IafDragonFlightManager.approach((float) speedModifier, 0.2F, 0.025F);
                    if (distToTarget < 100D && dragon.getTarget() != null) {
                        speedModifier = speedModifier * (distToTarget / 100D);
                    }
                }
                float finPitch = (float) (-(Mth.atan2(-distToY, xzPlaneDist) * 57.2957763671875D));
                dragon.setXRot(finPitch);
                float yawTurnHead = dragon.getYRot() + 90.0F;
                speedModifier *= dragon.getFlightSpeedModifier();

                if (dragon.getCommand() == 2) {
                    speedModifier *= 1.5;
                }

//                speedModifier *= dragonFlightManager.getFlightPhase() == IafAdvancedDragonFlightManager.FlightPhase.DIRECT
//                        ? Math.min(1, distToTarget / 50 + 0.3)  //Make the dragon fly slower when close to target
//                        : 1;    // Do not limit speed when detouring
                speedModifier *= Math.min(1, distToTarget / 50 + 0.3);
                double lvt_16_1_ = speedModifier * Mth.cos(yawTurnHead * 0.017453292F) * Math.abs((double) distToX / distToTarget);
                double lvt_18_1_ = speedModifier * Mth.sin(yawTurnHead * 0.017453292F) * Math.abs((double) distToZ / distToTarget);
                double lvt_20_1_ = speedModifier * Mth.sin(finPitch * 0.017453292F) * Math.abs((double) distToY / distToTarget);
                double motionCap = 0.2D;
                dragon.setDeltaMovement(dragon.getDeltaMovement().add(Math.min(lvt_16_1_ * 0.2D, motionCap), Math.min(lvt_20_1_ * 0.2D, motionCap), Math.min(lvt_18_1_ * 0.2D, motionCap)));
            }

        }
    }

}
