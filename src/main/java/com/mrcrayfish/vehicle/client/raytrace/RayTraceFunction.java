package com.mrcrayfish.vehicle.client.raytrace;

import com.mrcrayfish.framework.common.data.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.handler.ControllerHandler;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.item.JerryCanItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageFuelVehicle;
import com.mrcrayfish.vehicle.tileentity.GasPumpTankTileEntity;
import com.mrcrayfish.vehicle.tileentity.GasPumpTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public interface RayTraceFunction
{
    @Nullable
    InteractionHand apply(EntityRayTracer rayTracer, VehicleRayTraceResult result, Player player);

    /**
     * Checks if fuel can be transferred from a jerry can to a powered vehicle, and sends a packet to do so every other tick, if it can
     *
     * @return whether or not fueling can continue
     */
    RayTraceFunction FUNCTION_FUELING = (rayTracer, result, player) ->
    {
        Entity entity = result.getEntity();
        if(!(entity instanceof PoweredVehicleEntity poweredVehicle))
            return null;

        if(!poweredVehicle.requiresEnergy() || poweredVehicle.getCurrentEnergy() >= poweredVehicle.getEnergyCapacity())
            return null;

        gasPump: if(SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).isPresent() && ControllerHandler.isRightClicking())
        {
            BlockPos pos = SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).get();
            BlockEntity tileEntity = player.level.getBlockEntity(pos);
            if(!(tileEntity instanceof GasPumpTileEntity))
                break gasPump;

            tileEntity = player.level.getBlockEntity(pos.below());
            if(!(tileEntity instanceof GasPumpTankTileEntity gasPumpTank))
                break gasPump;

            FluidTank tank = gasPumpTank.getFluidTank();
            FluidStack stack = tank.getFluid();
            if(stack.isEmpty() || !Config.SERVER.validFuels.get().contains(stack.getFluid().getRegistryName().toString()))
                break gasPump;

            if(rayTracer.getContinuousInteractionTickCounter() % 2 == 0)
            {
                PacketHandler.getPlayChannel().sendToServer(new MessageFuelVehicle(result.getEntity().getId(), InteractionHand.MAIN_HAND));
            }
            return InteractionHand.MAIN_HAND;
        }

        for(InteractionHand hand : InteractionHand.values())
        {
            ItemStack stack = player.getItemInHand(hand);
            if(stack.isEmpty() || !(stack.getItem() instanceof JerryCanItem) || !ControllerHandler.isRightClicking())
                continue;

            Optional<IFluidHandlerItem> optional = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve();
            if(!optional.isPresent())
                continue;

            IFluidHandlerItem handler = optional.get();
            FluidStack fluidStack = handler.getFluidInTank(0);
            if(fluidStack.isEmpty() || !Config.SERVER.validFuels.get().contains(fluidStack.getFluid().getRegistryName().toString()))
                continue;

            if(rayTracer.getContinuousInteractionTickCounter() % 2 == 0)
            {
                PacketHandler.getPlayChannel().sendToServer(new MessageFuelVehicle(entity.getId(), hand));
            }
            return hand;
        }
        return null;
    };
}
