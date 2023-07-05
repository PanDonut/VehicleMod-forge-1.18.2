package com.mrcrayfish.vehicle.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Author: MrCrayfish
 */
public class Bounds
{
    public double x1, y1, z1;
    public double x2, y2, z2;

    public Bounds(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public Bounds(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        this.x1 = x1 * 0.0625;
        this.y1 = y1 * 0.0625;
        this.z1 = z1 * 0.0625;
        this.x2 = x2 * 0.0625;
        this.y2 = y2 * 0.0625;
        this.z2 = z2 * 0.0625;
    }

    public AABB toAABB()
    {
        return new AABB(x1, y1, z1, x2, y2, z2);
    }

    public VoxelShape getRotation(Direction facing)
    {
        return CollisionHelper.getBlockBounds(facing, this);
    }

    public VoxelShape[] getRotatedBounds()
    {
        VoxelShape boundsNorth = CollisionHelper.getBlockBounds(Direction.NORTH, this);
        VoxelShape boundsEast = CollisionHelper.getBlockBounds(Direction.EAST, this);
        VoxelShape boundsSouth = CollisionHelper.getBlockBounds(Direction.SOUTH, this);
        VoxelShape boundsWest = CollisionHelper.getBlockBounds(Direction.WEST, this);
        return new VoxelShape[] { boundsSouth, boundsWest, boundsNorth, boundsEast };
    }
}
