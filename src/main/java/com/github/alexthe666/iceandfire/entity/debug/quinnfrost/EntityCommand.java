package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageCommandEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityCommand {
    // 成员变量现在不是 final 的，因为它们在构造后仍可被修改
    public final Player issuer;
    public final CommandType commandType;
    public List<PathfinderMob> commandEntities = new ArrayList<>();
    public Vec3 pos;
    public List<LivingEntity> targetEntities = new ArrayList<>();

    public enum CommandType {
        SIGNAL,
        MOVE,
        ATTACK,
        FORCE_POS,
        STOP,
        LENGTH
    }

    // 构造函数接收必需的参数
    public EntityCommand(CommandType commandType, Player issuer) {
        this.commandType = commandType;
        this.issuer = issuer;
    }

    // 流式接口方法，用于设置可选参数并返回 this
    public EntityCommand withCommandEntity(Entity entity) {
        if (entity != null) {
            DebugUtils.getDebuggableTarget(entity).ifPresent(this.commandEntities::add);
        }
        return this; // 返回当前实例以支持链式调用
    }

    public EntityCommand withTargetEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            this.targetEntities.add(livingEntity);
        } else if (entity != null) {
            DebugUtils.getDebuggableTarget(entity).ifPresent(this.targetEntities::add);
        }
        return this;
    }

    public EntityCommand atPosition(Vec3 pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public String toString() {
        return "EntityCommand{" +
                "issuer=" + issuer.getDisplayName().getString() +
                ", commandType=" + commandType +
                ", commandEntities=" + commandEntities +
                ", pos=" + pos +
                ", targetEntities=" + targetEntities +
                '}';
    }

    public boolean issue() {
        if (issuer.level().isClientSide) {
            return false;
        }

        ServerPlayer player = (ServerPlayer) issuer;
        switch (commandType) {
            case SIGNAL -> commandEntities.forEach(commandEntity -> {
                IceAndFire.sendMSGToPlayer(MessageCommandEntity.getSignalMessage(commandEntity), player);
            });
            case MOVE -> commandEntities.forEach(commandEntity -> {
                setMoveTo(commandEntity, pos);
            });
            case ATTACK -> commandEntities.forEach(commandEntity -> {
                targetEntities.forEach(targetEntity -> {
                    setAttackTarget(commandEntity, targetEntity);
                });
            });
            case FORCE_POS -> commandEntities.forEach(commandEntity -> {
                setPos(commandEntity, pos);
            });
            case STOP -> commandEntities.forEach(commandEntity -> {
                setMoveTo(commandEntity, null);
                setAttackTarget(commandEntity, null);
            });
            default -> {
                return false;
            }
        }
        return true;
    }

    public static void setMoveTo(@Nonnull PathfinderMob commandEntity, @Nullable Vec3 pos) {
        if (pos != null) {
            commandEntity.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0);
            if (DebugUtils.hasMemoryItem(commandEntity)) {
                commandEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 1.0f, 0));
            }
            if (commandEntity instanceof EntityDragonBase dragon && (dragon.isFlying() || dragon.isHovering())) {
                dragon.flightManager.setFlightTarget(pos);
            }
        } else {
            commandEntity.getNavigation().stop();
            if (DebugUtils.hasMemoryItem(commandEntity)) {
                commandEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }
            if (commandEntity instanceof EntityDragonBase dragon && (dragon.isFlying() || dragon.isHovering())) {
                dragon.flightManager.setFlightTarget(null);
            }
        }
    }

    public static void setAttackTarget(@Nonnull PathfinderMob commandEntity, @Nullable LivingEntity targetEntity) {
        if (targetEntity != null && !targetEntity.equals(commandEntity)) {
            if (DebugUtils.hasMemoryItem(commandEntity)) {
                commandEntity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, targetEntity);
            }
            commandEntity.setTarget(targetEntity);
        } else {
            if (DebugUtils.hasMemoryItem(commandEntity)) {
                commandEntity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            commandEntity.setTarget(null);
        }
    }

    public static void setPos(@Nonnull Entity entity, @Nonnull Vec3 pos) {
        if (pos != null) {
            entity.setPos(pos.x, pos.y + 0.1, pos.z);
        }
    }
}
