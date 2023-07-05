package com.mrcrayfish.vehicle.client.render.complex;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.client.model.IComplexModel;
import com.mrcrayfish.vehicle.client.render.complex.transforms.Rotate;
import com.mrcrayfish.vehicle.client.render.complex.transforms.Transform;
import com.mrcrayfish.vehicle.client.render.complex.transforms.Translate;
import com.mrcrayfish.vehicle.client.render.complex.value.Dynamic;
import com.mrcrayfish.vehicle.client.render.complex.value.Static;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A special type of model that is fully data driven. Complex models can be made up of multiple models
 * with each model having it's own transforms and children. Transforms can take in data from vehicles,
 * such has steering angle, and apply it before rendering the model and it's subsequent children.
 *
 * Author: MrCrayfish
 */
public class ComplexModel
{
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ComplexModel.class, ComplexModel.deserializer())
            .registerTypeAdapter(Translate.class, Translate.deserializer())
            .registerTypeAdapter(Rotate.class, Rotate.deserializer())
            .registerTypeAdapter(Static.class, Static.deserializer())
            .registerTypeAdapter(Dynamic.class, Dynamic.deserializer())
            .create();

    public static JsonDeserializer<ComplexModel> deserializer()
    {
        return (json, type, ctx) -> fromJson(json.getAsJsonObject(), ctx);
    }

    public static ComplexModel fromJson(JsonObject object, JsonDeserializationContext ctx)
    {
        ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(object, "model"));
        List<Transform> transforms = Collections.emptyList();
        if(object.has("transforms") && object.get("transforms").isJsonArray())
        {
            transforms = new ArrayList<>();
            JsonArray transformArray = GsonHelper.getAsJsonArray(object, "transforms");
            for(JsonElement e : transformArray)
            {
                if(!e.isJsonObject()) throw new JsonParseException("Transforms array can only contain objects");
                JsonObject transformObj = e.getAsJsonObject();
                String transformType = GsonHelper.getAsString(transformObj, "type");
                switch (transformType) {
                    case "translate" -> transforms.add(ctx.deserialize(transformObj, Translate.class));
                    case "rotate" -> transforms.add(ctx.deserialize(transformObj, Rotate.class));
                }
            }
        }
        List<ComplexModel> children = Collections.emptyList();
        if(object.has("children") && object.get("children").isJsonArray())
        {
            children = new ArrayList<>();
            JsonArray childrenArray = GsonHelper.getAsJsonArray(object, "children");
            for(JsonElement e : childrenArray)
            {
                if(!e.isJsonObject()) throw new JsonParseException("Children array can only contain objects");
                JsonObject childrenObj = e.getAsJsonObject();
                children.add(ctx.deserialize(childrenObj, ComplexModel.class));
            }
        }
        return new ComplexModel(location, transforms, children.toArray(ComplexModel[]::new));
    }

    @Nullable
    public static ComplexModel load(Minecraft minecraft, IComplexModel model)
    {
        try
        {
            ResourceLocation modelLocation = model.getModelLocation();
            ResourceLocation complexLocation = new ResourceLocation(modelLocation.getNamespace(), "models/" + modelLocation.getPath() + ".complex");
            if(minecraft.getResourceManager().hasResource(complexLocation))
            {
                try(Resource resource = minecraft.getResourceManager().getResource(complexLocation))
                {
                    Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                    return GsonHelper.fromJson(GSON, reader, ComplexModel.class);
                }
            }
        }
        catch(JsonParseException | ResourceLocationException | IOException ex)
        {
            VehicleMod.LOGGER.error("Unable to load complex model", ex);
        }
        return null;
    }

    private final ResourceLocation modelLocation;
    private final List<Transform> transforms;
    private final ComplexModel[] children;
    private BakedModel cachedModel;

    public ComplexModel(ResourceLocation modelLocation, List<Transform> transforms, ComplexModel[] children)
    {
        this.modelLocation = modelLocation;
        this.transforms = ImmutableList.copyOf(transforms);
        this.children = children;
    }

    public void render(VehicleEntity entity, PoseStack matrices, MultiBufferSource buffers, float delta, int color, int light)
    {
        this.transforms.forEach(transform -> transform.apply(entity, matrices, delta));
        RenderUtil.renderColoredModel(this.getModel(), ItemTransforms.TransformType.NONE, false, matrices, buffers, color, light, OverlayTexture.NO_OVERLAY);

        for(int idx = 0; idx < this.children.length; idx++)
        {
            ComplexModel child = this.children[idx];

            matrices.pushPose();
            {
                child.render(entity, matrices, buffers, delta, color, light);
            }
            matrices.popPose();
        }
    }

    public final BakedModel getModel()
    {
        if(this.cachedModel == null)
        {
            this.cachedModel = Minecraft.getInstance().getModelManager().getModel(this.modelLocation);
        }
        return this.cachedModel;
    }

    public List<Transform> getTransforms()
    {
        return this.transforms;
    }

    public ComplexModel[] getChildren()
    {
        return this.children;
    }
}
