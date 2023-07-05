package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageCraftVehicle extends PlayMessage<MessageCraftVehicle>
{
    private String vehicleId;

    public MessageCraftVehicle() {}

    public MessageCraftVehicle(String vehicleId)
    {
        this.vehicleId = vehicleId;
    }

    @Override
    public void encode(MessageCraftVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(message.vehicleId, 128);
    }

    @Override
    public MessageCraftVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageCraftVehicle(buffer.readUtf(128));
    }

    @Override
    public void handle(MessageCraftVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleCraftVehicleMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public String getVehicleId()
    {
        return this.vehicleId;
    }
}
