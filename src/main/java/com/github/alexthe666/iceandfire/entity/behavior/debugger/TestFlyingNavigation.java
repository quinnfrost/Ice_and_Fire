package com.github.alexthe666.iceandfire.entity.behavior.debugger;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TestFlyingNavigation extends FlyingPathNavigation {
    public TestFlyingNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
    }

}
