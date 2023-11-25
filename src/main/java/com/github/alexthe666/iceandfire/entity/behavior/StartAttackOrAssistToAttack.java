package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class StartAttackOrAssistToAttack<E extends TamableAnimal & IBehaviorApplicable> extends StartAttacking<E> {
    private final Predicate<E> canAssistAttackPredicate;
    private final Function<E, Optional<? extends LivingEntity>> assistTargetFinderFunction;
    private Optional<? extends LivingEntity> assistTargetOptional;

    public StartAttackOrAssistToAttack(Predicate<E> canAttackPredicate, Function<E, Optional<? extends LivingEntity>> targetFinderFunction,
                                       Predicate<E> canAssistAttackPredicate, Function<E, Optional<? extends LivingEntity>> assistTargetFinderFunction) {
        super(canAttackPredicate, targetFinderFunction);
        this.canAssistAttackPredicate = canAssistAttackPredicate;
        this.assistTargetFinderFunction = assistTargetFinderFunction;
        this.assistTargetOptional = Optional.empty();
    }

    public StartAttackOrAssistToAttack(Function<E, Optional<? extends LivingEntity>> targetFinderFunction, Function<E, Optional<? extends LivingEntity>> assistTargetFinderFunction) {
        this(e -> true, targetFinderFunction, e -> true, assistTargetFinderFunction);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        if (!this.canAssistAttackPredicate.test(pOwner)) {
            return false;
        } else if (pOwner.isTame()) {
            this.assistTargetOptional = this.assistTargetFinderFunction.apply(pOwner);
            return this.assistTargetOptional.isPresent() ? pOwner.wantsToAttack(this.assistTargetOptional.get(),
                    pOwner.getOwner()) : false;
        } else {
            return super.checkExtraStartConditions(pLevel, pOwner);
        }
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (this.assistTargetOptional.isPresent()) {
            // super.setAttackTarget
            LivingChangeTargetEvent changeTargetEvent = ForgeHooks.onLivingChangeTarget(pEntity,
                    this.assistTargetOptional.get(),
                    LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
            if (!changeTargetEvent.isCanceled()) {
                pEntity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, changeTargetEvent.getNewTarget());
                pEntity.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                ForgeHooks.onLivingSetAttackTarget(pEntity,
                        changeTargetEvent.getNewTarget(),
                        LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET); // TODO: Remove in 1.20
            }
        } else {
            super.start(pLevel, pEntity, pGameTime);
        }
    }
}
