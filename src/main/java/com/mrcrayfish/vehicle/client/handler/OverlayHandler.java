package com.mrcrayfish.vehicle.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class OverlayHandler
{
    private List<Component> stats = new ArrayList<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        this.stats.clear();

        if(!Config.CLIENT.enabledSpeedometer.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(!mc.isWindowActive() || mc.options.hideGui)
            return;

        Player player = mc.player;
        if(player == null)
            return;

        Entity entity = player.getVehicle();
        if(!(entity instanceof PoweredVehicleEntity vehicle))
            return;

        DecimalFormat format = new DecimalFormat("0.00");
        this.addStat("BPS", format.format(vehicle.getSpeed()));

        if(vehicle.requiresEnergy())
        {
            String fuel = format.format(vehicle.getCurrentEnergy()) + "/" + format.format(vehicle.getEnergyCapacity());
            this.addStat("Fuel", fuel);
        }

        if(!FMLLoader.isProduction())
        {
            if(vehicle instanceof LandVehicleEntity landVehicle)
            {
                String traction = format.format(landVehicle.getTraction());
                this.addStat("Traction", traction);

                Vec3 forward = Vec3.directionFromRotation(landVehicle.getRotationVector());
                float side = (float) landVehicle.getVelocity().normalize().cross(forward.normalize()).length();
                String sideString = format.format(side);
                this.addStat("Side", sideString);
            }
        }
    }

    private void addStat(String label, String value)
    {
        this.stats.add(
                new TextComponent(label + ": ")
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.RESET)
                        .append(new TextComponent(value)
                                .withStyle(ChatFormatting.YELLOW)));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        PoseStack stack = new PoseStack();
        Minecraft mc = Minecraft.getInstance();
        for(int i = 0; i < this.stats.size(); i++)
        {
            mc.font.drawShadow(stack, this.stats.get(i), 10, 10 + 15 * i, 0xFFFFFF);
        }
    }
}
