package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageEntityFluid extends PlayMessage<MessageEntityFluid>
{
    private int entityId;
    private FluidStack stack;

    public MessageEntityFluid() {}

    public MessageEntityFluid(int entityId, FluidStack stack)
    {
        this.entityId = entityId;
        this.stack = stack;
    }

    @Override
    public void encode(MessageEntityFluid message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.stack.writeToNBT(new CompoundTag()));
    }

    @Override
    public MessageEntityFluid decode(FriendlyByteBuf buffer)
    {
        return new MessageEntityFluid(buffer.readInt(), FluidStack.loadFluidStackFromNBT(buffer.readNbt()));
    }

    @Override
    public void handle(MessageEntityFluid message, Supplier<NetworkEvent.Context> supplier)
    {
        IMessage.enqueueTask(supplier, () -> ClientPlayHandler.handleEntityFluid(message));
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public FluidStack getStack()
    {
        return this.stack;
    }
}
