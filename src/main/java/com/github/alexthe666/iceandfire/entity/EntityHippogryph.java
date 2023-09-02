package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.AnimationHandler;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.behavior.BehaviorHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonSchedule;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonSensorType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.CustomMoveController;
import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import com.github.alexthe666.iceandfire.entity.util.*;
import com.github.alexthe666.iceandfire.enums.EnumHippogryphTypes;
import com.github.alexthe666.iceandfire.inventory.ContainerHippogryph;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.ItemHippogryphEgg;
import com.github.alexthe666.iceandfire.message.MessageHippogryphArmor;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import com.github.alexthe666.iceandfire.pathfinding.PathNavigateFlyingCreature;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class EntityHippogryph extends TamableAnimal implements ISyncMount, IAnimatedEntity, IDragonFlute, IVillagerFear, IAnimalFear, IDropArmor, IFlyingMount, ICustomMoveController, IFlyableBehavior {

    private static final int FLIGHT_CHANCE_PER_TICK = 1200;
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SADDLE = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ARMOR = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHESTED = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HOVERING = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> CONTROL_STATE = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BYTE);
    @Deprecated
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.INT);
    public static Animation ANIMATION_EAT;
    public static Animation ANIMATION_SPEAK;
    public static Animation ANIMATION_SCRATCH;
    public static Animation ANIMATION_BITE;
    public SimpleContainer hippogryphInventory;

    public float sitProgress;
    public float hoverProgress;
    public float flyProgress;
    public int spacebarTicks;
    public int airBorneCounter;
    private Vec3 pMotion;

    public BlockPos getHomePos() {
        return this.getBrain().getMemory(MemoryModuleType.HOME).map(GlobalPos::pos).orElse(null);
    }

    public void setHomePos(BlockPos homePos) {
//        this.homePos = homePos;
        this.getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level.dimension(), homePos));
    }

    @Deprecated
    public BlockPos homePos;
    public boolean hasHomePosition = false;
    public int feedings = 0;
    private boolean isLandNavigator;
    private boolean isSitting;
    private boolean isHovering;
    private boolean isFlying;
    private int animationTick;
    private Animation currentAnimation;
    private int flyTicks;
    private int hoverTicks;
    private boolean hasChestVarChanged = false;
    private int navigatorType = -1;
    private boolean isOverAir;

    public EntityHippogryph(EntityType type, Level worldIn) {
        super(type, worldIn);

        if (this.isHovering) {
            this.airborneState = DragonBehaviorUtils.AirborneState.HOVER;
        } else if (isFlying) {
            this.airborneState = DragonBehaviorUtils.AirborneState.FLY;
        } else {
            this.airborneState = DragonBehaviorUtils.AirborneState.GROUNDED;
        }
        this.switchNavigator(airborneState == DragonBehaviorUtils.AirborneState.GROUNDED);
        ANIMATION_EAT = Animation.create(25);
        ANIMATION_SPEAK = Animation.create(15);
        ANIMATION_SCRATCH = Animation.create(25);
        ANIMATION_BITE = Animation.create(20);
        initHippogryphInv();
        this.maxUpStep = 1;
    }

    public Brain<EntityHippogryph> getBrain() {
        return (Brain<EntityHippogryph>) super.getBrain();
    }

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,

            MemoryModuleType.WALK_TARGET,
            DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE,
            DragonMemoryModuleType.COMMAND_STAY_POSITION,
            DragonMemoryModuleType.FORBID_WALKING,
            DragonMemoryModuleType.FORBID_FLYING,
            MemoryModuleType.PATH,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,

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

            MemoryModuleType.HAS_HUNTING_COOLDOWN,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,

            MemoryModuleType.HOME,
            DragonMemoryModuleType.FORBID_GO_HOME,

            DragonMemoryModuleType.PERSIST_MEMORY_TEST

    );
    private static final ImmutableList<SensorType<? extends Sensor<? super EntityHippogryph>>> SENSOR_TYPES = ImmutableList.of(
            DragonSensorType.NEARBY_LIVING_ENTITIES,
            DragonSensorType.NEAREST_ADULT_TAMED,

            DragonSensorType.OWNER_HURT_BY_TARGET_SENSOR,
            DragonSensorType.OWNER_HURT_TARGET_SENSOR,
            SensorType.HURT_BY,

            DragonSensorType.HIPPOGRYPH_TEMPTATIONS,
            DragonSensorType.NEAREST_WANTED_ITEM_TAMED,

            DragonSensorType.HIPPOGRYPH_HUNTABLES,

            DragonSensorType.SENSOR_TEST
    );
    protected Brain.Provider<EntityHippogryph> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<EntityHippogryph> brain = this.brainProvider().makeBrain(pDynamic);
        this.registerBrainGoals(brain);

//        if (!brain.hasMemoryValue(DragonMemoryModuleType.COMMAND)) {
//            brain.setMemory(DragonMemoryModuleType.COMMAND, 0);
//        }
        return brain;
    }

    public void refreshBrain(ServerLevel pServerLevel) {
        Brain<EntityHippogryph> brain = this.getBrain();
        brain.stopAll(pServerLevel, this);
        this.brain = brain.copyWithoutBehaviors();
        this.registerBrainGoals(this.getBrain());
    }

    private void registerBrainGoals(Brain<EntityHippogryph> brain) {
        BehaviorHippogryph.registerActivities(brain);
        brain.setSchedule(DragonSchedule.HIPPOGRYPH);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
    }

    protected void customServerAiStep() {
        this.level.getProfiler().push("hippogryphBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        BehaviorHippogryph.updateActivity(this);
        this.updateFlightStatus();
        super.customServerAiStep();

    }

    /**
     * onGround var is not updating correctly, don't use it
     */
    public void updateRider() {
        if (!this.isOverAir() && this.isGoingDown() && !this.isInWater()) {
            this.setAirborneState(DragonBehaviorUtils.AirborneState.GROUNDED);
        }
    }

    public void updateFlightStatus() {
//        if (!this.canFly() && this.getAirborneState() != DragonBehaviorUtils.AirborneState.GROUNDED) {
//            this.setAirborneState(DragonBehaviorUtils.AirborneState.GROUNDED);
//        }
//        if (!this.canLand() && this.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
//            this.setAirborneState(DragonBehaviorUtils.AirborneState.FLY);
//        }

        this.setNoGravity(this.getAirborneState() != DragonBehaviorUtils.AirborneState.GROUNDED);
        switch (this.getAirborneState()) {
            case HOVER:
            case FLY:
            case GROUNDED:
                this.setHovering(this.getAirborneState() == DragonBehaviorUtils.AirborneState.HOVER);
                this.setFlying(this.getAirborneState() == DragonBehaviorUtils.AirborneState.FLY);

//                if (this.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
//                    this.takeoff();
//                }
                break;
            case TAKEOFF:
                this.setHovering(true);
                this.setFlying(false);
                if (takeoffCounter++ > 20 || this.isOverAirLogic()) {
                    takeoffCounter = 0;
                    this.setAirborneState(DragonBehaviorUtils.AirborneState.FLY);
                }
                break;
            case LANDING:
                this.setHovering(true);
                this.setFlying(false);
                if (this.getControllingPassenger() == null) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0, -0.1, 0));
                }
                if (!this.isOverAirLogic()) {
                    this.setAirborneState(DragonBehaviorUtils.AirborneState.GROUNDED);
                }
                break;
        }
        if (this.isVehicle()) {
            this.updateRider();
        }
        // Random take off
