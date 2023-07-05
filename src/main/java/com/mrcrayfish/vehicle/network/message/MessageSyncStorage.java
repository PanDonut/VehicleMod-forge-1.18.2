package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageSyncStorage extends PlayMessage<MessageSyncStorage>
{
    private int entityId;
    private String[] keys;
    private CompoundTag[] tags;

    public MessageSyncStorage() {}

    public <T extends VehicleEntity & IStorage> MessageSyncStorage(T vehicle, String ... keys)
    {
        this.entityId = vehicle.getId();
        this.keys = keys;
        List<Pair<String, CompoundTag>> tagList = new ArrayList<>();
        for(String key : keys)
        {
            StorageInventory inventory = vehicle.getStorageInventory(key);
            if(inventory != null)
            {
                CompoundTag tag = new CompoundTag();
                tag.put("Inventory", inventory.createTag());
                tagList.add(Pair.of(key, tag));
            }
        }
        this.keys = new String[tagList.size()];
        this.tags = new CompoundTag[tagList.size()];
        for(int i = 0; i < tagList.size(); i++)
        {
            Pair<String, CompoundTag> pair = tagList.get(i);
            this.keys[i] = pair.getLeft();
            this.tags[i] = pair.getRight();
        }
    }

    private MessageSyncStorage(int entityId, String[] keys, CompoundTag[] tags)
    {
        this.entityId = entityId;
        this.keys = keys;
        this.tags = tags;
    }

    @Override
    public void encode(MessageSyncStorage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.keys.length);
        for(int i = 0; i < message.keys.length; i++)
        {
            buffer.writeUtf(message.keys[i]);
            buffer.writeNbt(message.tags[i]);
        }
    }

    @Override
    public MessageSyncStorage decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        int keyLength = buffer.readInt();
        String[] keys = new String[keyLength];
        CompoundTag[] tags = new CompoundTag[keyLength];
        for(int i = 0; i < keyLength; i++)
        {
            keys[i] = buffer.readUtf();
            tags[i] = buffer.readNbt();
        }
        return new MessageSyncStorage(entityId, keys, tags);
    }

    @Override
    public void handle(MessageSyncStorage message, Supplier<NetworkEvent.Context> supplier)
    {
        IMessage.enqueueTask(supplier, () -> ClientPlayHandler.handleSyncStorage(message));
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public String[] getKeys()
    {
        return this.keys;
    }

    public CompoundTag[] getTags()
    {
        return this.tags;
    }
}
