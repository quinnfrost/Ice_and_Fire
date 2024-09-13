package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.alexthe666.iceandfire.util.IAFMath;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;

public class MoveControlFrostDragon {
    public static class GroundMoveControl extends MoveControl {
        public GroundMoveControl(Mob LivingEntityIn) {
            super(LivingEntityIn);
        }

        public float distance(float rotateAngleFrom, float rotateAngleTo) {
            return (float) IAFMath.atan2_accurate(Mth.sin(rotateAngleTo - rotateAngleFrom),
                                                  Mth.cos(rotateAngleTo - rotateAngleFrom)
            );
        }

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
                    if (nodeprocessor != null && nodeprocessor.getBlockPathType(this.mob.level,
                                                                                Mth.floor(this.mob.getX() + (double) f7),
                                                                                Mth.floor(this.mob.getY()),
                                                                                Mth.floor(this.mob.getZ() + (double) f8)
                    ) != BlockPathTypes.WALKABLE) {
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
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));
                if (d2 > (double) this.mob.maxUpStep && d0 * d0 + d1 * d1 < (double) Math.max(1.0F,
                                                                                              this.mob.getBbWidth() / 2
                )) {
                    this.mob.getJumpControl().jump();
                    this.operation = Operation.JUMPING;
                }
            } else if (this.operation == Operation.JUMPING) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));

                if (this.mob.isOnGround()) {
                    this.operation = Operation.WAIT;
                }
            } else {
                this.mob.setZza(0.0F);
            }
        }

    }

    // Fixme: sudden downward on hover
    public static class HoverMoveControl extends MoveControl {
        private final EntityDragonBase dragon;

        public HoverMoveControl(EntityDragonBase dragonBase) {
            super(dragonBase);
            this.dragon = dragonBase;
        }

        @Override
        public void tick() {
            tickHover();
        }

        public void tickHover() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (dragon.horizontalCollision) {
                    dragon.setYRot(dragon.getYRot() + 180.0F);
                    BlockPos target = new BlockPos(this.getWantedX(), this.getWantedY(), this.getWantedZ());
                    this.speedModifier = 0.1F;
                    if (target != null) {
                        this.wantedX = target.getX() + 0.5F;
                        this.wantedY = target.getY() + 0.5F;
                        this.wantedZ = target.getZ() + 0.5F;
                    }
                }
                double d0 = this.wantedX - dragon.getX();
                double d1 = this.wantedY - dragon.getY();
                double d2 = this.wantedZ - dragon.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                d3 = Math.sqrt(d3);

                if (d3 < dragon.getBoundingBox().getSize() * 2) {
                    this.operation = MoveControl.Operation.WAIT;
                    dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
                } else {
                    dragon.setDeltaMovement(dragon.getDeltaMovement().add(d0 / d3 * 0.1D * this.speedModifier,
                                                                          d1 / d3 * 0.1D * this.speedModifier,
                                                                          d2 / d3 * 0.1D * this.speedModifier
                    ));

                    if (dragon.getTarget() == null) {
                        dragon.setYRot(-((float) Mth.atan2(dragon.getDeltaMovement().x,
                                                           dragon.getDeltaMovement().z
                        )) * (180F / (float) Math.PI));
                        dragon.yBodyRot = dragon.getYRot();
                    } else {
                        double d4 = dragon.getTarget().getX() - dragon.getX();
                        double d5 = dragon.getTarget().getZ() - dragon.getZ();
                        dragon.setYRot(-((float) Mth.atan2(d4, d5)) * (180F / (float) Math.PI));
                        dragon.yBodyRot = dragon.getYRot();
                    }
                }
            }
        }

    }


    public static class FlyMoveControl extends MoveControl {

        private final EntityDragonBase dragon;

        public FlyMoveControl(EntityDragonBase dragonBase) {
            super(dragonBase);
            this.dragon = dragonBase;
        }

        public boolean shouldHoverToPosition() {
            return dragon.getAirborneState() == DragonBehaviorUtils.AirborneState.HOVER;
        }

        @Override
        public void tick() {
            tickFlight();
        }

        public void tickFlight() {
            if (dragon.horizontalCollision) {
                dragon.setYRot(dragon.getYRot() + 180.0F);
                this.speedModifier = 0.1F;

                return;
            }
            float distX = (float) (this.getWantedX() - dragon.getX());
            float distY = (float) (this.getWantedY() - dragon.getY());
            float distZ = (float) (this.getWantedZ() - dragon.getZ());
            double planeDist = Math.sqrt(distX * distX + distZ * distZ);
            double yDistMod = 1.0D - (double) Mth.abs(distY * 0.7F) / planeDist;
            distX = (float) ((double) distX * yDistMod);
            distZ = (float) ((double) distZ * yDistMod);
            planeDist = Mth.sqrt(distX * distX + distZ * distZ);
            double dist = Math.sqrt(distX * distX + distZ * distZ + distY * distY);
            if (dist > 1.0F) {
                float yawCopy = dragon.getYRot();
                float atan = (float) Mth.atan2(distZ, distX);
                float yawTurn = Mth.wrapDegrees(dragon.getYRot() + 90);
                float yawTurnAtan = Mth.wrapDegrees(atan * 57.295776F);
                dragon.setYRot(IafDragonFlightManager.approachDegrees(yawTurn,
                                                                      yawTurnAtan,
                                                                      dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getTarget() != null ? 10 : 4.0F
                ) - 90.0F);
                dragon.yBodyRot = dragon.getYRot();
                if (IafDragonFlightManager.degreesDifferenceAbs(yawCopy, dragon.getYRot()) < 3.0F) {
                    speedModifier = IafDragonFlightManager.approach((float) speedModifier,
                                                                    1.8F,
                                                                    0.005F * (1.8F / (float) speedModifier)
                    );
                } else {
                    speedModifier = IafDragonFlightManager.approach((float) speedModifier, 0.2F, 0.025F);
                    if (dist < 100D && dragon.getTarget() != null) {
                        speedModifier = speedModifier * (dist / 100D);
                    }
                }
                float finPitch = (float) (-(Mth.atan2(-distY, planeDist) * 57.2957763671875D));
                dragon.setXRot(finPitch);
                float yawTurnHead = dragon.getYRot() + 90.0F;
                speedModifier *= dragon.getFlightSpeedModifier();
//                speedModifier *= Math.min(1, dist / 50 + 0.3);//Make the dragon fly slower when close to target
                double lvt_16_1_ = speedModifier * Mth.cos(yawTurnHead * 0.017453292F) * Math.abs((double) distX / dist);
                double lvt_18_1_ = speedModifier * Mth.sin(yawTurnHead * 0.017453292F) * Math.abs((double) distZ / dist);
                double lvt_20_1_ = speedModifier * Mth.sin(finPitch * 0.017453292F) * Math.abs((double) distY / dist);
                double motionCap = 0.2D;
                dragon.setDeltaMovement(dragon.getDeltaMovement().add(Math.min(lvt_16_1_ * 0.2D, motionCap),
                                                                      Math.min(lvt_20_1_ * 0.2D, motionCap),
                                                                      Math.min(lvt_18_1_ * 0.2D, motionCap)
                ));
            }
        }


    }

    public static float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
        float f = Mth.wrapDegrees(pTargetAngle - pSourceAngle);
        if (f > pMaximumChange) {
            f = pMaximumChange;
        }

        if (f < -pMaximumChange) {
            f = -pMaximumChange;
        }

        float f1 = pSourceAngle + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }
}
