package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageThrowVehicle extends PlayMessage<MessageThrowVehicle>
{
    @Override
    public void encode(MessageThrowVehicle message, FriendlyByteBuf buffer) {}

    @Override
    public MessageThrowVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageThrowVehicle();
    }

    @Override
    public void handle(MessageThrowVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleThrowVehicle(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }
}
