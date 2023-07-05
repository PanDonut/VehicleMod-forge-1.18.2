package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.framework.api.network.HandshakeMessage;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class HandshakeHandler
{
    private static final Marker VEHICLE_HANDSHAKE = MarkerManager.getMarker("VEHICLE_HANDSHAKE");

    static void handleVehicleProperties(HandshakeMessages.S2CVehicleProperties message, Supplier<NetworkEvent.Context> c)
    {
        VehicleMod.LOGGER.debug(VEHICLE_HANDSHAKE, "Received vehicle properties from server");

        AtomicBoolean updated = new AtomicBoolean(false);
        CountDownLatch block = new CountDownLatch(1);
        c.get().enqueueWork(() ->
        {
            updated.set(VehicleProperties.updateNetworkVehicleProperties(message));
            block.countDown();
        });

        try
        {
            block.await();
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        c.get().setPacketHandled(true);

        if(updated.get())
        {
            VehicleMod.LOGGER.info("Successfully synchronized vehicle properties from server");
            PacketHandler.getHandshakeChannel().reply(new HandshakeMessage.Acknowledge(), c.get());
        }
        else
        {
            VehicleMod.LOGGER.error("Failed to synchronize vehicle properties from server");
            c.get().getNetworkManager().disconnect(new TextComponent("Connection closed - [MrCrayfish's Vehicle Mod] Failed to synchronize vehicle properties from server"));
        }
    }
}