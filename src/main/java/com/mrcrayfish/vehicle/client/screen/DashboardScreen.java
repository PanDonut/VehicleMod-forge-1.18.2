package com.mrcrayfish.vehicle.client.screen;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.screen.toolbar.AbstractToolbarScreen;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.DoorButton;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.IconButton;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.SeatButton;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.common.cosmetic.actions.OpenableAction;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class DashboardScreen extends AbstractToolbarScreen
{
    protected WeakReference<VehicleEntity> vehicleRef;

    public DashboardScreen(@Nullable Screen parent, VehicleEntity vehicle)
    {
        super(new TextComponent("Dashboard"), parent);
        this.vehicleRef = new WeakReference<>(vehicle);
    }

    private DashboardScreen(@Nullable Screen parent, Component title, VehicleEntity vehicle)
    {
        super(title, parent);
        this.vehicleRef = new WeakReference<>(vehicle);
    }

    @Override
    protected void loadWidgets(List<AbstractWidget> widgets)
    {
        widgets.add(new IconButton(20, 20, Icons.LEFT_DOOR, new TranslatableComponent("vehicle.toolbar.label.doors"), onPress -> {
            this.minecraft.setScreen(new DoorScreen(this, this.vehicleRef.get()));
        }));
        widgets.add(new IconButton(20, 20, Icons.SEAT_PASSENGER, new TranslatableComponent("vehicle.toolbar.label.seats"), onPress -> {
            this.minecraft.setScreen(new SeatScreen(this, this.vehicleRef.get()));
        }));
    }

    private static class DoorScreen extends DashboardScreen
    {
        private DoorScreen(@Nullable Screen parent, VehicleEntity vehicle)
        {
            super(parent, new TextComponent("Doors"), vehicle);
        }

        @Override
        protected void loadWidgets(List<AbstractWidget> widgets)
        {
            VehicleEntity vehicle = this.vehicleRef.get();
            if(vehicle != null)
            {
                VehicleProperties properties = vehicle.getProperties();
                properties.getCosmetics().forEach((cosmeticId, cosmeticProperties) -> {
                    vehicle.getCosmeticTracker().getSelectedCosmeticEntry(cosmeticId)
                        .flatMap(entry -> entry.getActions().stream().filter(action -> action instanceof OpenableAction).findAny())
                        .ifPresent(action -> widgets.add(new DoorButton(vehicle, cosmeticProperties, (OpenableAction) action)));
                });
            }
        }
    }

    private static class SeatScreen extends DashboardScreen
    {
        private SeatScreen(@Nullable Screen parent, VehicleEntity vehicle)
        {
            super(parent, new TextComponent("Seats"), vehicle);
        }

        @Override
        protected void loadWidgets(List<AbstractWidget> widgets)
        {
            VehicleEntity vehicle = this.vehicleRef.get();
            if(vehicle != null)
            {
                VehicleProperties properties = vehicle.getProperties();
                for(int i = 0; i < properties.getSeats().size(); i++)
                {
                    Seat seat = properties.getSeats().get(i);
                    widgets.add(new SeatButton(vehicle, i, seat.isDriver()));
                }
            }
        }
    }

    public enum Icons implements IconButton.IconProvider
    {
        BACK,
        LEFT_DOOR,
        RIGHT_DOOR,
        HOOD,
        TRUNK,
        SEAT_PASSENGER,
        SEAT_DRIVER;

        private static final ResourceLocation ICON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/icons.png");

        @Override
        public ResourceLocation getTextureLocation()
        {
            return ICON_TEXTURE;
        }

        @Override
        public int getU()
        {
            return (this.ordinal() % 10) * 10;
        }

        @Override
        public int getV()
        {
            return (this.ordinal() / 10) * 10;
        }
    }
}
