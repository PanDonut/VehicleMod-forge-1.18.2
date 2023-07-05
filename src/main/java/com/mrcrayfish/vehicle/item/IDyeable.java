package com.mrcrayfish.vehicle.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public interface IDyeable
{
    String NBT_KEY = "color";

    default boolean hasColor(ItemStack stack)
    {
        CompoundTag compound = stack.getTag();
        return compound != null && compound.contains(NBT_KEY, Tag.TAG_INT);
    }

    default int getColor(ItemStack stack)
    {
        CompoundTag compound = stack.getTag();
        return compound != null ? compound.getInt(NBT_KEY) : -1;
    }

    default void setColor(ItemStack stack, int color)
    {
        CompoundTag compound = stack.getOrCreateTag();
        compound.putInt(NBT_KEY, color);
    }

    static ItemStack dyeStack(ItemStack stack, List<DyeItem> dyes)
    {
        ItemStack resultStack = ItemStack.EMPTY;
        int[] combinedColors = new int[3];
        int maxColor = 0;
        int colorCount = 0;
        IDyeable dyeable = null;
        if(stack.getItem() instanceof IDyeable)
        {
            dyeable = (IDyeable) stack.getItem();
            resultStack = stack.copy();
            resultStack.setCount(1);
            if(dyeable.hasColor(stack))
            {
                int color = dyeable.getColor(resultStack);
                float red = (float) (color >> 16 & 255) / 255.0F;
                float green = (float) (color >> 8 & 255) / 255.0F;
                float blue = (float) (color & 255) / 255.0F;
                maxColor = (int) ((float) maxColor + Math.max(red, Math.max(green, blue)) * 255.0F);
                combinedColors[0] = (int) ((float) combinedColors[0] + red * 255.0F);
                combinedColors[1] = (int) ((float) combinedColors[1] + green * 255.0F);
                combinedColors[2] = (int) ((float) combinedColors[2] + blue * 255.0F);
                colorCount++;
            }

            for(DyeItem dyeitem : dyes)
            {
                float[] colorComponents = dyeitem.getDyeColor().getTextureDiffuseColors();
                int red = (int) (colorComponents[0] * 255.0F);
                int green = (int) (colorComponents[1] * 255.0F);
                int blue = (int) (colorComponents[2] * 255.0F);
                maxColor += Math.max(red, Math.max(green, blue));
                combinedColors[0] += red;
                combinedColors[1] += green;
                combinedColors[2] += blue;
                colorCount++;
            }
        }

        if(dyeable == null)
        {
            return ItemStack.EMPTY;
        }
        else
        {
            int red = combinedColors[0] / colorCount;
            int green = combinedColors[1] / colorCount;
            int blue = combinedColors[2] / colorCount;
            float averageColor = (float) maxColor / (float) colorCount;
            float maxValue = (float) Math.max(red, Math.max(green, blue));
            red = (int) ((float) red * averageColor / maxValue);
            green = (int) ((float) green * averageColor / maxValue);
            blue = (int) ((float) blue * averageColor / maxValue);
            int finalColor = (red << 8) + green;
            finalColor = (finalColor << 8) + blue;
            dyeable.setColor(resultStack, finalColor);
            return resultStack;
        }
    }

    static int getColorFromStack(ItemStack stack)
    {
        if(stack.getItem() instanceof IDyeable)
        {
            return ((IDyeable) stack.getItem()).getColor(stack);
        }
        return -1;
    }
}
