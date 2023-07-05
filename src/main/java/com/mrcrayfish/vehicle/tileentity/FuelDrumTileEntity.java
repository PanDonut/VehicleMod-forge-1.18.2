package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Author: MrCrayfish
 */
public class FuelDrumTileEntity extends TileFluidHandlerSynced
{
    public FuelDrumTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.FUEL_DRUM.get(), ModBlocks.FUEL_DRUM.get().getCapacity(), pos, state);
    }

    public FuelDrumTileEntity(BlockEntityType<?> tileEntityType, int capacity, BlockPos pos, BlockState state)
    {
        super(tileEntityType, capacity, pos, state);
    }

    public boolean hasFluid()
    {
        return !this.tank.getFluid().isEmpty();
    }

    public int getAmount()
    {
        return this.tank.getFluidAmount();
    }

    public int getCapacity()
    {
        return this.tank.getCapacity();
    }
}
