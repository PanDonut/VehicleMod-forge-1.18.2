package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.tileentity.FluidExtractorTileEntity;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public record FluidExtractorRecipe(ResourceLocation id, ItemStack ingredient, FluidEntry result) implements Recipe<FluidExtractorTileEntity>
{
    @Override
    public boolean matches(FluidExtractorTileEntity fluidExtractor, @NotNull Level level)
    {
        ItemStack source = fluidExtractor.getItem(FluidExtractorTileEntity.SLOT_FLUID_SOURCE);
        return InventoryUtil.areItemStacksEqualIgnoreCount(source, this.ingredient);
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull FluidExtractorTileEntity inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    @NotNull
    public ItemStack getResultItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.FLUID_EXTRACTOR.get();
    }

    @Override
    @NotNull
    public RecipeType<?> getType()
    {
        return RecipeTypes.FLUID_EXTRACTOR;
    }
}
