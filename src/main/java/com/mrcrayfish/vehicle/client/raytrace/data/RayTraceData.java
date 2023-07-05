package com.mrcrayfish.vehicle.client.raytrace.data;

import com.mojang.math.Matrix4f;
import com.mrcrayfish.vehicle.client.raytrace.ITriangleList;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;

import javax.annotation.Nullable;

/**
 * A raytrace part representing either an item or a box
 */
public abstract class RayTraceData
{
    @Nullable
    private final RayTraceFunction function;
    @Nullable
    private ITriangleList triangles = null;
    protected Matrix4f matrix;

    public RayTraceData(@Nullable RayTraceFunction function)
    {
        this.function = function;
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        this.matrix = matrix;
    }

    @Nullable
    public final RayTraceFunction getRayTraceFunction()
    {
        return this.function;
    }

    public void setMatrix(Matrix4f matrix)
    {
        this.matrix = matrix;
    }

    @Nullable
    public final ITriangleList getTriangleList()
    {
        if(this.triangles == null)
        {
            this.triangles = this.createTriangleList();
        }
        return this.triangles;
    }

    protected abstract ITriangleList createTriangleList();

    public final void clearTriangles()
    {
        this.triangles = null;
    }
}
