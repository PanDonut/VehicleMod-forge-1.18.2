package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageSyncActionData extends PlayMessage<MessageSyncActionData>
{
    private int entityId;
    private ResourceLocation cosmeticId;
    private List<Pair<ResourceLocation, CompoundTag>> actionData;

    public MessageSyncActionData() {}

    public MessageSyncActionData(int entityId, ResourceLocation cosmeticId, List<Pair<ResourceLocation, CompoundTag>> actionData)
    {
        this.entityId = entityId;
        this.cosmeticId = cosmeticId;
        this.actionData = actionData;
    }

    @Override
    public void encode(MessageSyncActionData message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeResourceLocation(message.cosmeticId);
        buffer.writeInt(message.actionData.size());

        message.actionData.forEach(pair -> {
            buffer.writeResourceLocation(pair.getLeft());
            buffer.writeNbt(pair.getRight());
        });
    }

    @Override
    public MessageSyncActionData decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        ResourceLocation cosmeticId = buffer.readResourceLocation();

        List<Pair<ResourceLocation, CompoundTag>> actionData = new ArrayList<>();
        int size = buffer.readInt();

        for(int i = 0; i < size; i++)
        {
            ResourceLocation actionId = buffer.readResourceLocation();
            CompoundTag data = buffer.readNbt();
            actionData.add(Pair.of(actionId, data));
        }
        return new MessageSyncActionData(entityId, cosmeticId, actionData);
    }

    @Override
    public void handle(MessageSyncActionData message, Supplier<NetworkEvent.Context> supplier)
    {
        IMessage.enqueueTask(supplier, () -> ClientPlayHandler.handleSyncActionData(message));
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public ResourceLocation getCosmeticId()
    {
        return this.cosmeticId;
    }

    public List<Pair<ResourceLocation, CompoundTag>> getActionData()
    {
        return this.actionData;
    }
}
