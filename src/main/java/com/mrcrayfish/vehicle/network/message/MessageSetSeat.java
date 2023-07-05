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
public class MessageSetSeat extends PlayMessage<MessageSetSeat>
{
    private int index;

    public MessageSetSeat() {}

    public MessageSetSeat(int index)
    {
        this.index = index;
    }

    @Override
    public void encode(MessageSetSeat message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.index);
    }

    @Override
    public MessageSetSeat decode(FriendlyByteBuf buffer)
    {
        return new MessageSetSeat(buffer.readInt());
    }

    @Override
    public void handle(MessageSetSeat message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleSetSeatMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getIndex()
    {
        return this.index;
    }
}
