package com.mrcrayfish.vehicle.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionHelper
{
    public static VoxelShape getBlockBounds(Direction facing, double x1, double y1, double z1, double x2, double y2, double z2)
    {
        double[] bounds = fixRotation(facing, x1, z1, x2, z2);
        return Shapes.create(bounds[0], y1, bounds[1], bounds[2], y2, bounds[3]);
    }

    public static VoxelShape getBlockBounds(Direction facing, Bounds bounds)
    {
        double[] fixedBounds = fixRotation(facing, bounds.x1, bounds.z1, bounds.x2, bounds.z2);
        return Shapes.create(fixedBounds[0], bounds.y1, fixedBounds[1], fixedBounds[2], bounds.y2, fixedBounds[3]);
    }

    public static double[] fixRotation(Direction facing, double x1, double z1, double x2, double z2)
    {
        switch (facing) {
            case WEST -> {
                double origX1 = x1;
                x1 = 1.0F - x2;
                double origZ1 = z1;
                z1 = 1.0F - z2;
                x2 = 1.0F - origX1;
                z2 = 1.0F - origZ1;
            }
            case NORTH -> {
                double origX1 = x1;
                x1 = z1;
                z1 = 1.0F - x2;
                x2 = z2;
                z2 = 1.0F - origX1;
            }
            case SOUTH -> {
                double origX1 = x1;
                x1 = 1.0F - z2;
                double origZ1 = z1;
                z1 = origX1;
                double origX2 = x2;
                x2 = 1.0F - origZ1;
                z2 = origX2;
            }
            default -> {
            }
        }
        return new double[] { x1, z1, x2, z2 };
    }
}
