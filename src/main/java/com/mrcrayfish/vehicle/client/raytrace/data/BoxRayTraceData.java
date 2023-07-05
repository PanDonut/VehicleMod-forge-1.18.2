package com.mrcrayfish.vehicle.client.raytrace.data;

import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.raytrace.ITriangleList;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.Triangle;
import com.mrcrayfish.vehicle.client.raytrace.TriangleList;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class BoxRayTraceData extends RayTraceData
{
    private final AABB box;

    public BoxRayTraceData(AABB box)
    {
        this(box, null);
    }

    public BoxRayTraceData(AABB box, @Nullable RayTraceFunction function)
    {
        super(function);
        this.box = box;
    }

    public AABB getBox()
    {
        return this.box;
    }

    @Override
    public ITriangleList createTriangleList()
    {
        ITriangleList triangleList = EntityRayTracer.boxToTriangles(this.getBox());
        List<Triangle> transformedTriangles = new ArrayList<>();
        for(Triangle triangle : triangleList.getTriangles())
        {
            transformedTriangles.add(new Triangle(EntityRayTracer.getTransformedTriangle(triangle.vertices(), this.matrix)));
        }
        return new TriangleList(transformedTriangles);
    }
}
