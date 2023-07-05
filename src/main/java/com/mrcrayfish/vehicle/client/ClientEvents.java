package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.vehicle.client.screen.DashboardScreen;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class ClientEvents
{
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null)
            return;

        if(event.getAction() != GLFW.GLFW_PRESS)
            return;

        if(KeyBinds.KEY_DASHBOARD.isDown() && mc.player != null && mc.player.getVehicle() instanceof VehicleEntity)
        {
            mc.setScreen(new DashboardScreen(null, (VehicleEntity) mc.player.getVehicle()));
        }
        else if(FMLLoader.isProduction() && event.getKey() == GLFW.GLFW_KEY_RIGHT_BRACKET)
        {
            VehicleProperties.loadDefaultProperties();
        }
    }
}
