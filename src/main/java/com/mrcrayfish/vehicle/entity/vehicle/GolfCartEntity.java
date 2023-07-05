package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class GolfCartEntity extends HelicopterEntity
{
    public GolfCartEntity(EntityType<? extends GolfCartEntity> type, Level worldIn)
    {
        super(type, worldIn);
        //TODO figure out electric vehicles
    }
}
