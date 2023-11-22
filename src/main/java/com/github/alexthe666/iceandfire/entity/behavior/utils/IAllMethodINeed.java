package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public interface IAllMethodINeed {
    boolean isOverAirLogic();

    boolean isFlying();
    boolean isHovering();
    void setFlying(boolean flying);
    void setHovering(boolean hovering);
    void switchNavigator(boolean fly);

    void setAirborneState(DragonBehaviorUtils.AirborneState state);
    DragonBehaviorUtils.AirborneState getAirborneState();
    void takeoff();
    void land();
    void hover();
    void walkTo(WalkTarget walkTarget);
    void flightTo(WalkTarget walkTarget);
    void hoverAt(WalkTarget walkTarget);

    boolean canMove();
    default boolean canLand() {
        return canMove();
    }
    default boolean canFly() {
        return canMove();
    }

    int getCommand();

}
