package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class OffRoaderEntity extends LandVehicleEntity
{
    public OffRoaderEntity(EntityType<? extends OffRoaderEntity> type, Level worldIn)
    {
        super(type, worldIn);
    }
}