//        if (!level.isClientSide && !this.isOverAir() && this.getNavigation().isDone() && attackTarget != null && attackTarget.getY() - 3 > this.getY() && this.getRandom().nextInt(15) == 0 && this.canMove() && !this.isHovering() && !this.isFlying()) {
//            this.setHovering(true);
//            this.hoverTicks = 0;
//            this.flyTicks = 0;
//        }
        if (this.isOverAir()) {
            airBorneCounter++;
        } else {
            airBorneCounter = 0;
        }
//        if (this.isFlying() && this.tickCount % 40 == 0 || this.isFlying() && this.isOrderedToSit()) {
//            this.setFlying(true);
//        }
//        // Handle flight and float
//        if ((flying || hovering) && !doesWantToLand() && this.getControllingPassenger() == null) {
//            double up = isInWater() ? 0.16D : 0.08D;
//            this.setDeltaMovement(this.getDeltaMovement().add(0, up, 0));
//        }

        //        if (this.isOnGround() && this.doesWantToLand() && (this.isFlying() || this.isHovering())) {
//            this.setFlying(false);
//            this.setHovering(false);
//        }
//        if (this.isHovering()) {
//            if (this.isOrderedToSit()) {
//                this.setHovering(false);
//            }
//            this.hoverTicks++;
//            if (this.doesWantToLand()) {
//                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.05D, 0));
//            } else {
//                if (this.getControllingPassenger() == null) {
//                    this.setDeltaMovement(this.getDeltaMovement().add(0, 0.08D, 0));
//                }
//                if (this.hoverTicks > 40) {
//                    if (!this.isBaby()) {
//                        this.setFlying(true);
//                    }
//                    this.setHovering(false);
//                    this.hoverTicks = 0;
//                    this.flyTicks = 0;
//                }
//            }
//        }
        if (this.isOnGround() && flyTicks != 0) {
            flyTicks = 0;
        }
        if (this.isFlying() && this.doesWantToLand() && this.getControllingPassenger() == null) {
//            this.setHovering(false);
            if (this.isOnGround()) {
                flyTicks = 0;
            }
//            this.setFlying(false);
        }
        if (this.isFlying()) {
            this.flyTicks++;
        }
        if ((this.isHovering() || this.isFlying()) && this.isOrderedToSit()) {
            this.setAirborneState(DragonBehaviorUtils.AirborneState.GROUNDED);
        }
