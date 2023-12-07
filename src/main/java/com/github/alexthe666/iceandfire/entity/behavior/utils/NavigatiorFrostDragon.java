package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathingStuckHandler;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * Take care of walking, flying, and hovering navigation <br>
 *
 */
public class NavigatiorFrostDragon extends AdvancedPathNavigate {
    public NavigatiorFrostDragon(Mob entity, Level world, MovementType type, float width, float height, PathingStuckHandler stuckHandler) {
        super(entity, world, type, width, height, stuckHandler);
    }

    public NavigatiorFrostDragon(Mob entity, Level world, MovementType type, float width, float height) {
        super(entity, world, type, width, height);
    }
}
