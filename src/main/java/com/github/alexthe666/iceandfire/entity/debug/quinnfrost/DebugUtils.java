package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import com.github.alexthe666.iceandfire.message.MessageSyncPath;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageClientDraw;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageDebugEntity;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class DebugUtils {

    private static Map<Player, UUID> getTrackingMap() {
        return AbstractPathJob.trackingMap;
    }

    public static Optional<PathfinderMob> getDebuggableTarget(Entity entity) {
        if (entity instanceof PathfinderMob) {
            return Optional.of((PathfinderMob) entity);
        } else if (entity instanceof EntityMutlipartPart part && part.getRootParent() instanceof PathfinderMob pathfinderMob) {
            return Optional.of(pathfinderMob);
        } else {
            return Optional.empty();
        }
    }

    public static boolean isTracking(Player player, Entity entity) {
        return getTrackingMap().getOrDefault(player,
                                             UUID.randomUUID()
        ).equals(getDebuggableTarget(entity).map(mob -> mob.getUUID()).orElse(UUID.randomUUID()));
    }

    public static boolean isTracking(Player player) {
        return getTrackingMap().containsKey(player);
    }

    public static boolean isTracking(Entity entity) {
        return getTrackingMap().containsValue(getDebuggableTarget(entity).map(mob -> mob.getUUID()).orElse(UUID.randomUUID()));
    }

    public static boolean switchTracking(Player player, Entity entity) {
        if (isTracking(player, entity)) {
            stopTracking(player, true);
            return false;
        } else {
            startTracking(player, entity);
            return true;
        }
    }

    public static void startTracking(Player player, Entity entity) {
        // Handle debug path render
        if (ExtendedEntityDebugger.EXTENDED_DEBUG) {
            getDebuggableTarget(entity).ifPresent(mob -> {
                getTrackingMap().put(player, mob.getUUID());
                IceAndFire.LOGGER.debug(player.getName().getString() + " start tracking " + mob.getName().getString());
                player.displayClientMessage(Component.nullToEmpty("Tracking: " + getEntityNameLong(mob)), false);

            });
        }
    }

    public static void stopTracking(Player player, boolean clearScreen) {
        getTrackingMap().remove(player);
        if (clearScreen) {
            IceAndFire.sendMSGToPlayer(new MessageSyncPath(new HashSet<>(), new HashSet<>(), new HashSet<>()),
                                       (ServerPlayer) player
            );
            player.displayClientMessage(Component.nullToEmpty("Stop tracking"), false);
            IceAndFire.sendMSGToPlayer(new MessageDebugEntity(),
                                       (ServerPlayer) player
            );

        }
        EntityCommander.removeCommand(player);
        player.displayClientMessage(Component.nullToEmpty("Stopped tracking"), false);

        IceAndFire.LOGGER.debug(player.getName().getString() + " stop tracking");
    }

    public static void stopTracking(Player player) {
        stopTracking(player, true);
    }

    public static Optional<PathfinderMob> getTrackingMob(Player player) {
        if (getTrackingMap().containsKey(player)) {
            UUID uuid = getTrackingMap().get(player);
            if (player.level() instanceof ServerLevel serverLevel && serverLevel.getEntity(uuid) instanceof PathfinderMob target) {
                return Optional.of(target);
            }
        }
        // Failed to get tracking mob, this might because wrong type of entity is added to track list or player is in another dimension
        // Either way, we clear the player from track list
        stopTracking(player, true);
        return Optional.empty();
    }

    public static Optional<Player> getTrackingPlayer(Entity entity) {
        if (getTrackingMap().containsValue(entity.getUUID())) {
            for (Player player : getTrackingMap().keySet()) {
                if (getTrackingMap().get(player).equals(entity.getUUID())) {
                    return Optional.of(player);
                }
            }
        }
        return Optional.empty();
    }

    public static String formatBlockPos(BlockPos pos) {
        if (pos != null) {
            return String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
        } else {
            return "-, -, -";
        }
    }

    public static String formatVector(Vec3 vector3d) {
        if (vector3d != null) {
            return String.format("%.4f, %.4f, %.4f", vector3d.x(), vector3d.y(), vector3d.z());
        } else {
            return "-, -, -";
        }
    }
    
    public static String formatAttribute(LivingEntity entity, Attribute attribute) {
        return Optional.ofNullable(entity.getAttribute(attribute)).map(attributeInstance -> {
            return String.format("%.2f", attributeInstance.getValue());
        }).orElse("-");
    }

    public static double getSpeed(Mob mob) {
//        double dX = mob.getX() - mob.xOld;
//        double dY = mob.getY() - mob.yOld;
//        double dZ = mob.getZ() - mob.zOld;
        return mob.getPosition(1.0f).distanceTo(new Vec3(mob.xOld, mob.yOld, mob.zOld)) / 0.05;
    }

    /**
     * Try to get an entity's target position
     *
     * @param entity
     * @return
     */
    public static Vec3 getReachTarget(Mob entity) {
        try {
            BlockPos targetPos = null;
            if (entity.getNavigation() instanceof AdvancedPathNavigate) {
                AdvancedPathNavigate navigate = (AdvancedPathNavigate) entity.getNavigation();
                // What is this?
                if (navigate.getTargetPos() != null) {
                    targetPos = navigate.getTargetPos();
                } else if (navigate.getDestination() != null) {
                    targetPos = navigate.getDestination();
                } else if (navigate.getDesiredPos() != null) {
                    targetPos = navigate.getDesiredPos();
                } else {
                    return null;
                }

            } else if (entity.getNavigation().getTargetPos() != null) {
                targetPos = entity.getNavigation().getTargetPos();
            }

            return (targetPos == null ? null : Vec3.atCenterOf(targetPos));
        } catch (Exception ignored) {

        }
        return null;
    }

    public static List<String> getEntityNameLong(LivingEntity mob) {
        return List.of(String.format("%s \"%s\" [%s](%d) (%.1f/%.1f)",
                                     mob.getName().getString(),
                                     mob.getCustomName() == null ? "-" : mob.getCustomName(),
                                     mob.getEncodeId(),
                                     mob.getId(),
                                     mob.getHealth(),
                                     mob.getAttribute(
                                             Attributes.MAX_HEALTH).getValue()
                       )
        );
    }

    public static List<String> getPositionInfo(PathfinderMob mobEntity, Player player) {
        return List.of(String.format("Pos: %.5f, %.5f, %.5f ",
                                     mobEntity.position().x,
                                     mobEntity.position().y,
                                     mobEntity.position().z
                       ) + String.format("[%d, %d, %d] ",
                                         mobEntity.blockPosition().getX(),
                                         mobEntity.blockPosition().getY(),
                                         mobEntity.blockPosition().getZ()
                       ) + String.format("(%.2f)", mobEntity.distanceTo(player)),
                       String.format("Rot: %.2f, %.2f ", mobEntity.getXRot(), mobEntity.getYRot())
                               + String.format("(%s)", mobEntity.getDirection()),
                       "Motion: " + String.format("%.3f, %.3f, %.3f (%.2f)",
                                                  mobEntity.getDeltaMovement().x,
                                                  mobEntity.getDeltaMovement().y,
                                                  mobEntity.getDeltaMovement().z,
                                                  getSpeed(mobEntity)
                       )

        );
    }

    public static List<String> getRotationInfo(PathfinderMob mob, Player player) {
        return List.of(
                "Facing: " + String.format("%s", formatVector(mob.getLookAngle())),
                String.format("Rot: %.2f(%.2f), %.2f(%.2f) ", mob.getXRot(), mob.xRotO, mob.getYRot(), mob.yRotO)
                        + String.format("(%s)", mob.getDirection()),
                String.format("yBodyRot: %.2f(%.2f) ", mob.yBodyRot, mob.yBodyRotO) + String.format("yHeadRot: %.2f(%.2f)", mob.yHeadRot, mob.yHeadRotO)

        );
    }

    public static List<String> getTravelInfo(PathfinderMob mob, Player player) {
        return List.of(
                String.format("AISpeed:%.2f", mob.getSpeed()),
                String.format("Strafing:%f - Vertical:%f - Forward:%f", mob.xxa, mob.yya, mob.zza),
                String.format("XRot:%.2f, YRot:%.2f", mob.getXRot(), mob.getYRot())
        );
    }

    public static List<String> getAttributeInfo(PathfinderMob mob, Player player) {
        return List.of(
//                String.format("Armor: %.2f", formatAttribute(mob, Attributes.ARMOR)),
//                String.format("FollowRange: %.2f", formatAttribute(mob, Attributes.FOLLOW_RANGE)),
//                String.format("AttackDamage: %.2f", formatAttribute(mob, Attributes.ATTACK_DAMAGE)),
//                String.format("AttackSpeed: %.2f", formatAttribute(mob, Attributes.ATTACK_SPEED)),
//                String.format("KnockbackResistance: %.2f", formatAttribute(mob, Attributes.KNOCKBACK_RESISTANCE)),
//                String.format("FlyingSpeed: %s", formatAttribute(mob, Attributes.FLYING_SPEED)),
                String.format("MovementSpeed: %s", formatAttribute(mob, Attributes.MOVEMENT_SPEED))
        );
    }

    public static List<String> getDestinationInfo(PathfinderMob mobEntity, Player player) {
        Vec3 targetVec = getReachTarget(mobEntity);
        String targetPosString = "TargetPos: " + (targetVec == null ? "-" :
                String.format("%.2f, %.2f, %.2f (%.2f)",
                              targetVec.x, targetVec.y, targetVec.z,
                              mobEntity.position().distanceTo(targetVec)
                ));
        return List.of(
                targetPosString
        );
    }

    public static List<String> getGoalInfo(PathfinderMob mobEntity, Player player) {
        return List.of(
                "Goals: " + mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(
                        Collectors.toList()).toString(),
                "Selectors: " + mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(
                        Collectors.toList()).toString()
        );
    }

    public static List<String> getTaskInfo(PathfinderMob mob, Player player) {
        String scheduleString = (mob.getBrain().getSchedule() == null ? "" : mob.getBrain().getActiveActivities()) + String.format(
                " [%s]",
                (mob.getBrain().getSchedule() == null ? "-" : mob.getBrain().getSchedule().getActivityAt((int) mob.level().getDayTime()))
        );

        return List.of(
                "Schedule: " + scheduleString,
                "Activity: " + String.format("(%s)",
                                             mob.getBrain().getActiveNonCoreActivity().orElse(new Activity(""))
                ),
                "Tasks: " + mob.getBrain().getRunningBehaviors().toString()
        );
    }

    public static List<String> getMemoryInfo(PathfinderMob mob, Player player) {
        Brain<?> brain = mob.getBrain();
        List<String> stringList = new ArrayList<>();

        try {
            if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                brain.getMemory(MemoryModuleType.WALK_TARGET).ifPresent(target -> {
                    stringList.add("WalkTarget: " + formatBlockPos(target.getTarget().currentBlockPosition()));
                });
            }
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                    stringList.add("AttackTarget: " + target.getName().getContents());
                });
            }
            if (brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
                brain.getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(iPosWrapper -> {
                    stringList.add("LookTarget: " + formatBlockPos(iPosWrapper.currentBlockPosition()));
                });
            }
