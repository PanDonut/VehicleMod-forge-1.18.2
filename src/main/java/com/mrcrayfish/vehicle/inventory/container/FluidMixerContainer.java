package com.mrcrayfish.vehicle.inventory.container;

import com.mrcrayfish.vehicle.init.ModContainers;
import com.mrcrayfish.vehicle.inventory.container.slot.FuelSlot;
import com.mrcrayfish.vehicle.tileentity.FluidMixerTileEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class FluidMixerContainer extends AbstractContainerMenu
{
    private int extractionProgress;
    private int remainingFuel;
    private int maxFuelProgress;
    private int blazeLevel;
    private int enderSapLevel;
    private int fueliumLevel;

    private final FluidMixerTileEntity fluidExtractor;

    public FluidMixerContainer(int windowId, Inventory playerInventory, FluidMixerTileEntity fluidExtractor)
    {
        super(ModContainers.FLUID_MIXER.get(), windowId);
        this.fluidExtractor = fluidExtractor;

        this.addSlot(new FuelSlot(fluidExtractor, 0, 9, 50));
        this.addSlot(new Slot(fluidExtractor, 1, 103, 41));

        for(int x = 0; x < 3; x++)
        {
            for(int y = 0; y < 9; y++)
            {
                this.addSlot(new Slot(playerInventory, y + x * 9 + 9, 8 + y * 18, 98 + x * 18));
            }
        }

        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 156));
        }

        this.addDataSlots(fluidExtractor.getFluidMixerData());
    }

    public FluidMixerTileEntity getFluidExtractor()
    {
        return fluidExtractor;
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn)
    {
        return true;
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player playerIn, int index)
    {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();

            if(index == 0 || index == 1)
            {
                if(!this.moveItemStackTo(slotStack, 2, 38, true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                if(this.fluidExtractor.canPlaceItem(1, slotStack))
                {
                    if(!this.moveItemStackTo(slotStack, 1, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(ForgeHooks.getBurnTime(slotStack, null) > 0)
                {
                    if(!this.moveItemStackTo(slotStack, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(index < 29)
                {
                    if(!this.moveItemStackTo(slotStack, 29, 38, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(index < 38 && !this.moveItemStackTo(slotStack, 2, 29, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if(slotStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            if(slotStack.getCount() == stack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return stack;
    }
}
