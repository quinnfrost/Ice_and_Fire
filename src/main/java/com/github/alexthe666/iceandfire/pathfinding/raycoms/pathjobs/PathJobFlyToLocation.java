package com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.MNode;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathPointExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PathJobFlyToLocation extends PathJobMoveToLocation{


    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world  world the entity is in.
     * @param start  starting location.
     * @param end    target location.
     * @param range  max search range.
     * @param entity the entity.
     */
    public PathJobFlyToLocation(Level world, BlockPos start, BlockPos end, int range, LivingEntity entity) {
        super(world, start, end, range, entity);
    }

    // Todo: if an old path is available/in progress, try start search from the end of last path
    @Nullable
    @Override
    protected Path search() {
        List<Node> nodes = List.of(new PathPointExtended(
                start
        ), new PathPointExtended(
                end
        ));

        nodes.forEach(node -> {

        });

        Path path = new Path(nodes, end, false);

        return path;
    }
}
