package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class GoKartEntity extends LandVehicleEntity
{
    public GoKartEntity(EntityType<? extends GoKartEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.maxUpStep = 0.625F;
    }


    @Override
    public boolean shouldRenderFuelPort()
    {
        return false;
    }
}
