package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.framework.api.network.FrameworkChannelBuilder;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.network.message.*;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler
{
    private static final SimpleChannel HANDSHAKE_CHANNEL = FrameworkChannelBuilder
            .create(Reference.MOD_ID, "handshake", 1)
            .registerHandshakeMessage(HandshakeMessages.S2CVehicleProperties.class)
            .build();

    private static final SimpleChannel PLAY_CHANNEL = FrameworkChannelBuilder
            .create(Reference.MOD_ID, "play", 1)
            .registerPlayMessage(MessageTurnAngle.class)
            .registerPlayMessage(MessageHandbrake.class)
            .registerPlayMessage(MessageHorn.class)
            .registerPlayMessage(MessageThrowVehicle.class)
            .registerPlayMessage(MessagePickupVehicle.class)
            .registerPlayMessage(MessageAttachChest.class)
            .registerPlayMessage(MessageAttachTrailer.class)
            .registerPlayMessage(MessageFuelVehicle.class)
            .registerPlayMessage(MessageInteractKey.class)
            .registerPlayMessage(MessageHelicopterInput.class)
            .registerPlayMessage(MessageCraftVehicle.class)
            .registerPlayMessage(MessageHitchTrailer.class)
            .registerPlayMessage(MessageSyncStorage.class)
            .registerPlayMessage(MessageOpenStorage.class)
            .registerPlayMessage(MessageThrottle.class)
            .registerPlayMessage(MessageEntityFluid.class)
            .registerPlayMessage(MessageSyncPlayerSeat.class, NetworkDirection.PLAY_TO_CLIENT)
            .registerPlayMessage(MessageCycleSeats.class, NetworkDirection.PLAY_TO_SERVER)
            .registerPlayMessage(MessageSetSeat.class)
            .registerPlayMessage(MessageSyncHeldVehicle.class, NetworkDirection.PLAY_TO_CLIENT)
            .registerPlayMessage(MessagePlaneInput.class)
            .registerPlayMessage(MessageSyncCosmetics.class, NetworkDirection.PLAY_TO_CLIENT)
            .registerPlayMessage(MessageInteractCosmetic.class)
            .registerPlayMessage(MessageSyncActionData.class, NetworkDirection.PLAY_TO_CLIENT)
            .build();

    public static void init()
    {}

    /**
     * Gets the handshake network channel for MrCrayfish's Vehicle Mod
     */
    public static SimpleChannel getHandshakeChannel()
    {
        return HANDSHAKE_CHANNEL;
    }

    /**
     * Gets the play network channel for MrCrayfish's Vehicle Mod
     */
    public static SimpleChannel getPlayChannel()
    {
        return PLAY_CHANNEL;
    }
}
