package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageInteractCosmetic extends PlayMessage<MessageInteractCosmetic>
{
    private int entityId;
    private ResourceLocation cosmeticId;

    public MessageInteractCosmetic() {}

    public MessageInteractCosmetic(int entityId, ResourceLocation cosmeticId)
    {
        this.entityId = entityId;
        this.cosmeticId = cosmeticId;
    }

    @Override
    public void encode(MessageInteractCosmetic message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeResourceLocation(message.cosmeticId);
    }

    @Override
    public MessageInteractCosmetic decode(FriendlyByteBuf buffer)
    {
        return new MessageInteractCosmetic(buffer.readInt(), buffer.readResourceLocation());
    }

    @Override
    public void handle(MessageInteractCosmetic message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleInteractCosmeticMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public ResourceLocation getCosmeticId()
    {
        return this.cosmeticId;
    }
}
