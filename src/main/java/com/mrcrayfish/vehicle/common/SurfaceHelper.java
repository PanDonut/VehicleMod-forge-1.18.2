package com.mrcrayfish.vehicle.common;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.IWheelType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.world.level.material.Material.*;

/**
 * Categories materials into a surface type to determine the
 * Author: MrCrayfish
 */
public class SurfaceHelper
{
    private static final ImmutableMap<Material, SurfaceType> MATERIAL_TO_SURFACE_TYPE;

    static
    {
        ImmutableMap.Builder<Material, SurfaceType> builder = new ImmutableMap.Builder<>();
        builder.put(CLOTH_DECORATION, SurfaceType.DIRT);
        builder.put(PLANT, SurfaceType.DIRT);
        builder.put(WATER_PLANT, SurfaceType.DIRT);
        builder.put(TOP_SNOW, SurfaceType.SNOW);
        builder.put(CLAY, SurfaceType.DIRT);
        builder.put(DIRT, SurfaceType.DIRT);
        builder.put(GRASS, SurfaceType.DIRT);
        builder.put(ICE_SOLID, SurfaceType.ICE);
        builder.put(SAND, SurfaceType.DIRT);
        builder.put(SPONGE, SurfaceType.DIRT);
        builder.put(SHULKER_SHELL, SurfaceType.SOLID);
        builder.put(WOOD, SurfaceType.SOLID);
        builder.put(NETHER_WOOD, SurfaceType.SOLID);
        builder.put(BAMBOO, SurfaceType.SOLID);
        builder.put(WOOL, SurfaceType.DIRT);
        builder.put(EXPLOSIVE, SurfaceType.SNOW);
        builder.put(LEAVES, SurfaceType.SNOW);
        builder.put(GLASS, SurfaceType.SOLID);
        builder.put(ICE, SurfaceType.ICE);
        builder.put(CACTUS, SurfaceType.SNOW);
        builder.put(STONE, SurfaceType.SOLID);
        builder.put(METAL, SurfaceType.SOLID);
        builder.put(SNOW, SurfaceType.SNOW);
        builder.put(HEAVY_METAL, SurfaceType.SOLID);
        builder.put(BARRIER, SurfaceType.SOLID);
        builder.put(PISTON, SurfaceType.SOLID);
        builder.put(MOSS, SurfaceType.SNOW);
        builder.put(CAKE, SurfaceType.SNOW);
        MATERIAL_TO_SURFACE_TYPE = builder.build();
    }

    public static SurfaceType getSurfaceTypeForMaterial(Material material)
    {
        return MATERIAL_TO_SURFACE_TYPE.getOrDefault(material, SurfaceType.NONE);
    }

    private static float getValue(PoweredVehicleEntity vehicle, BiFunction<IWheelType, SurfaceType, Float> function, float defaultValue)
    {
        VehicleProperties properties = vehicle.getProperties();
        List<Wheel> wheels = properties.getWheels();
        if(!vehicle.hasWheelStack() || wheels.isEmpty())
            return defaultValue;

        Optional<IWheelType> optional = vehicle.getWheelType();
        if(optional.isEmpty())
            return defaultValue;

        int wheelCount = 0;
        float surfaceModifier = 0F;
        double[] wheelPositions = vehicle.getWheelPositions();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for(int i = 0; i < wheels.size(); i++)
        {
            double wheelX = wheelPositions[i * 3];
            double wheelY = wheelPositions[i * 3 + 1];
            double wheelZ = wheelPositions[i * 3 + 2];

            int x = Mth.floor(vehicle.getX() + wheelX);
            int y = Mth.floor(vehicle.getY() + wheelY - 0.2D);
            int z = Mth.floor(vehicle.getZ() + wheelZ);

            BlockState state = vehicle.level.getBlockState(mpos.set(x, y, z));
            SurfaceType surfaceType = getSurfaceTypeForMaterial(state.getMaterial());
            if(surfaceType == SurfaceType.NONE)
                continue;
            IWheelType wheelType = optional.get();
            surfaceModifier += function.apply(wheelType, surfaceType);
            wheelCount++;
        }
        return surfaceModifier / Math.max(1F, wheelCount);
    }

    public static float getFriction(PoweredVehicleEntity vehicle)
    {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.friction * surfaceType.frictionFunction.apply(wheelType), 0.0F);
    }

    public static float getSurfaceTraction(PoweredVehicleEntity vehicle, float original)
    {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.tractionFactor, 1.0F) * original;
    }

    public record SurfaceType(Function<IWheelType, Float> frictionFunction, float friction, float tractionFactor)
    {
        public static final SurfaceType SOLID = new SurfaceType(IWheelType::getRoadFrictionFactor, 0.9F, 1.0F);
        public static final SurfaceType DIRT = new SurfaceType(IWheelType::getDirtFrictionFactor, 1.1F, 0.9F);
        public static final SurfaceType SNOW = new SurfaceType(IWheelType::getSnowFrictionFactor, 1.5F, 0.9F);
        public static final SurfaceType ICE = new SurfaceType(type -> 1F, 1.5F, 0.01F);
        public static final SurfaceType NONE = new SurfaceType(type -> 0F, 1.0F, 1.0F);
    }
}
