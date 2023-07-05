package com.mrcrayfish.vehicle.client.render.complex.value;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.PlaneEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public record Dynamic(Source source, boolean inverse, float scale) implements IValue
{
    public static JsonDeserializer<Dynamic> deserializer()
    {
        return (json, type, ctx) -> fromJson(json);
    }

    public static Dynamic fromJson(JsonElement json)
    {
        if(json.isJsonObject())
        {
            JsonObject object = json.getAsJsonObject();
            Source source = Source.fromKey(GsonHelper.getAsString(object, "source"));
            if(source == null) throw new JsonParseException("Invalid source: " + GsonHelper.getAsString(object, "source"));
            boolean inverse = GsonHelper.getAsBoolean(object, "inverse", false);
            float scale = GsonHelper.getAsFloat(object, "scale", 1.0F);
            return new Dynamic(source, inverse, scale);
        }
        throw new JsonParseException("Dynamic values must be object");
    }

    public double getValue(VehicleEntity entity, float partialTicks)
    {
        double value = this.source.valueFunction.apply(entity, partialTicks);
        value *= this.scale;
        return this.inverse ? -value : value;
    }

    // TODO remove hardcoded nature and allow externals to register custom sources
    public enum Source
    {
        PLANE_AILERON("plane_aileron", (vehicle, partialTicks) -> {
            return vehicle instanceof PlaneEntity ? (double) ((PlaneEntity) vehicle).getFlapAngle(partialTicks) : 0.0;
        }),
        PLANE_PROPELLER("plane_propeller", (vehicle, partialTicks) -> {
            return vehicle instanceof PlaneEntity ? (double) ((PlaneEntity) vehicle).getPropellerRotation(partialTicks) : 0.0;
        }),
        PLANE_ELEVATOR("plane_elevator", (vehicle, partialTicks) -> {
            return vehicle instanceof PlaneEntity ? (double) ((PlaneEntity) vehicle).getElevatorAngle(partialTicks) : 0.0;
        }),
        HELICOPTER_BLADES("helicopter_blades", (vehicle, partialTicks) -> {
            return vehicle instanceof HelicopterEntity ? (double) ((HelicopterEntity) vehicle).getBladeRotation(partialTicks) : 0.0;
        }),
        HELICOPTER_FORWARDS("helicopter_forwards", (vehicle, partialTicks) -> {
            return vehicle instanceof HelicopterEntity ? (double) ((HelicopterEntity) vehicle).getForwards(partialTicks) : 0.0;
        }),
        HELICOPTER_SIDEWARDS("helicopter_sidewards", (vehicle, partialTicks) -> {
            return vehicle instanceof HelicopterEntity ? (double) ((HelicopterEntity) vehicle).getSidewards(partialTicks) : 0.0;
        });

        private final String key;
        private final BiFunction<VehicleEntity, Float, Double> valueFunction;

        Source(String key, BiFunction<VehicleEntity, Float, Double> valueFunction)
        {
            this.key = key;
            this.valueFunction = valueFunction;
        }

        public String getKey()
        {
            return this.key;
        }

        @Nullable
        public static Source fromKey(@Nullable String key)
        {
            return Arrays.stream(values()).filter(axis -> axis.key.equals(key)).findFirst().orElse(null);
        }
    }
}
