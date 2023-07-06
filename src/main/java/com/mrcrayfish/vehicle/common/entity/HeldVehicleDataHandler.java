package com.mrcrayfish.vehicle.common.entity;

import com.mrcrayfish.framework_embedded.common.data.SyncedEntityData;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncHeldVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Author: MrCrayfish
 */
public class HeldVehicleDataHandler
{
    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new HeldVehicleDataHandler());
    }

    public static boolean isHoldingVehicle(Player player)
    {
        return player.getFirstPassenger() instanceof VehicleEntity;
    }

    public static VehicleEntity getHeldVehicle(Player player)
    {
        if (player.getFirstPassenger() instanceof VehicleEntity vehicle) {
            return vehicle;
        }
        return null;
    }

    public static void setHeldVehicle(Player player, VehicleEntity vehicle)
    {
        vehicle.startRiding(player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.isWasDeath())
            return;

        VehicleEntity vehicle = getHeldVehicle(event.getOriginal());
        if(vehicle != null)
        {
            setHeldVehicle(event.getPlayer(), vehicle);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        if(event.getTarget() instanceof Player player)
        {
            VehicleEntity vehicle = getHeldVehicle(player);
        }
    }

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Player player && !event.getWorld().isClientSide)
        {
            VehicleEntity vehicle = getHeldVehicle(player);
        }
    }
}
