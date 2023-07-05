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
public class MessageAttachTrailer extends PlayMessage<MessageAttachTrailer>
{
    private int trailerId;

    public MessageAttachTrailer() {}

    public MessageAttachTrailer(int trailerId)
    {
        this.trailerId = trailerId;
    }

    @Override
    public void encode(MessageAttachTrailer message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.trailerId);
    }

    @Override
    public MessageAttachTrailer decode(FriendlyByteBuf buffer)
    {
        return new MessageAttachTrailer(buffer.readInt());
    }

    @Override
    public void handle(MessageAttachTrailer message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleAttachTrailerMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getTrailerId()
    {
        return this.trailerId;
    }
}
