package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.Reference;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public enum EngineType implements IEngineType
{
    NONE(new ResourceLocation(Reference.MOD_ID, "none")),
    SMALL_MOTOR(new ResourceLocation(Reference.MOD_ID, "small_motor")),
    LARGE_MOTOR(new ResourceLocation(Reference.MOD_ID, "large_motor")),
    ELECTRIC_MOTOR(new ResourceLocation(Reference.MOD_ID, "electric_motor"));

    private final ResourceLocation id;

    EngineType(ResourceLocation id)
    {
        this.id = id;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }
}
