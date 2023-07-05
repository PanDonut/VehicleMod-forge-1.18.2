package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageHelicopterInput extends PlayMessage<MessageHelicopterInput>
{
	private float lift;
	private float forward;
	private float side;

	public MessageHelicopterInput() {}

	public MessageHelicopterInput(float lift, float forward, float side)
	{
		this.lift = lift;
		this.forward = forward;
		this.side = side;
	}

	@Override
	public void encode(MessageHelicopterInput message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.lift);
		buffer.writeFloat(message.forward);
		buffer.writeFloat(message.side);
	}

	@Override
	public MessageHelicopterInput decode(FriendlyByteBuf buffer)
	{
		return new MessageHelicopterInput(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
	}

	@Override
	public void handle(MessageHelicopterInput message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleHelicopterInputMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getLift()
	{
		return this.lift;
	}

	public float getForward()
	{
		return this.forward;
	}

	public float getSide()
	{
		return this.side;
	}
}
