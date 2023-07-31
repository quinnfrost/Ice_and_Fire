package com.github.alexthe666.iceandfire.entity.behavior.utils;

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
}