//            if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) {
//                brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(entityList -> {
//                    stringList.add("VisibleMobs: " + entityList.);
//                });
//            }
            return stringList;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }

    }

    public static List<String> getNavigationInfo(PathfinderMob mob, Player player) {
        return new ArrayList<>();
    }

    public static List<String> getMoveControlInfo(PathfinderMob mob, Player player) {
        return new ArrayList<>();
    }

    public static List<String> getAttackTargetInfo(PathfinderMob mobEntity, Player player) {
        LivingEntity targetEntity = mobEntity.getTarget();
        return List.of(
                "AttackTarget: " + (targetEntity == null ? "-" :
                        String.format("%s [%s] (%.1f/%s) [%d, %d, %d] (%.2f)",
                                      targetEntity.getName().getString(),
                                      targetEntity.getEncodeId(),
                                      mobEntity.getHealth(),
                                      Objects.toString((mobEntity.getAttribute(
                                              Attributes.MAX_HEALTH).getValue()), "-"),
                                      mobEntity.getTarget().blockPosition().getX(),
                                      mobEntity.getTarget().blockPosition().getY(),
                                      mobEntity.getTarget().blockPosition().getZ(),
                                      mobEntity.position().distanceTo(targetEntity.position())
                        ))
                // Todo: target attack and defence info here
        );
    }

    public static List<String> getEntityInfoShort(PathfinderMob mob, Player player) {
        return List.of(
                String.format("%s \"%s\" [%s](%d) (%.1f/%.1f)",
                              mob.getName().getString(),
                              mob.getCustomName() == null ? "-" : mob.getCustomName(),
                              mob.getEncodeId(),
                              mob.getId(),
                              mob.getHealth(),
                              mob.getAttribute(
                                      Attributes.MAX_HEALTH).getValue()
                )
        );
    }

    public static List<String> getRiderInfo(PathfinderMob mob, Player player) {
        return new ArrayList<>();
    }

    public static List<String> getDebuggerInfo(PathfinderMob mob, Player player) {
        return new ArrayList<>();
    }

    public static List<String> getFlags(PathfinderMob mobEntity, Player player) {
        return List.of("StepHeight:" + mobEntity.getStepHeight(),
                       "OnGround: " + mobEntity.onGround(),
                       "isInWater? " + mobEntity.isInWater(),
                       "FluidHeight: " + mobEntity.getFluidHeight(FluidTags.WATER),
                       "HorizontalCollide? " + mobEntity.horizontalCollision,
                       "VerticalCollide? " + (mobEntity.verticalCollision ? (mobEntity.verticalCollisionBelow ? "↓" : "↑") : "")
        );


    }

    public static List<String> getTargetInfoString(PathfinderMob mobEntity, Player player) {
        if (mobEntity == null) {
            return new ArrayList<>();
        }
        mobEntity.level().getProfiler().push("debugString");

        List<String> list = new ArrayList<>();

        list.addAll(getEntityNameLong(mobEntity));
        list.addAll(getPositionInfo(mobEntity, player));
        list.addAll(getRotationInfo(mobEntity, player));
        list.addAll(getTravelInfo(mobEntity, player));
        list.addAll(getGoalInfo(mobEntity, player));
        list.addAll(getTaskInfo(mobEntity, player));
        list.addAll(getAttributeInfo(mobEntity, player));
        list.addAll(getAttackTargetInfo(mobEntity, player));
        list.addAll(getDestinationInfo(mobEntity, player));
        list.addAll(getFlags(mobEntity, player));

        return list;
    }

    public static void onTrackerUpdate(Player player) {
        getTrackingMob(player).ifPresent(pathfinderMob -> {
//            OverlayInfoPanel.bufferInfoLeft = getTargetInfoString(pathfinderMob, player);
//            RenderNode.setRenderPos(2, pathfinderMob.position().add(pathfinderMob.getDeltaMovement().scale(4f)), pathfinderMob.position(), 25555);
            IceAndFire.sendMSGToPlayer(new MessageDebugEntity(pathfinderMob.getId(),
                                                              new ArrayList<>(),
                                                              getTargetInfoString(pathfinderMob, player)
                                       ),
                                       (ServerPlayer) player
            );
            IceAndFire.sendMSGToPlayer(new MessageClientDraw(25556,
                                                             pathfinderMob.position().add(0,
                                                                                          pathfinderMob.getBbHeight() / 2f,
                                                                                          0
                                                             ).add(pathfinderMob.getDeltaMovement().scale(
                                                                     4f)),
                                                             pathfinderMob.position().add(0,
                                                                                          pathfinderMob.getBbHeight() / 2f,
                                                                                          0
                                                             )
                                       ),
                                       (ServerPlayer) player
            );

            Vec3 targetPos = getReachTarget(pathfinderMob);
            if (targetPos != null) {
                IceAndFire.sendMSGToPlayer(new MessageClientDraw(25557,
                                                                 targetPos,
                                                                 pathfinderMob.getPosition(1.0f)
                                           ),
                                           (ServerPlayer) player
                );
            }
        });

    }

}
