package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class KeyBinds
{
    public static final IKeyConflictContext RIDING_VEHICLE = new IKeyConflictContext()
    {
        @Override
        public boolean isActive()
        {
            Player player = Minecraft.getInstance().player;
            if(player != null && player.getVehicle() instanceof VehicleEntity)
            {
                return KeyConflictContext.IN_GAME.isActive();
            }
            return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return other == this;
        }
    };

    public static final KeyMapping KEY_HORN = new KeyMapping("key.vehicle.horn", GLFW.GLFW_KEY_H, "key.categories.vehicle");
    public static final KeyMapping KEY_CYCLE_SEATS = new KeyMapping("key.vehicle.cycle_seats", GLFW.GLFW_KEY_C, "key.categories.vehicle");
    public static final KeyMapping KEY_HITCH_TRAILER = new KeyMapping("key.vehicle.hitch_trailer", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.vehicle");
    public static final KeyMapping KEY_DASHBOARD = new KeyMapping("key.vehicle.dashboard", GLFW.GLFW_KEY_G, "key.categories.vehicle");

    static
    {
        KEY_HORN.setKeyConflictContext(RIDING_VEHICLE);
        KEY_CYCLE_SEATS.setKeyConflictContext(RIDING_VEHICLE);
        KEY_HITCH_TRAILER.setKeyConflictContext(RIDING_VEHICLE);
        KEY_DASHBOARD.setKeyConflictContext(RIDING_VEHICLE);
    }

    public static void register()
    {
        ClientRegistry.registerKeyBinding(KEY_HORN);
        ClientRegistry.registerKeyBinding(KEY_CYCLE_SEATS);
        ClientRegistry.registerKeyBinding(KEY_HITCH_TRAILER);
        ClientRegistry.registerKeyBinding(KEY_DASHBOARD);
    }
}