//        if (this.isVehicle() && this.isGoingDown() && this.isOnGround()) {
//            this.setAirborneState(DragonBehaviorUtils.AirborneState.GROUNDED);
//        }
//        if ((!level.isClientSide && this.getRandom().nextInt(FLIGHT_CHANCE_PER_TICK) == 0 && !this.isOrderedToSit() && !this.isFlying() && this.getPassengers().isEmpty() && !this.isBaby() && !this.isHovering() && !this.isOrderedToSit() && this.canMove() && !this.isOverAir() || this.getY() < -1)) {
//            this.setHovering(true);
//            this.hoverTicks = 0;
//            this.flyTicks = 0;
//        }
    }

    public DragonBehaviorUtils.AirborneState airborneState;
    protected int takeoffCounter = 0;

    @Override
    public void setAirborneState(DragonBehaviorUtils.AirborneState state) {
        this.airborneState = state;

        switch (state) {

            case HOVER:
            case FLY:
            case GROUNDED:

                break;
            case LANDING:
                break;
            case TAKEOFF:
//                this.setHovering(true);
//                this.setFlying(false);
                break;
        }
    }

    @Override
    public DragonBehaviorUtils.AirborneState getAirborneState() {
        return airborneState;
    }

    @Override
    public void takeoff() {
        if (this.isLandNavigator) {
            this.switchNavigator(false);
        }
        if (this.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
            this.setDeltaMovement(this.getDeltaMovement().add(0,0.05,0));
            this.switchNavigator(false);
            this.setAirborneState(DragonBehaviorUtils.AirborneState.TAKEOFF);
        }
    }

    @Override
    public void land() {
        if (this.getAirborneState() == DragonBehaviorUtils.AirborneState.FLY || this.getAirborneState() == DragonBehaviorUtils.AirborneState.HOVER) {
            this.setAirborneState(DragonBehaviorUtils.AirborneState.LANDING);
        }
    }

    @Override
    public void hover() {
        if (this.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
            this.takeoff();
        } else {
            if (this.getControllingPassenger() == null) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5f,0.5f,0.5f));
            }
            this.setAirborneState(DragonBehaviorUtils.AirborneState.HOVER);
        }
    }

    @Override
    public void walkTo(WalkTarget walkTarget) {
        if (this.getAirborneState() != DragonBehaviorUtils.AirborneState.GROUNDED) {
            this.land();
            return;
        }

        if (!isLandNavigator) {
            switchNavigator(true);
        }
        AdvancedPathNavigate navigator = (AdvancedPathNavigate) this.getNavigation();
        navigator.moveToXYZ(
                walkTarget.getTarget().currentPosition().x,
                walkTarget.getTarget().currentPosition().y,
                walkTarget.getTarget().currentPosition().z,
                walkTarget.getSpeedModifier()
        );
    }

    @Override
    public void flightTo(WalkTarget walkTarget) {
        if (this.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
            this.takeoff();
            return;
        }

        if (isLandNavigator) {
            switchNavigator(false);
        }
//        this.getNavigation().moveTo(
//                walkTarget.getTarget().currentPosition().x,
//                walkTarget.getTarget().currentPosition().y,
//                walkTarget.getTarget().currentPosition().z,
//                walkTarget.getSpeedModifier()
//        );
        AdvancedPathNavigate navigator = (AdvancedPathNavigate) this.getNavigation();
        navigator.moveToXYZ(
                walkTarget.getTarget().currentPosition().x,
                walkTarget.getTarget().currentPosition().y,
                walkTarget.getTarget().currentPosition().z,
                walkTarget.getSpeedModifier()
        );
//        FlyMoveHelper moveHelper = (FlyMoveHelper) this.getMoveControl();
//        moveHelper.setWantedPosition(
//                walkTarget.getTarget().currentPosition().x,
//                walkTarget.getTarget().currentPosition().y,
//                walkTarget.getTarget().currentPosition().z,
//                walkTarget.getSpeedModifier()
//        );
    }

    @Override
    public void hoverTo(WalkTarget walkTarget) {

    }

    @Override
    public boolean canLand() {
        if (!IFlyableBehavior.super.canLand()) {
            return false;
        }
        return !brain.getMemory(DragonMemoryModuleType.FORBID_WALKING).orElse(false)
                && !brain.isMemoryValue(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE, DragonMemoryModuleType.NavigationType.FLY)
                ;
    }

    @Override
    public boolean canFly() {
        if (!IFlyableBehavior.super.canFly()) {
            return false;
        }
        return !brain.getMemory(DragonMemoryModuleType.FORBID_FLYING).orElse(false)
                && !brain.isMemoryValue(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE, DragonMemoryModuleType.NavigationType.WALK)
                && !this.getBrain().getMemory(MemoryModuleType.IS_TEMPTED).orElse(false)
                ;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel pLevel, Animal pMate) {
        if (pMate instanceof EntityHippogryph mate) {
            ItemEntity egg = new ItemEntity(pLevel, this.getX(), this.getY(), this.getZ(),
                    ItemHippogryphEgg.createEggStack(this.getEnumVariant(), mate.getEnumVariant()));
            this.setAge(6000);
            mate.setAge(6000);
            this.resetLove();
            mate.resetLove();
            egg.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            if (!pLevel.isClientSide) {
                pLevel.addFreshEntity(egg);
            }
            Random random = this.getRandom();

            for (int i = 0; i < 7; ++i) {
                final double d0 = random.nextGaussian() * 0.02D;
                final double d1 = random.nextGaussian() * 0.02D;
                final double d2 = random.nextGaussian() * 0.02D;
                final double d3 = random.nextDouble() * this.getBbWidth() * 2.0D - this.getBbWidth();
                final double d4 = 0.5D + random.nextDouble() * this.getBbHeight();
                final double d5 = random.nextDouble() * this.getBbWidth() * 2.0D - this.getBbWidth();
                pLevel.addParticle(ParticleTypes.HEART, this.getX() + d3, this.getY() + d4,
                        this.getZ() + d5, d0, d1, d2);
            }

            if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                pLevel.addFreshEntity(new ExperienceOrb(pLevel, this.getX(), this.getY(),
                        this.getZ(), random.nextInt(7) + 1));
            }
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return pStack.is(Items.RABBIT_FOOT);
    }

    /**
     * Used when a tamed is trying to attack because their owner
     * @param targetAssignedByOwner
     * @param pOwner
     * @return
     */
    @Override
    public boolean wantsToAttack(@NotNull LivingEntity targetAssignedByOwner, @NotNull LivingEntity pOwner) {
        if (targetAssignedByOwner instanceof TamableAnimal tamed && tamed.isTame() && tamed.getOwner().equals(pOwner)) {
            return false;
        } else if (targetAssignedByOwner instanceof AbstractHorse horse && horse.isTamed() && horse.getOwnerUUID().equals(this.getOwnerUUID())) {
            return false;
        } else if (targetAssignedByOwner instanceof Player playerVictim && pOwner instanceof Player playerOwner && !playerOwner.canHarmPlayer(playerVictim)) {
            return false;
        }
        return true;
    }

    /**
     * Used when a mob is trying to attack
     * @param pTarget
     * @return
     */
    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return super.canAttack(pTarget);
    }

    @Override
    public boolean canAttackType(EntityType<?> pType) {
        return super.canAttackType(pType);
    }

    @Override
    public boolean isAlliedTo(Team pTeam) {
        return super.isAlliedTo(pTeam);
    }

    @Override
    public void setNoGravity(boolean pNoGravity) {
        super.setNoGravity(pNoGravity);
    }

    @Override
    public void setDeltaMovement(Vec3 pMotion) {
        super.setDeltaMovement(pMotion);
    }

    public static int getIntFromArmor(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() != null && stack.getItem() == IafItemRegistry.IRON_HIPPOGRYPH_ARMOR.get()) {
            return 1;
        }
        if (!stack.isEmpty() && stack.getItem() != null && stack.getItem() == IafItemRegistry.GOLD_HIPPOGRYPH_ARMOR.get()) {
            return 2;
        }
        if (!stack.isEmpty() && stack.getItem() != null && stack.getItem() == IafItemRegistry.DIAMOND_HIPPOGRYPH_ARMOR.get()) {
            return 3;
        }
        return 0;
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Mob.createMobAttributes()
            //HEALTH
            .add(Attributes.MAX_HEALTH, 40.0D)
            //SPEED
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            //ATTACK
            .add(Attributes.ATTACK_DAMAGE, 5.0D)
            //FOLLOW RANGE
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Deprecated(forRemoval = true)
    protected void switchNavigator() {
        if (this.isVehicle() && this.isOverAir()) {
            if (navigatorType != 1) {
                this.moveControl = new IafDragonFlightManager.PlayerFlightMoveHelper(this);
                this.navigation = new PathNavigateFlyingCreature(this, level);
                navigatorType = 1;
            }
        }
        if (!this.isVehicle() || !this.isOverAir()) {
            if (navigatorType != 0) {
                this.moveControl = new MoveControl(this);
                this.navigation = new GroundPathNavigation(this, level);
                navigatorType = 0;
            }
        }
    }

    protected boolean isOverAir() {
        return isOverAir;
    }

    public boolean isOverAirLogic() {
        return level.isEmptyBlock(new BlockPos(this.getX(), this.getBoundingBox().minY - 1, this.getZ()));
    }

    @Override
    protected int getExperienceReward(@NotNull Player player) {
        return 10;
    }

    @Override
    @Deprecated
    protected void registerGoals() {
//        this.goalSelector.addGoal(0, new DragonAIRide(this));
//        this.goalSelector.addGoal(1, new FloatGoal(this));
//        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
//        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
//        this.goalSelector.addGoal(4, new HippogryphAIMate(this, 1.0D));
//        this.goalSelector.addGoal(5, new TemptGoal(this, 1.0D, Ingredient.of(Items.RABBIT, Items.COOKED_RABBIT), false));
//        this.goalSelector.addGoal(6, new AIFlyRandom());
//        this.goalSelector.addGoal(7, new HippogryphAIWander(this, 1.0D));
//        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, LivingEntity.class, 6.0F));
//        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
//        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
//        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
//        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
//        this.targetSelector.addGoal(4, new HippogryphAITargetItems(this, false));
//        this.targetSelector.addGoal(5, new HippogryphAITarget(this, LivingEntity.class, false, new Predicate<Entity>() {
//            @Override
//            public boolean apply(@Nullable Entity entity) {
//                return entity instanceof LivingEntity && !(entity instanceof AbstractHorse) && DragonUtils.isAlive((LivingEntity) entity);
//            }
//        }));
//        this.targetSelector.addGoal(5, new HippogryphAITarget(this, Player.class, 350, false, new Predicate<Player>() {
//            @Override
//            public boolean apply(@Nullable Player entity) {
//                return !entity.isCreative();
//            }
//        }));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(ARMOR, 0);
        this.entityData.define(SADDLE, Boolean.FALSE);
        this.entityData.define(CHESTED, Boolean.FALSE);
        this.entityData.define(HOVERING, Boolean.FALSE);
        this.entityData.define(FLYING, Boolean.FALSE);
        this.entityData.define(CONTROL_STATE, (byte) 0);
        this.entityData.define(COMMAND, 0);

    }

    @Override
    public double getYSpeedMod() {
        return 4;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, @NotNull BlockState state, @NotNull BlockPos pos) {
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return super.isControlledByLocalInstance();
    }

    @Override
    public boolean canBeControlledByRider() {
        return true;
    }

    @Override
    public void positionRider(@NotNull Entity passenger) {
        super.positionRider(passenger);
        if (this.hasPassenger(passenger)) {
            yBodyRot = getYRot();
            setYHeadRot(passenger.getYHeadRot());
            setYBodyRot(passenger.getYRot());
        }
        passenger.setPos(this.getX(), this.getY() + 1.05F, this.getZ());
    }

    private void initHippogryphInv() {
        SimpleContainer animalchest = this.hippogryphInventory;
        this.hippogryphInventory = new SimpleContainer(18);
        if (animalchest != null) {
            int i = Math.min(animalchest.getContainerSize(), this.hippogryphInventory.getContainerSize());
            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = animalchest.getItem(j);
                if (!itemstack.isEmpty()) {
                    this.hippogryphInventory.setItem(j, itemstack.copy());
                }
            }

            if (level.isClientSide) {
                ItemStack saddle = animalchest.getItem(0);
                ItemStack chest = animalchest.getItem(1);
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getId(), 0, saddle != null && saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getId(), 1, chest != null && chest.getItem() == Blocks.CHEST.asItem() && !chest.isEmpty() ? 1 : 0));
                IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getId(), 2, getIntFromArmor(animalchest.getItem(2))));
            }
        }
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player && this.getTarget() != passenger) {
                Player player = (Player) passenger;
                if (this.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID())) {
                    return player;
                }
            }
        }
        return null;
    }

    public boolean isBlinking() {
        return this.tickCount % 50 > 43;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        String s = ChatFormatting.stripFormatting(player.getName().getContents());
        boolean isDev = s.equals("Alexthe666") || s.equals("Raptorfarian") || s.equals("tweakbsd");
        if (this.isTame() && this.isOwnedBy(player)) {
            if (itemstack != null && itemstack.getItem() == Items.RED_DYE && this.getEnumVariant() != EnumHippogryphTypes.ALEX && isDev) {
                this.setEnumVariant(EnumHippogryphTypes.ALEX);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                this.playSound(SoundEvents.ZOMBIE_INFECT, 1, 1);
                for (int i = 0; i < 20; i++) {
                    this.level.addParticle(ParticleTypes.CLOUD, this.getX() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), this.getY() + (double) (this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), 0, 0, 0);
                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack != null && itemstack.getItem() == Items.LIGHT_GRAY_DYE && this.getEnumVariant() != EnumHippogryphTypes.RAPTOR && isDev) {
                this.setEnumVariant(EnumHippogryphTypes.RAPTOR);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                this.playSound(SoundEvents.ZOMBIE_INFECT, 1, 1);
                for (int i = 0; i < 20; i++) {
                    this.level.addParticle(ParticleTypes.CLOUD, this.getX() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), this.getY() + (double) (this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), 0, 0, 0);
                }
                return InteractionResult.SUCCESS;
            }

            if (itemstack != null && itemstack.getItem() == Items.RABBIT_STEW && this.getAge() == 0 && !isInLove()) {
                this.setInLove(player);
                this.playSound(SoundEvents.GENERIC_EAT, 1, 1);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack != null && itemstack.getItem() == Items.STICK) {
                if (player.isShiftKeyDown()) {
                    // Set and remove home
                    if (this.hasHomePosition) {
                        this.hasHomePosition = false;
                        player.displayClientMessage(new TranslatableComponent("hippogryph.command.remove_home"), true);
                        return InteractionResult.SUCCESS;
                    } else {
                        BlockPos pos = this.blockPosition();
                        this.setHomePos(pos);
                        this.hasHomePosition = true;
                        player.displayClientMessage(new TranslatableComponent("hippogryph.command.new_home", this.getHomePos().getX(), this.getHomePos().getY(), this.getHomePos().getZ()), true);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    // Circle commands
                    String commandMsg = "wander";
                    int command = this.getCommand() + 1;
                    switch (command) {
                        // Wander
                        case 0 -> {
                            commandMsg = "wander";
                        }
                        // Sit
                        case 1 -> {
                            commandMsg = "sit";
                        }
                        // Stand
                        case 2 -> {
                            commandMsg = "follow";
                        }
                        default -> {
                            command = 0;
                            commandMsg = "wander";
                        }
                    }
                    this.setCommand(command);
                    player.displayClientMessage(new TranslatableComponent("hippogryph.command." + commandMsg), true);

                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack != null && itemstack.getItem() == Items.GLISTERING_MELON_SLICE && this.getEnumVariant() != EnumHippogryphTypes.DODO) {
                this.setEnumVariant(EnumHippogryphTypes.DODO);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                this.playSound(SoundEvents.ZOMBIE_INFECT, 1, 1);
                for (int i = 0; i < 20; i++) {
                    this.level.addParticle(ParticleTypes.ENCHANT, this.getX() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), this.getY() + (double) (this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), 0, 0, 0);
                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack != null && itemstack.getItem().isEdible() && itemstack.getItem().getFoodProperties() != null && itemstack.getItem().getFoodProperties().isMeat() && this.getHealth() < this.getMaxHealth()) {
                // Feed meat healing
                this.heal(5);
                this.playSound(SoundEvents.GENERIC_EAT, 1, 1);
                for (int i = 0; i < 3; i++) {
                    this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemstack), this.getX() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), this.getY() + (double) (this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double) (this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), 0, 0, 0);
                }
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack.isEmpty()) {
                if (player.isShiftKeyDown()) {
                    this.openGUI(player);
                    return InteractionResult.SUCCESS;
                } else if (this.isSaddled() && !this.isBaby() && !player.isPassenger()) {
                    player.startRiding(this, true);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    public void openGUI(Player playerEntity) {
        if (!this.level.isClientSide && (!this.isVehicle() || this.hasPassenger(playerEntity))) {
            NetworkHooks.openGui((ServerPlayer) playerEntity, new MenuProvider() {
                @Override
                public AbstractContainerMenu createMenu(int p_createMenu_1_, @NotNull Inventory p_createMenu_2_, @NotNull Player p_createMenu_3_) {
                    return new ContainerHippogryph(p_createMenu_1_, hippogryphInventory, p_createMenu_2_, EntityHippogryph.this);
                }

                @Override
                public @NotNull Component getDisplayName() {
                    return new TranslatableComponent("entity.iceandfire.hippogryph");
                }
            });
        }
        IceAndFire.PROXY.setReferencedMob(this);
    }

    @Override
    public boolean isGoingUp() {
        return (entityData.get(CONTROL_STATE).byteValue() & 1) == 1;
    }

    @Override
    public boolean isGoingDown() {
        return (entityData.get(CONTROL_STATE).byteValue() >> 1 & 1) == 1;
    }

    public boolean attack() {
        return (entityData.get(CONTROL_STATE).byteValue() >> 2 & 1) == 1;
    }

    public boolean dismountIAF() {
        return (entityData.get(CONTROL_STATE).byteValue() >> 3 & 1) == 1;
    }

    @Override
    public void up(boolean up) {
        setStateField(0, up);
    }

    @Override
    public void down(boolean down) {
        setStateField(1, down);
    }

    @Override
    public void attack(boolean attack) {
        setStateField(2, attack);
    }

    @Override
    public void strike(boolean strike) {

    }

    @Override
    public void dismount(boolean dismount) {
        setStateField(3, dismount);
    }

    private void setStateField(int i, boolean newState) {
        byte prevState = entityData.get(CONTROL_STATE).byteValue();
        if (newState) {
            entityData.set(CONTROL_STATE, (byte) (prevState | (1 << i)));
        } else {
            entityData.set(CONTROL_STATE, (byte) (prevState & ~(1 << i)));
        }
    }

    @Override
    public byte getControlState() {
        return entityData.get(CONTROL_STATE).byteValue();
    }

    @Override
    public void setControlState(byte state) {
        entityData.set(CONTROL_STATE, state);
    }

    public int getCommand() {
        return this.entityData.get(COMMAND).intValue();
//        Optional<Integer> optional = this.getBrain().getMemory(DragonMemoryModuleType.COMMAND);
//        if (optional.isEmpty()) {
//            IceAndFire.LOGGER.warn("No value for command");
//            return 0;
//        }
//        return optional.get();
    }

    public void setCommand(int command) {
//        this.getBrain().setMemory(DragonMemoryModuleType.COMMAND, command);
        this.entityData.set(COMMAND, command);
        this.setOrderedToSit(command == 1);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());
        compound.putBoolean("Chested", this.isChested());
        compound.putBoolean("Saddled", this.isSaddled());
        compound.putBoolean("Hovering", this.isHovering());
        compound.putBoolean("Flying", this.isFlying());
        compound.putInt("Armor", this.getArmor());
        compound.putInt("Feedings", feedings);
        if (hippogryphInventory != null) {
            ListTag nbttaglist = new ListTag();
            for (int i = 0; i < this.hippogryphInventory.getContainerSize(); ++i) {
                ItemStack itemstack = this.hippogryphInventory.getItem(i);
                if (!itemstack.isEmpty()) {
                    CompoundTag CompoundNBT = new CompoundTag();
                    CompoundNBT.putByte("Slot", (byte) i);
                    itemstack.save(CompoundNBT);
                    nbttaglist.add(CompoundNBT);
                }
            }
            compound.put("Items", nbttaglist);
        }
        compound.putBoolean("HasHomePosition", this.hasHomePosition);
        if (this.getHomePos() != null && this.hasHomePosition) {
            compound.putInt("HomeAreaX", this.getHomePos().getX());
            compound.putInt("HomeAreaY", this.getHomePos().getY());
            compound.putInt("HomeAreaZ", this.getHomePos().getZ());
        }
        compound.putInt("Command", this.getCommand());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt("Variant"));
        this.setChested(compound.getBoolean("Chested"));
        this.setSaddled(compound.getBoolean("Saddled"));
        this.setHovering(compound.getBoolean("Hovering"));
        this.setFlying(compound.getBoolean("Flying"));
        this.setArmor(compound.getInt("Armor"));
        feedings = compound.getInt("Feedings");
        if (hippogryphInventory != null) {
            ListTag nbttaglist = compound.getList("Items", 10);
            this.initHippogryphInv();
            for (int i = 0; i < nbttaglist.size(); ++i) {
                CompoundTag CompoundNBT = nbttaglist.getCompound(i);
                int j = CompoundNBT.getByte("Slot") & 255;
                this.hippogryphInventory.setItem(j, ItemStack.of(CompoundNBT));
            }
        } else {
            ListTag nbttaglist = compound.getList("Items", 10);
            this.initHippogryphInv();
            for (int i = 0; i < nbttaglist.size(); ++i) {
                CompoundTag CompoundNBT = nbttaglist.getCompound(i);
                int j = CompoundNBT.getByte("Slot") & 255;
                this.initHippogryphInv();
                this.hippogryphInventory.setItem(j, ItemStack.of(CompoundNBT));
                //this.setArmorInSlot(j, this.getIntFromArmor(ItemStack.loadItemStackFromNBT(CompoundNBT)));
                ItemStack saddle = hippogryphInventory.getItem(0);
                ItemStack chest = hippogryphInventory.getItem(1);
                if (level.isClientSide) {
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getId(), 0, saddle != null && saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getId(), 1, chest != null && chest.getItem() == Blocks.CHEST.asItem() && !chest.isEmpty() ? 1 : 0));
                    IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getId(), 2, getIntFromArmor(hippogryphInventory.getItem(2))));
                }
            }
        }
        this.hasHomePosition = compound.getBoolean("HasHomePosition");
        if (hasHomePosition && compound.getInt("HomeAreaX") != 0 && compound.getInt("HomeAreaY") != 0 && compound.getInt("HomeAreaZ") != 0) {
             this.setHomePos(new BlockPos(compound.getInt("HomeAreaX"), compound.getInt("HomeAreaY"), compound.getInt("HomeAreaZ")));
        }
        this.setCommand(compound.getInt("Command"));

        if (this.isOrderedToSit()) {
            this.sitProgress = 20.0F;
        }
    }

    public int getVariant() {
        return this.entityData.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public EnumHippogryphTypes getEnumVariant() {
        return EnumHippogryphTypes.values()[this.getVariant()];
    }

    public void setEnumVariant(EnumHippogryphTypes variant) {
        this.setVariant(variant.ordinal());
    }

    public boolean isSaddled() {
        return this.entityData.get(SADDLE).booleanValue();
    }

    public void setSaddled(boolean saddle) {
        this.entityData.set(SADDLE, saddle);
    }

    public boolean isChested() {
        return this.entityData.get(CHESTED).booleanValue();
    }

    public void setChested(boolean chested) {
        this.entityData.set(CHESTED, chested);
        this.hasChestVarChanged = true;
    }

    @Override
    public boolean isOrderedToSit() {
        if (level.isClientSide) {
            boolean isSitting = (this.entityData.get(DATA_FLAGS_ID).byteValue() & 1) != 0;
            this.isSitting = isSitting;
            return isSitting;
        }
        return isSitting;
    }

    @Override
    public void setOrderedToSit(boolean sitting) {
        if (!level.isClientSide) {
            this.isSitting = sitting;
        }
        byte b0 = this.entityData.get(DATA_FLAGS_ID).byteValue();
        if (sitting) {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -2));
        }
    }

    @Override
    public boolean isHovering() {
        if (level.isClientSide) {
            return this.isHovering = this.entityData.get(HOVERING).booleanValue();
        }
        return isHovering;
    }

    public void setHovering(boolean hovering) {
        this.entityData.set(HOVERING, hovering);
        if (!level.isClientSide) {
            this.isHovering = hovering;
        }
    }

    public boolean isRidingPlayer(Player player) {
        return getRidingPlayer() != null && player != null && getRidingPlayer().getUUID().equals(player.getUUID());
    }

    @Override
    @Nullable
    public Player getRidingPlayer() {
        if (this.getControllingPassenger() instanceof Player) {
            return (Player) this.getControllingPassenger();
        }
        return null;
    }

    @Override
    public double getFlightSpeedModifier() {
        return IafConfig.hippogryphFlightSpeedMod * 0.9F;
    }

    @Override
    public boolean isFlying() {
        if (level.isClientSide) {
            return this.isFlying = this.entityData.get(FLYING).booleanValue();
        }
        return isFlying;
    }

    public void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
        if (!level.isClientSide) {
            this.isFlying = flying;
        }
    }

    public int getArmor() {
        return this.entityData.get(ARMOR).intValue();
    }

    public void setArmor(int armorType) {
        this.entityData.set(ARMOR, armorType);
        double armorValue = 0;
        switch (armorType) {
            case 1:
                armorValue = 10;
                break;
            case 2:
                armorValue = 20;
                break;
            case 3:
                armorValue = 30;
        }
        this.getAttribute(Attributes.ARMOR).setBaseValue(armorValue);
    }

    public boolean canMove() {
        return !this.isOrderedToSit() && this.getControllingPassenger() == null && sitProgress == 0;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        this.setEnumVariant(EnumHippogryphTypes.getBiomeType(worldIn.getBiome(this.blockPosition())));
        return data;
    }

    @Override
    public boolean hurt(@NotNull DamageSource dmg, float i) {
        if (this.isVehicle() && dmg.getEntity() != null && this.getControllingPassenger() != null && dmg.getEntity() == this.getControllingPassenger()) {
            return false;
        }
        return super.hurt(dmg, i);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverWorld, @NotNull AgeableMob ageable) {
        return null;
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public void playAmbientSound() {
        if (this.getAnimation() == this.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
        }
        super.playAmbientSound();
    }

    @Override
    protected void playHurtSound(@NotNull DamageSource source) {
        if (this.getAnimation() == this.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
        }
        super.playHurtSound(source);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return IafSoundRegistry.HIPPOGRYPH_IDLE;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
        return IafSoundRegistry.HIPPOGRYPH_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return IafSoundRegistry.HIPPOGRYPH_DIE;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{IAnimatedEntity.NO_ANIMATION, EntityHippogryph.ANIMATION_EAT, EntityHippogryph.ANIMATION_BITE, EntityHippogryph.ANIMATION_SPEAK, EntityHippogryph.ANIMATION_SCRATCH};
    }

    @Deprecated(forRemoval = true)
    public boolean shouldDismountInWater(Entity rider) {
        return true;
    }

    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        return super.canBeRiddenInWater(rider);
    }

    public boolean isDirectPathBetweenPoints(Vec3 vec1, Vec3 vec2) {
        HitResult movingobjectposition = this.level.clip(new ClipContext(vec1, new Vec3(vec2.x, vec2.y + (double) this.getBbHeight() * 0.5D, vec2.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        return movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK;
    }

    public final boolean DISABLE_MOVEMENT_CHECK = true;
    @Override
    public void travel(@NotNull Vec3 pTravelVector) {
        if (!this.canMove() && !this.isVehicle()) {
            super.travel(Vec3.ZERO);
            return;
        }

        // Handle riding movement
        // Reference: AbstractHorse#travel
        if (this.isAlive()) {
            if (this.isVehicle() && this.canBeControlledByRider() && this.isSaddled()) {
                // Approx value for speed tweak
                // Maybe should be put into config
                float walkSpeedFactor = 0.80f;
                float flightSpeedFactor = 0.35F;
                LivingEntity rider = (LivingEntity)this.getControllingPassenger();

                // Mouse controlled yaw
                this.setYRot(rider.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(rider.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;

                float sideway = rider.xxa;
                float forward = rider.zza;
                float vertical = this.isGoingUp() ? 1.0F : this.isGoingDown() ? -1.0F : 0F;

                float speedFactor = 1.0f;
                if (this.isFlying() || this.isHovering()) {
                    speedFactor *= flightSpeedFactor;
                    // Let server know we're flying before they kick us
                    this.setNoGravity(true);
                    this.flyingSpeed = this.getSpeed();
                } else {
                    speedFactor *= walkSpeedFactor;
                    this.setNoGravity(false);
                    // Inherit the vertical movement, e.g. falling movement
                    vertical = (float) pTravelVector.y;
                    // In air moving speed
                    this.flyingSpeed = this.getSpeed() * 0.1F;
                }

                // Faster on sprint
                speedFactor *= rider.isSprinting() ? 1.5f : 1.0f;
                // Slower on going back/sideways
                forward *= forward <= 0f ? 0.25f : 1.0f;
                sideway *= 0.5F;

                if (this.isControlledByLocalInstance()) {
                    this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * speedFactor);
                    super.travel(new Vec3(sideway, vertical, forward));

                    // Vanilla travel has a smaller friction factor for Y axis
                    // Add more friction in case moving too fast on Y axis
                    if (this.isFlying() || this.isHovering()) {
                        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0f, 0.92f, 1.0f));
                    }
                } else if (rider instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                    // Disable server side vehicle movement check, in case of console log spam
                    // Happens when stepping up blocks
                    // Might because client & server's onGround flag is out of sync
                    // I can't get it fixed, so it's disabled
                    this.noPhysics = DISABLE_MOVEMENT_CHECK;
                }

                this.calculateEntityAnimation(this, false);
                this.tryCheckInsideBlocks();
            }
            // Handle non-riding movement
            else {
                this.setNoGravity(this.getAirborneState() != DragonBehaviorUtils.AirborneState.GROUNDED);
                this.noPhysics = false;

                if (this.getAirborneState() != DragonBehaviorUtils.AirborneState.GROUNDED) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1.0f, 0.92f, 1.0f));
                }

                this.flyingSpeed = 0.02F;
                super.travel(pTravelVector);
            }
        } else {
            // Return to defaults
            this.setNoGravity(false);
            this.noPhysics = false;
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
//        boolean flag = pEntity.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (this.getAnimation() != ANIMATION_SCRATCH && this.getAnimation() != ANIMATION_BITE) {
            this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_SCRATCH : ANIMATION_BITE);
        } else {
            return true;
        }
        return false;
    }

    public ItemEntity createEgg(EntityHippogryph partner) {
        int i = Mth.floor(this.getX());
        int j = Mth.floor(this.getY());
        int k = Mth.floor(this.getZ());
        ItemStack stack = new ItemStack(IafItemRegistry.HIPPOGRYPH_EGG.get());
        ItemEntity egg = new ItemEntity(this.level, i, j, k, stack);
        return egg;
    }

    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public void aiStep() {
        super.aiStep();

        //switchNavigator();
        if (level.getDifficulty() == Difficulty.PEACEFUL && this.getTarget() instanceof Player) {
            this.setTarget(null);
        }
        // Server side logic
        if (!this.level.isClientSide) {
            // Stand
            if (this.isOrderedToSit() && (this.getCommand() != 1 || this.getControllingPassenger() != null)) {
                this.setOrderedToSit(false);
            }
            // Sit
            if (!this.isOrderedToSit() && this.getCommand() == 1 && this.getControllingPassenger() == null) {
                this.setOrderedToSit(true);
            }
            if (this.isOrderedToSit()) {
                this.getNavigation().stop();
            }
            if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
                this.heal(1.0F);
            }
        }

        LivingEntity attackTarget = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (this.getAnimation() == ANIMATION_BITE && attackTarget != null && this.getAnimationTick() == 4) {
            double dist = this.distanceToSqr(attackTarget);
            if (dist < this.getMeleeAttackRangeSqr(attackTarget)) {
                attackTarget.hurt(DamageSource.mobAttack(this), ((int) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));
            }
        }
        if (this.getAnimation() == ANIMATION_SCRATCH && attackTarget != null && this.getAnimationTick() == 4) {
            double dist = this.distanceToSqr(attackTarget);

            if (dist < this.getMeleeAttackRangeSqr(attackTarget)) {
                attackTarget.hurt(DamageSource.mobAttack(this), ((int) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));
                attackTarget.hasImpulse = true;
                float f = Mth.sqrt((float) (0.5 * 0.5 + 0.5 * 0.5));
                attackTarget.setDeltaMovement(attackTarget.getDeltaMovement().add(-0.5 / (double) f, 1, -0.5 / (double) f));
                attackTarget.setDeltaMovement(attackTarget.getDeltaMovement().multiply(0.5D, 1, 0.5D));

                if (attackTarget.isOnGround()) {
                    attackTarget.setDeltaMovement(attackTarget.getDeltaMovement().add(0, 0.3, 0));
                }
            }
        }
        if (hasChestVarChanged && hippogryphInventory != null && !this.isChested()) {
            for (int i = 3; i < 18; i++) {
                if (!hippogryphInventory.getItem(i).isEmpty()) {
                    if (!level.isClientSide) {
                        this.spawnAtLocation(hippogryphInventory.getItem(i), 1);
                    }
                    hippogryphInventory.removeItemNoUpdate(i);
                }
            }
            hasChestVarChanged = false;
        }

        if (!this.canMove() && attackTarget != null) {
            this.setTarget(null);
        }
        if (!this.canMove()) {
            this.getNavigation().stop();

        }
        AnimationHandler.INSTANCE.updateAnimations(this);
        boolean sitting = isOrderedToSit() && !isHovering() && !isFlying();
        if (sitting && sitProgress < 20.0F) {
            sitProgress += 0.5F;
        } else if (!sitting && sitProgress > 0.0F) {
            sitProgress -= 0.5F;
        }

        boolean hovering = isHovering();
        if (hovering && hoverProgress < 20.0F) {
            hoverProgress += 0.5F;
        } else if (!hovering && hoverProgress > 0.0F) {
            hoverProgress -= 0.5F;
        }
        boolean flying = this.isFlying() || this.isHovering() && airBorneCounter > 10;
        if (flying && flyProgress < 20.0F) {
            flyProgress += 0.5F;
        } else if (!flying && flyProgress > 0.0F) {
            flyProgress -= 0.5F;
        }
