package com.mrcrayfish.vehicle.common.cosmetic;

import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.common.cosmetic.actions.OpenableAction;
import com.mrcrayfish.vehicle.common.cosmetic.actions.RotateAction;
import com.mrcrayfish.vehicle.util.Axis;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class CosmeticActions
{
    private static final Map<ResourceLocation, Function<JsonObject, ?>> ACTIONS = new HashMap<>();
    private static final Map<Class<?>, ResourceLocation> CLASS_TO_ID = new HashMap<>();

    static
    {
        register(new ResourceLocation(Reference.MOD_ID, "openable"), OpenableAction.class, object -> {
            JsonObject rotation = object.getAsJsonObject("rotation");
            Axis axis = Axis.fromKey(GsonHelper.getAsString(rotation, "axis", "x"));
            float angle = GsonHelper.getAsFloat(rotation, "angle", 0F);
            int animationLength = GsonHelper.getAsInt(rotation, "animationLength", 12);
            JsonObject sound = object.getAsJsonObject("sound");
            ResourceLocation openSound = ExtraJSONUtils.getAsResourceLocation(sound, "open", null);
            ResourceLocation closeSound = ExtraJSONUtils.getAsResourceLocation(sound, "close", null);
            return () -> new OpenableAction(axis, angle, openSound, closeSound, animationLength);
        });
        register(new ResourceLocation(Reference.MOD_ID, "rotate"), RotateAction.class, RotateAction::createSupplier);
    }

    public static <A extends Action> void register(ResourceLocation id, Class<A> clazz, Function<JsonObject, Supplier<A>> deserializer)
    {
        ACTIONS.putIfAbsent(id, deserializer);
        CLASS_TO_ID.putIfAbsent(clazz, id);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static Supplier<Action> getSupplier(ResourceLocation id, JsonObject object)
    {
        Function<JsonObject, Supplier<Action>> actionFunction = (Function<JsonObject, Supplier<Action>>) ACTIONS.get(id);
        return actionFunction != null ? actionFunction.apply(object) : null;
    }

    public static <A extends Action> ResourceLocation getId(Class<A> clazz)
    {
        if(!CLASS_TO_ID.containsKey(clazz))
            throw new IllegalArgumentException("Tried to get id for cosmetic action that doesn't exist!");
        return CLASS_TO_ID.get(clazz);
    }
}
