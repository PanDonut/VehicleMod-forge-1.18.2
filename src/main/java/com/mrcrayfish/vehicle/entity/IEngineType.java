package com.mrcrayfish.vehicle.entity;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public interface IEngineType
{
    ResourceLocation getId();

    int hashCode();

    default Component getEngineName()
    {
        return new TranslatableComponent(this.getId().getNamespace() + ".engine_type." + this.getId().getPath() + ".name");
    }
}
