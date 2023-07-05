package com.mrcrayfish.vehicle.crafting;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Author: MrCrayfish
 */
public class RecipeTypes
{
    public static final RecipeType<FluidExtractorRecipe> FLUID_EXTRACTOR = register("vehicle:fluid_extractor");
    public static final RecipeType<FluidMixerRecipe> FLUID_MIXER = register("vehicle:fluid_mixer");
    public static final RecipeType<WorkstationRecipe> WORKSTATION = register("vehicle:workstation");

    static <T extends Recipe<?>> RecipeType<T> register(final String key)
    {
        return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(key), new RecipeType<T>()
        {
            @Override
            public String toString()
            {
                return key;
            }
        });
    }

    // Does nothing, just forces static fields to initialize
    public static void init() {}
}