//        if (flying && this.isLandNavigator) {
//            switchNavigator(false);
//        }
//        if (!flying && !this.isLandNavigator) {
//            switchNavigator(true);
//        }
        this.setNoGravity(this.isFlying || this.isHovering);

        if ((flying || hovering) && tickCount % 20 == 0 && this.isOverAir()) {
            this.playSound(IafSoundRegistry.HIPPOGRYPH_FLAP, this.getSoundVolume() * (IafConfig.dragonFlapNoiseDistance / 2), 0.6F + this.random.nextFloat() * 0.6F * this.getVoicePitch());
        }

        if (this.isOrderedToSit()) {
            this.getNavigation().stop();
        }

        if (getTarget() != null && !this.getPassengers().isEmpty() && this.getOwner() != null && this.getPassengers().contains(this.getOwner())) {
            this.setTarget(null);
        }
    }

    public boolean doesWantToLand() {
        return (this.flyTicks > 200 || flyTicks > 40 && this.flyProgress == 0) && !this.isVehicle();
    }

    @Override
    public void tick() {
        super.tick();
        isOverAir = this.isOverAirLogic();
        if (this.isGoingUp()) {
            if (this.airBorneCounter == 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02F, 0));
            }
            if (!this.isFlying() && !this.isHovering()) {
                this.spacebarTicks += 2;
            }
        } else if (this.dismountIAF()) {
//            if (this.isFlying() || this.isHovering()) {
//                this.setAirborneState(DragonBehaviorUtils.AirborneState.GROUNDED);
//            }
        }
        if (this.attack() && this.getControllingPassenger() != null && this.getControllingPassenger() instanceof Player) {

            LivingEntity target = DragonUtils.riderLookingAtEntity(this, (Player) this.getControllingPassenger(), 3);
            if (this.getAnimation() != ANIMATION_BITE && this.getAnimation() != ANIMATION_SCRATCH) {
                this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_SCRATCH : ANIMATION_BITE);
            }
            if (target != null && this.getAnimationTick() >= 10 && this.getAnimationTick() < 13) {
                target.hurt(DamageSource.mobAttack(this), ((int) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));
            }
        }
        if (this.getControllingPassenger() != null && this.getControllingPassenger().isShiftKeyDown()) {
            this.getControllingPassenger().stopRiding();
        }

        double motion = this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z;//Use squared norm2

        if (this.isFlying() && !this.isHovering() && this.getControllingPassenger() != null && this.isOverAir() &&  motion < 0.01F) {
            this.setAirborneState(DragonBehaviorUtils.AirborneState.HOVER);
        }
        if (this.isHovering() && !this.isFlying() && this.getControllingPassenger() != null && this.isOverAir() && motion > 0.01F) {
            this.setAirborneState(DragonBehaviorUtils.AirborneState.FLY);
        }
        if (this.spacebarTicks > 0) {
            this.spacebarTicks--;
        }
        if (this.spacebarTicks > 10 && this.getOwner() != null && this.getPassengers().contains(this.getOwner()) && !this.isFlying() && !this.isHovering()) {
            this.setAirborneState(DragonBehaviorUtils.AirborneState.HOVER);
        }
        if (this.getTarget() != null && this.getVehicle() == null && !this.getTarget().isAlive() || this.getTarget() != null && this.getTarget() instanceof EntityDragonBase && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
    }

    public boolean isTargetBlocked(Vec3 target) {
        if (target != null) {
            HitResult rayTrace = this.level.clip(new ClipContext(this.getEyePosition(1.0F), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            if (rayTrace != null && rayTrace.getLocation() != null) {
                BlockPos pos = new BlockPos(rayTrace.getLocation());
                return !level.isEmptyBlock(pos);
            }
        }
        return false;
    }

    public float getDistanceSquared(Vec3 Vector3d) {
        float f = (float) (this.getX() - Vector3d.x);
        float f1 = (float) (this.getY() - Vector3d.y);
        float f2 = (float) (this.getZ() - Vector3d.z);
        return f * f + f1 * f1 + f2 * f2;
    }

    public boolean setSlot(int inventorySlot, @Nullable ItemStack itemStackIn) {
        int j = inventorySlot - 500 + 2;
        if (j >= 0 && j < this.hippogryphInventory.getContainerSize()) {
            this.hippogryphInventory.setItem(j, itemStackIn);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        if (hippogryphInventory != null && !this.level.isClientSide) {
            for (int i = 0; i < hippogryphInventory.getContainerSize(); ++i) {
                ItemStack itemstack = hippogryphInventory.getItem(i);
                if (!itemstack.isEmpty()) {
                    this.spawnAtLocation(itemstack, 0.0F);
                }
            }
        }
    }

    public void refreshInventory() {
        //This isn't needed (anymore) since it's already being handled by minecraft
        if (!this.level.isClientSide) {
            ItemStack saddle = this.hippogryphInventory.getItem(0);
            ItemStack chest = this.hippogryphInventory.getItem(1);
            this.setSaddled(saddle != null && saddle.getItem() == Items.SADDLE && !saddle.isEmpty());
            this.setChested(chest != null && chest.getItem() == Blocks.CHEST.asItem() && !chest.isEmpty());
            this.setArmor(getIntFromArmor(this.hippogryphInventory.getItem(2)));
        }
        /*if (this.world.isRemote) {
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 0, saddle != null && saddle.getItem() == Items.SADDLE && !saddle.isEmpty() ? 1 : 0));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 1, chest != null && chest.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !chest.isEmpty() ? 1 : 0));
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageHippogryphArmor(this.getEntityId(), 2, this.getIntFromArmor(this.hippogryphInventory.getStackInSlot(2))));
        }*/

    }

    public void switchNavigator(boolean onLand) {
        this.switchNavigator(onLand ? DragonMemoryModuleType.NavigationType.WALK : DragonMemoryModuleType.NavigationType.FLY);
    }

    public void switchNavigator(DragonMemoryModuleType.NavigationType navigationType) {
        if (navigationType == DragonMemoryModuleType.NavigationType.WALK) {
            this.moveControl = new CustomMoveController.BasicMoveControl(this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.CLIMBING);
            this.isLandNavigator = true;
        } else {
            this.moveControl = new EntityHippogryph.FlyMoveHelper(this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.FLYING);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level worldIn) {
        return createNavigator(worldIn, AdvancedPathNavigate.MovementType.CLIMBING);
    }

    protected PathNavigation createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type) {
        return createNavigator(worldIn, type, 2, 2);
    }

    protected PathNavigation createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, float width, float height) {
        AdvancedPathNavigate newNavigator = new AdvancedPathNavigate(this, level, type, width, height);
        this.navigation = newNavigator;
        newNavigator.setCanFloat(true);
        newNavigator.getNodeEvaluator().setCanOpenDoors(true);

        // Fixme: 真的要这个吗？
        newNavigator.getPathingOptions().setCanClimb(false);
        return newNavigator;
    }

    @Override
    public boolean isAlliedTo(@NotNull Entity entityIn) {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (entityIn == livingentity) {
                return true;
            }
            if (entityIn instanceof TamableAnimal) {
                return ((TamableAnimal) entityIn).isOwnedBy(livingentity);
            }
            if (livingentity != null) {
                return livingentity.isAlliedTo(entityIn);
            }
        }

        return super.isAlliedTo(entityIn);
    }

    @Override
    public void onHearFlute(Player player) {
        if (this.isTame() && this.isOwnedBy(player)) {
            if (this.isFlying() || this.isHovering()) {
                this.setFlying(false);
                this.setHovering(false);
            }
        }
    }

    @Override
    public boolean shouldAnimalsFear(Entity entity) {
        return DragonUtils.canTameDragonAttack(this, entity);
    }

    @Override
    public void dropArmor() {
        if (hippogryphInventory != null && !this.level.isClientSide) {
            for (int i = 0; i < hippogryphInventory.getContainerSize(); ++i) {
                ItemStack itemstack = hippogryphInventory.getItem(i);
                if (!itemstack.isEmpty()) {
                    this.spawnAtLocation(itemstack, 0.0F);
                }
            }
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    class FlyMoveHelper extends MoveControl {
        public FlyMoveHelper(EntityHippogryph hippogryph) {
            super(hippogryph);
            this.speedModifier = 1.75F;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (EntityHippogryph.this.horizontalCollision) {
                    EntityHippogryph.this.setYRot(getYRot() + 180.0F);
                    BlockPos target = DragonUtils.getBlockInViewHippogryph(EntityHippogryph.this, 180);
                    this.speedModifier = 0.1F;
                    if (target != null) {
                        this.wantedX = target.getX() + 0.5F;
                        this.wantedY = target.getY() + 0.5F;
                        this.wantedZ = target.getZ() + 0.5F;
                    }
                }
                double d0 = this.wantedX - EntityHippogryph.this.getX();
                double d1 = this.wantedY - EntityHippogryph.this.getY();
                double d2 = this.wantedZ - EntityHippogryph.this.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                d3 = Math.sqrt(d3);

                if (d3 < EntityHippogryph.this.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    EntityHippogryph.this.setDeltaMovement(EntityHippogryph.this.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
                } else {
                    EntityHippogryph.this.setDeltaMovement(EntityHippogryph.this.getDeltaMovement().add(d0 / d3 * 0.1D * this.speedModifier, d1 / d3 * 0.1D * this.speedModifier, d2 / d3 * 0.1D * this.speedModifier));

                    if (EntityHippogryph.this.getTarget() == null) {
                        EntityHippogryph.this.setYRot(-((float) Mth.atan2(EntityHippogryph.this.getDeltaMovement().x, EntityHippogryph.this.getDeltaMovement().z)) * (180F / (float) Math.PI));
                        EntityHippogryph.this.yBodyRot = EntityHippogryph.this.getYRot();
                    } else {
                        double d4 = EntityHippogryph.this.getTarget().getX() - EntityHippogryph.this.getX();
                        double d5 = EntityHippogryph.this.getTarget().getZ() - EntityHippogryph.this.getZ();
                        EntityHippogryph.this.setYRot(-((float) Mth.atan2(d4, d5)) * (180F / (float) Math.PI));
                        EntityHippogryph.this.yBodyRot = EntityHippogryph.this.getYRot();
                    }
                }
            }
        }
    }

    @Deprecated(forRemoval = true)
    class AIFlyRandom extends Goal {
        BlockPos target;

        public AIFlyRandom() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return EntityHippogryph.this.isFlying() && !EntityHippogryph.this.isVehicle();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            if (target == null || !isDirectPathBetweenPoints(EntityHippogryph.this.position(), Vec3.atCenterOf(target)) || EntityHippogryph.this.getDistanceSquared(Vec3.atCenterOf(target)) < 9) {
                if (EntityHippogryph.this.getTarget() != null) {
                    target = EntityHippogryph.this.getTarget().blockPosition();
                } else {
                    target = DragonUtils.getBlockInViewHippogryph(EntityHippogryph.this, 0);
                    if (EntityHippogryph.this.doesWantToLand()) {
                        while (target != null && target.getY() > 3 && EntityHippogryph.this.level.isEmptyBlock(target)) {
                            target = target.below();
                        }
                    }
                }
            }

            if (target != null && (EntityHippogryph.this.doesWantToLand() || EntityHippogryph.this.level.isEmptyBlock(target))) {
                EntityHippogryph.this.getMoveControl().setWantedPosition((double) target.getX() + 0.5D, (double) target.getY() + 0.5D, (double) target.getZ() + 0.5D, 0.75D);
                if (EntityHippogryph.this.getTarget() == null) {
                    EntityHippogryph.this.getLookControl().setLookAt((double) target.getX() + 0.5D, (double) target.getY() + 0.5D, (double) target.getZ() + 0.5D, 180.0F, 20.0F);
                }
            }
        }
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

}
