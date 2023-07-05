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
public class MessageAttachChest extends PlayMessage<MessageAttachChest>
{
    private int entityId;
    private String key;

    public MessageAttachChest() {}

    public MessageAttachChest(int entityId, String key)
    {
        this.entityId = entityId;
        this.key = key;
    }

    @Override
    public void encode(MessageAttachChest message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeUtf(message.key);
    }

    @Override
    public MessageAttachChest decode(FriendlyByteBuf buffer)
    {
        return new MessageAttachChest(buffer.readInt(), buffer.readUtf());
    }

    @Override
    public void handle(MessageAttachChest message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleAttachChestMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public String getKey()
    {
        return this.key;
    }
}
