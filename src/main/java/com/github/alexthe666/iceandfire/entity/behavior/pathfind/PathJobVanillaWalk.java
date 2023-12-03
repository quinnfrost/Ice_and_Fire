package com.github.alexthe666.iceandfire.entity.behavior.pathfind;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.MNode;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class PathJobVanillaWalk extends AbstractPathJob {
    public PathJobVanillaWalk(Level world, BlockPos start, BlockPos end, int range, LivingEntity entity) {
        super(world, start, end, range, entity);
    }

    @Override
    protected double computeHeuristic(BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean isAtDestination(MNode n) {
        return false;
    }

    @Override
    protected double getNodeResultScore(MNode n) {
        return 0;
    }
}
