package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonActivity;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonSensorType;
import com.github.alexthe666.iceandfire.entity.behavior.core.FlyAndHover;
import com.github.alexthe666.iceandfire.entity.behavior.core.LookAt;
import com.github.alexthe666.iceandfire.entity.behavior.core.Perch;
import com.github.alexthe666.iceandfire.entity.behavior.core.WalkAndStay;
import com.github.alexthe666.iceandfire.entity.behavior.utils.StopAttackingIf;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BehaviorHippogryph {
    public static final int PATHFIND_TICK_TIMESTAMP_OFFSET = -1;
    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;

    @NotNull
    public static ImmutableList<MemoryModuleType<?>> getMemoryTypes() {
        return ImmutableList.of(
                MemoryModuleType.LOOK_TARGET,

                MemoryModuleType.WALK_TARGET,
                DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE,
                DragonMemoryModuleType.COMMAND_STAY_POSITION,
                DragonMemoryModuleType.FORBID_WALKING,
                DragonMemoryModuleType.FORBID_FLYING,
                MemoryModuleType.PATH,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.HAS_HUNTING_COOLDOWN,

                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_HOSTILE,
                DragonMemoryModuleType.NEAREST_HUNTABLE,


                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY,
                DragonMemoryModuleType.LAST_OWNER_HURT_TARGET,
                DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET,

                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_ADULT,

                MemoryModuleType.BREED_TARGET,
                MemoryModuleType.TEMPTING_PLAYER,
                MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                MemoryModuleType.IS_TEMPTED,

                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,

                MemoryModuleType.HOME,
                DragonMemoryModuleType.FORBID_GO_HOME,

                DragonMemoryModuleType.PERSIST_MEMORY_TEST

        );
    }

    @NotNull
    public static ImmutableList<SensorType<? extends Sensor<? super EntityHippogryph>>> getSensorTypes() {
        return ImmutableList.of(
//                SensorType.NEAREST_LIVING_ENTITIES,
                DragonSensorType.LONG_RANGE_LIVING_ENTITY_SENSOR,
                DragonSensorType.NEAREST_ADULT_TAMED,

                DragonSensorType.OWNER_HURT_BY_TARGET_SENSOR,
                DragonSensorType.OWNER_HURT_TARGET_SENSOR,
                SensorType.HURT_BY,

                DragonSensorType.HIPPOGRYPH_TEMPTATIONS,
                DragonSensorType.NEAREST_WANTED_ITEM_TAMED,

                DragonSensorType.HIPPOGRYPH_HUNTABLES,

                DragonSensorType.SENSOR_TEST
        );
    }

    public static void moveTo(EntityHippogryph hippogryph, Vec3 vec3) {
        if (hippogryph.canMove()) {

        }
    }

    public static void registerActivities(Brain<EntityHippogryph> brain) {
        brain.addActivity(Activity.CORE, BehaviorHippogryph.getCorePackage());
        brain.addActivity(Activity.IDLE, BehaviorHippogryph.getWanderPackage());
        brain.addActivityWithConditions(
                Activity.FIGHT,
                BehaviorHippogryph.getFightPackage(),
                ImmutableSet.of(Pair.of(
                        MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
                ))
        );

        brain.addActivity(DragonActivity.SIT, BehaviorHippogryph.getSitPackage());
        brain.addActivity(Activity.RIDE, BehaviorHippogryph.getRidePackage());
        brain.addActivityWithConditions(
                DragonActivity.HUNT,
                BehaviorHippogryph.getHuntPackage(),
                ImmutableSet.of(
//                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                )
        );
        brain.addActivityWithConditions(
                DragonActivity.FOLLOW,
                BehaviorHippogryph.getEscortPackage(),
                ImmutableSet.of(Pair.of(
                        MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT
                ))
        );
    }

    public static void stopActivity(Mob mob, Activity activity) {
        if (mob.getBrain().getActiveNonCoreActivity().orElse(activity) == activity) {
            mob.getBrain().updateActivityFromSchedule(mob.level.getDayTime(), mob.level.getGameTime());
        }
    }

    public static void updateActivity(EntityHippogryph hippogryph) {
        Brain<EntityHippogryph> brain = hippogryph.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse((Activity) null);
        if (hippogryph.getControllingPassenger() != null) {
            brain.setActiveActivityIfPossible(Activity.RIDE);
        } else {
            stopActivity(hippogryph, Activity.RIDE);
        }

        if (hippogryph.isOrderedToSit() && hippogryph.getCommand() == 1) {
            brain.setActiveActivityIfPossible(DragonActivity.SIT);
        } else {
            stopActivity(hippogryph, DragonActivity.SIT);
        }

        if (hippogryph.isTame() && hippogryph.getCommand() == 2) {
            brain.setActiveActivityIfPossible(DragonActivity.FOLLOW);
        } else {
            stopActivity(hippogryph, DragonActivity.FOLLOW);
        }

        if (hippogryph.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            brain.setActiveActivityIfPossible(Activity.FIGHT);
        } else {
            stopActivity(hippogryph, Activity.FIGHT);
        }

        if (activity == Activity.FIGHT && brain.getActiveNonCoreActivity().orElse((Activity) null) != Activity.FIGHT) {
            brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 240L);
        }
        Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity) null);
    }

    public static void isFormerTargetMoreImportant(EntityHippogryph hippogryph, LivingEntity newTarget, LivingEntity oldTarget) {
        LivingEntity owner = hippogryph.getOwner();
        if (owner != null) {
            if (owner.getLastHurtByMob() == oldTarget) {

            }
        }
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(EntityHippogryph hippogryph) {
        if (hippogryph.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET)) {
            return Optional.empty();
        }
//        if (hippogryph.getOwner() instanceof Player player) {
//            int currentTimestamp = player.tickCount;
//            LivingEntity protectToAttack = player.getLastHurtByMob();
//            if (protectToAttack != null
//                    && player.getLastHurtByMobTimestamp() == currentTimestamp + AI_TICK_TIMESTAMP_OFFSET
//                    && hippogryph.wantsToAttack(protectToAttack, hippogryph.getOwner())) {
//                return Optional.of(protectToAttack);
//            }
//            LivingEntity assistToAttack = player.getLastHurtMob();
//            if (assistToAttack != null
//                    && player.getLastHurtMobTimestamp() == currentTimestamp + AI_TICK_TIMESTAMP_OFFSET
//                    && hippogryph.wantsToAttack(assistToAttack, hippogryph.getOwner())) {
//                return Optional.of(assistToAttack);
//            }
//        }
//        int attackerTimestamp = hippogryph.tickCount;
//        LivingEntity revengeToAttack = hippogryph.getLastHurtByMob();
//        if (revengeToAttack != null
//                && hippogryph.getLastHurtByMobTimestamp() == attackerTimestamp + AI_TICK_TIMESTAMP_OFFSET
//                && hippogryph.canAttack(revengeToAttack)) {
//            return Optional.of(revengeToAttack);
//        }
//
//
//        if (hippogryph.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).isPresent()) {
//            LivingEntity nearestAttackable = hippogryph.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).get();
//            if (checkShouldAttack(hippogryph, nearestAttackable)) {
//                return Optional.of(nearestAttackable);
//            }
//        }
//
//        return Optional.empty();
        Optional<LivingEntity> potentialTargetOptional = hippogryph.getBrain().getMemory(DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET);
        if (potentialTargetOptional.isPresent()) {
            return potentialTargetOptional;
        }
        potentialTargetOptional = hippogryph.getBrain().getMemory(DragonMemoryModuleType.LAST_OWNER_HURT_TARGET);
        if (potentialTargetOptional.isPresent()) {
            return potentialTargetOptional;
        }

        // PiglinBruteAI#findNearestValidAttackTarget
        potentialTargetOptional = hippogryph.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (potentialTargetOptional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(hippogryph,
                                                                                                potentialTargetOptional.get()
        )) {
            return potentialTargetOptional;
        }
        return hippogryph.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    private static Optional<? extends LivingEntity> findSuitableAssistAttackTarget(EntityHippogryph hippogryph) {
        return DragonSensorType.getAssistOwnerTarget(hippogryph);
    }

    public static double getAwareDistance(EntityHippogryph hippogryph) {
        return hippogryph.isFlying() ? 128d : 16d;
    }

    @Deprecated
    public static boolean checkShouldAttack(EntityHippogryph hippogryph, LivingEntity target) {
        TargetingConditions conditions = TargetingConditions.DEFAULT;

        // TargetGoal#canAttack
        if (target == null) {
            return false;
        } else if (!conditions.test(hippogryph, target)) {
            return false;
        } else if (!hippogryph.isWithinRestriction(target.blockPosition())) {
            return false;
        }

        return hippogryph.wantsToAttack(target, hippogryph.getOwner());
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(Items.RABBIT_FOOT);
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getCorePackage() {
        return ImmutableList.of(
                Pair.of(1, new Swim(0.8f)),
                Pair.of(1, new LookAt<>(45, 90)),
                Pair.of(1, new WalkAndStay<>()),
                Pair.of(1, new FlyAndHover<>()),
                Pair.of(2, new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getIdlePackage() {
        return ImmutableList.of(
//                Pair.of(0, new RunOne<>(ImmutableList.of(
//                        Pair.of(new OwnerHurtByTarget<>(), 0),
//                        Pair.of(new OwnerHurtTarget<>(), 1),
//                        Pair.of(new HurtByTarget<>(), 2)
//                ))),
                Pair.of(0,
                        new StartAttacking<>(hippogryph -> hippogryph.canMove(),
                                             BehaviorHippogryph::findNearestValidAttackTarget
                        )
                ),
//                Pair.of(0, new RunOne<>(ImmutableMap.of(
//                        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
//                ), ImmutableList.of(
//                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1),
//                        Pair.of(new SetEntityLookTarget(8.0F), 1),
//                        Pair.of(new DoNothing(30, 60), 1)
//                ))),
                Pair.of(1, new DoNothing(20, 60))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getFightPackage() {
        return ImmutableList.of(
                Pair.of(0, new StopAttackingIfTargetInvalid<>(
                        mob -> {
                            return mob == null || !mob.isAlive();
                        },
                        hippogryph -> {
                            hippogryph.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                            hippogryph.land();
                        }
                ) {
                    @Override
                    protected void start(ServerLevel pLevel, EntityHippogryph pEntity, long pGameTime) {
                        super.start(pLevel, pEntity, pGameTime);
                    }
                }),
                Pair.of(0,
                        new RememberNextTargetOrSwap<>(hippogryph -> hippogryph.canMove(),
                                                       BehaviorHippogryph::findNearestValidAttackTarget
                        )
                ),
                Pair.of(0, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f)),
                Pair.of(0, new MeleeAttack(0)),
//                Pair.of(1, new RunOne<>(ImmutableList.of(
//                        Pair.of(new OwnerHurtByTarget<>(), 0),
//                        Pair.of(new OwnerHurtTarget<>(), 1),
//                        Pair.of(new HurtByTarget<>(), 2)
//                ))),
                Pair.of(1, new DoNothing(20, 60))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getSitPackage() {
        return ImmutableList.of(
                Pair.of(0, new Perch<>()),
//                Pair.of(0, new RunOne<>(ImmutableList.of(
//                        Pair.of(new OwnerHurtByTarget<>(), 0),
//                        Pair.of(new OwnerHurtTarget<>(), 1),
//                        Pair.of(new HurtByTarget<>(), 2)
//                )))
                Pair.of(1, new DoNothing(30, 60)),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getRidePackage() {
        return ImmutableList.of();
    }

    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);

    /**
     * Hippogryph wander/idle behavior
     * 1. random look
     * 2. breed
     * 3. follow adult/temptation
     * 4. attack trigger
     * 5. random stroll
     *
     * @return
     * @see net.minecraft.world.entity.animal.axolotl.AxolotlAi#initIdleActivity(Brain)
     */
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getWanderPackage() {
        return ImmutableList.of(
                // Random look
                Pair.of(0, new RunOne<>(ImmutableMap.of(), ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), 1),
                        Pair.of(new LookAtPoi<>(30, 60), 2),
                        Pair.of(new DoNothing(30, 60), 5)
                ))),
                // Breed
                Pair.of(1, new AnimalMakeLove(IafEntityRegistry.HIPPOGRYPH.get(), 0.6F)),
                // Follow temptation
                Pair.of(2, new GateBehavior<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(),
                        GateBehavior.OrderPolicy.ORDERED,
                        GateBehavior.RunningPolicy.TRY_ALL,
                        ImmutableList.of(
                                Pair.of(new HippogryphGoEat<>(entityHippogryph -> {
                                    return !entityHippogryph.isTame();
                                }, 1.0f, true, 18), 1),
                                Pair.of(new FollowTemptationTamed(), 1)
//                                Pair.of(new FollowAdultTamed<>(), 1)
                        )
                )),
                // Start attacking, find water
                Pair.of(3, new StartAttacking<>(BehaviorHippogryph::findNearestValidAttackTarget) {
                    @Override
                    protected void start(ServerLevel pLevel, EntityHippogryph pEntity, long pGameTime) {
                        super.start(pLevel, pEntity, pGameTime);
//                        pEntity.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
                    }
                }),
                Pair.of(3, new RunSometimes<>(new RandomHunt<>(), UniformInt.of(0, 6))),
                // Random stroll
                // Entry condition is treated like canUse()
                Pair.of(4, new RunOne<>(ImmutableMap.of(
                        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_ABSENT
                ), ImmutableList.of(
                        Pair.of(new RunSometimes<>(new RandomStrollGround<EntityHippogryph>(0.9F, false) {
                            @Override
                            protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
                                return !pOwner.getBrain().hasMemoryValue(MemoryModuleType.HOME) && super.checkExtraStartConditions(
                                        pLevel,
                                        pOwner
                                );
                            }
                        }, UniformInt.of(1, 3)), 2),
                        Pair.of(new StrollAroundPoi(MemoryModuleType.HOME, 1.0f, 16), 2),
//                        Pair.of(new RandomStrollAir<>(0.9F, false), 2),
                        Pair.of(new RunSometimes<>(new HippogryphHighJump<>(), UniformInt.of(3, 6)), 2),
                        Pair.of(new DoNothing(60, 120), 2)
//                        Pair.of(new SetWalkTargetFromLookTarget(AxolotlAi::canSetWalkTargetFromLookTarget, AxolotlAi::getSpeedModifier, 3), 3),
//                        Pair.of(new RunIf<>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5),
//                        Pair.of(new RunIf<>(Entity::isOnGround, new DoNothing(200, 400)), 5))
                )))
        );
    }

    /**
     * Hippogryph hunting: attack with more interrupt conditions
     * This is seperated from wander, maybe it should?
     * 1. random look (more aggressive, distance mob only)
     * 2. tempting player will get attacked
     * 3. random hunt trigger / quit hunt trigger (find nearby dropped food when not having target, and quit hunting)
     * 4. random stroll (longer range) / high jump
     *
     * @return
     */
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getHuntPackage() {
        return ImmutableList.of(
                // Random look
                Pair.of(0,
                        new RunOne<>(ImmutableMap.of(
                                DragonMemoryModuleType.NEAREST_HUNTABLE, MemoryStatus.VALUE_ABSENT
                        ), ImmutableList.of(
                                Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), 1),
                                Pair.of(new LookAtPoi<>(30, 60), 2),
                                Pair.of(new DoNothing(30, 60), 5)
                        ))
                ),

                Pair.of(0, new StartAttacking<>(e -> {
                    return e.getBrain().hasMemoryValue(DragonMemoryModuleType.NEAREST_HUNTABLE);
                }, mob -> {
                    return mob.getBrain().getMemory(DragonMemoryModuleType.NEAREST_HUNTABLE);
                })),
                Pair.of(0, new StopAttackingIf<>(
                        mob -> {
                            return ImmutableSet.of(
                                    MemoryModuleType.BREED_TARGET,
                                    MemoryModuleType.TEMPTING_PLAYER,
                                    MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM
                            ).stream().anyMatch(memoryModuleType -> {
                                return mob.getBrain().hasMemoryValue(memoryModuleType);
                            });
                        },
                        mob -> {
                            return mob == null || !mob.isAlive();
                        },
                        hippogryph -> {
                            hippogryph.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                            hippogryph.land();
                            hippogryph.getBrain().updateActivityFromSchedule(hippogryph.level.getDayTime(),
                                                                             hippogryph.level.getGameTime()
                            );
                        }
                )),
                Pair.of(0, new RunSometimes<>(new StopHuntingIf<>(
                        hippogryph -> {
                            hippogryph.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                            hippogryph.land();
                            hippogryph.getBrain().updateActivityFromSchedule(hippogryph.level.getDayTime(),
                                                                             hippogryph.level.getGameTime()
                            );
                        },
                        200
                ), UniformInt.of(0, 2))),
//                Pair.of(0,
//                        new RememberNextTargetOrSwap<>(hippogryph -> hippogryph.canMove(),
//                                                       BehaviorHippogryph::findNearestValidAttackTarget
//                        )
//                ),
                Pair.of(0, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f)),
                Pair.of(0, new MeleeAttack(0)),
//                Pair.of(1, new RunOne<>(ImmutableList.of(
//                        Pair.of(new OwnerHurtByTarget<>(), 0),
//                        Pair.of(new OwnerHurtTarget<>(), 1),
//                        Pair.of(new HurtByTarget<>(), 2)
//                ))),

                Pair.of(4, new RunOne<>(ImmutableMap.of(
                        DragonMemoryModuleType.NEAREST_HUNTABLE, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_ABSENT
                ), ImmutableList.of(
                        Pair.of(new RunSometimes<>(new RandomStrollGround<EntityHippogryph>(0.9F, false) {
                            @Override
                            protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
                                return !pOwner.getBrain().hasMemoryValue(MemoryModuleType.HOME) && super.checkExtraStartConditions(
                                        pLevel,
                                        pOwner
                                );
                            }
                        }, UniformInt.of(1, 3)), 2),
                        Pair.of(new StrollAroundPoi(MemoryModuleType.HOME, 1.0f, 16), 2),
//                        Pair.of(new RandomStrollAir<>(0.9F, false), 2),
                        Pair.of(new RunSometimes<>(new HippogryphHighJump<>(), UniformInt.of(30, 60)), 2),
                        Pair.of(new DoNothing(60, 120), 2)
//                        Pair.of(new SetWalkTargetFromLookTarget(AxolotlAi::canSetWalkTargetFromLookTarget, AxolotlAi::getSpeedModifier, 3), 3),
//                        Pair.of(new RunIf<>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5),
//                        Pair.of(new RunIf<>(Entity::isOnGround, new DoNothing(200, 400)), 5))
                ))),

                Pair.of(1, new DoNothing(20, 60))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityHippogryph>>> getEscortPackage() {
        return ImmutableList.of(
                Pair.of(0, new FollowAlong<>(10)),
                Pair.of(0, new StartAttacking<>(BehaviorHippogryph::findNearestValidAttackTarget))
        );
    }

//    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getVCorePackage(VillagerProfession pProfession, float pSpeedModifier) {
//        return ImmutableList.of(
//                Pair.of(0, new Swim(0.8F)),
//                Pair.of(0, new InteractWithDoor()),
//                Pair.of(0, new LookAtTargetSink(45, 90)),
//                Pair.of(0, new VillagerPanicTrigger()),
//                Pair.of(0, new WakeUp()),
//                Pair.of(0, new ReactToBell()),
//                Pair.of(0, new SetRaidStatus()),
//                Pair.of(0, new ValidateNearbyPoi(pProfession.getJobPoiType(), MemoryModuleType.JOB_SITE)),
//                Pair.of(0, new ValidateNearbyPoi(pProfession.getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
//                Pair.of(1, new MoveToTargetSink()),
//                Pair.of(2, new PoiCompetitorScan(pProfession)),
//                Pair.of(3, new LookAndFollowTradingPlayerSink(pSpeedModifier)),
//                Pair.of(5, new GoToWantedItem(pSpeedModifier, false, 4)),
//                Pair.of(6, new AcquirePoi(pProfession.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
//                Pair.of(7, new GoToPotentialJobSite(pSpeedModifier)),
//                Pair.of(8, new YieldJobSite(pSpeedModifier)),
//                Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte)14))),
//                Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
//                Pair.of(10, new AssignProfessionFromJobSite()),
//                Pair.of(10, new ResetProfession())
//        );
//    }
//    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getVWorkPackage(VillagerProfession pProfession, float pSpeedModifier) {
//        WorkAtPoi workatpoi;
//        if (pProfession == VillagerProfession.FARMER) {
//            workatpoi = new WorkAtComposter();
//        } else {
//            workatpoi = new WorkAtPoi();
//        }
//
//        return ImmutableList.of(
//                Pair.of(5, new RunOne<>(ImmutableList.of(
//                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2),
//                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2),
//                        Pair.of(new DoNothing(30, 60), 8)
//                ))),
//                Pair.of(5, new RunOne<>(ImmutableList.of(
//                        Pair.of(workatpoi, 7),
//                        Pair.of(new StrollAroundPoi(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
//                        Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
//                        Pair.of(new StrollToPoiList(MemoryModuleType.SECONDARY_JOB_SITE, pSpeedModifier, 1, 6, MemoryModuleType.JOB_SITE), 5),
//                        Pair.of(new HarvestFarmland(), pProfession == VillagerProfession.FARMER ? 2 : 5),
//                        Pair.of(new UseBonemeal(), pProfession == VillagerProfession.FARMER ? 4 : 7)
//                ))),
//                Pair.of(10, new ShowTradesToPlayer(400, 1600)),
//                Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)),
//                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, pSpeedModifier, 9, 100, 1200)),
//                Pair.of(3, new GiveGiftToHero(100)),
//                Pair.of(99, new UpdateActivityFromSchedule())
//        );
//    }
//
//    private static void initCoreActivity(Brain<Piglin> pBrain) {
//        pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(
//                new LookAtTargetSink(45, 90),
//                new MoveToTargetSink(),
//                new InteractWithDoor(),
//                new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION),
//                new CopyMemoryWithExpiry<>(PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION),
//                new StopHoldingItemIfNoLongerAdmiring<>(),
//                new StartAdmiringItemIfSeen<>(120),
//                new StartCelebratingIfTargetDead(300, PiglinAi::wantsToDance),
//                new StopBeingAngryIfTargetDead<>())
//        );
//    }
//
//    private static void initIdleActivity(Brain<Piglin> pBrain) {
//        pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(
//                new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
//                new StartAttacking<>(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
//                new RunIf<>(Piglin::canHunt, new StartHuntingHoglin<>()),
//                SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false),
//                new RunSometimes<>(new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION), RIDE_START_INTERVAL),
//                new RunOne<>(ImmutableList.of(
//                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1),
//                        Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1),
//                        Pair.of(new SetEntityLookTarget(8.0F), 1),
//                        Pair.of(new DoNothing(30, 60), 1)
//                )),
//                new RunOne<>(ImmutableList.of(
//                        Pair.of(new RandomStroll(0.6F), 2),
//                        Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
//                        Pair.of(new RunIf<>(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, new SetWalkTargetFromLookTarget(0.6F, 3)), 2),
//                        Pair.of(new DoNothing(30, 60), 1)
//                )),
//                new SetLookAndInteract(EntityType.PLAYER, 4))
//        );
//    }
//private static void initIdleActivity(Brain<Axolotl> pBrain) {
//    pBrain.addActivity(Activity.IDLE,
//            ImmutableList.of(Pair.of(0,
//                            new RunSometimes<>(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), UniformInt.of(30, 60))),
//                    Pair.of(1, new AnimalMakeLove(EntityType.AXOLOTL, 0.2F)),
//                    Pair.of(2, new RunOne<>(ImmutableList.of(Pair.of(new FollowTemptation(
//                                    AxolotlAi::getSpeedModifier), 1),
//                            Pair.of(new BabyFollowAdult<>(ADULT_FOLLOW_RANGE,
//                                    AxolotlAi::getSpeedModifierFollowingAdult), 1)))),
//                    Pair.of(3, new StartAttacking<>(AxolotlAi::findNearestValidAttackTarget)),
//                    Pair.of(3, new TryFindWater(6, 0.15F)),
//                    Pair.of(4,
//                            new GateBehavior<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
//                                    ImmutableSet.of(),
//                                    GateBehavior.OrderPolicy.ORDERED,
//                                    GateBehavior.RunningPolicy.TRY_ALL,
//                                    ImmutableList.of(Pair.of(new RandomSwim(0.5F), 2),
//                                            Pair.of(new RandomStroll(0.15F, false), 2),
//                                            Pair.of(new SetWalkTargetFromLookTarget(AxolotlAi::canSetWalkTargetFromLookTarget,
//                                                    AxolotlAi::getSpeedModifier,
//                                                    3), 3),
//                                            Pair.of(new RunIf<>(
//                                                    Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5),
//                                            Pair.of(new RunIf<>(Entity::isOnGround, new DoNothing(200, 400)), 5))))));
//}

}
