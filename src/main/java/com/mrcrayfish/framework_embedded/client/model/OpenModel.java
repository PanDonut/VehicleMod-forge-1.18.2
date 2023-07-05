package com.mrcrayfish.framework_embedded.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Open Model format from Framework. Allows larger models and removes rotation step restriction.
 *
 * Author: MrCrayfish
 */
public class OpenModel implements IModelGeometry<OpenModel>
{
    private final BlockModel model;

    public OpenModel(BlockModel model)
    {
        this.model = model;
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        return this.model.bake(bakery, this.model, spriteGetter, modelTransform, modelLocation, true);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
    {
        return this.model.getMaterials(modelGetter, missingTextureErrors);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Loader implements IModelLoader<OpenModel>
    {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {}

        @Override
        public OpenModel read(@NotNull JsonDeserializationContext context, @NotNull JsonObject object)
        {
            return new OpenModel(Deserializer.INSTANCE.deserialize(object, BlockModel.class, context));
        }

        @SubscribeEvent
        public static void onModelRegister(ModelRegistryEvent event)
        {
            ModelLoaderRegistry.registerLoader(new ResourceLocation("framework", "open_model"), new Loader());
        }
    }

    public static class Deserializer extends BlockModel.Deserializer
    {
        private static final BlockElement.Deserializer BLOCK_PART_DESERIALIZER = new BlockElement.Deserializer();
        private static final Deserializer INSTANCE = new Deserializer();

        /**
         * Reads the bl
         */
        @Override
        protected List<BlockElement> getElements(JsonDeserializationContext context, JsonObject object)
        {
            try
            {
                List<BlockElement> list = new ArrayList<>();

                JsonArray array = object.getAsJsonArray("components");

                for(int idx = 0; idx < array.size(); idx++)
                {
                    list.add(this.readBlockElement(array.get(idx), context));
                }

                return list;
            }
            catch(Exception e)
            {
                throw new JsonParseException(e);
            }
        }

        /**
         * Reads a block element without restrictions on the size and rotation angle.
         */
        private BlockElement readBlockElement(JsonElement element, JsonDeserializationContext context)
        {
            JsonObject object = element.getAsJsonObject();

            // Get copy of custom size and angle properties
            Vector3f from = ExtraJSONUtils.getAsVector3f(object, "from");
            Vector3f to = ExtraJSONUtils.getAsVector3f(object, "to");
            JsonObject rotation = GsonHelper.getAsJsonObject(object, "rotation", new JsonObject());
            float angle = GsonHelper.getAsFloat(rotation, "angle", 0F);

            // Make valid for vanilla block element deserializer
            JsonArray zero = new JsonArray();
            zero.add(0F);
            zero.add(0F);
            zero.add(0F);
            object.add("from", zero);
            object.add("to", zero);
            rotation.addProperty("angle", 0F);

            // Read vanilla element and construct new element with custom properties
            BlockElement e = BLOCK_PART_DESERIALIZER.deserialize(element, BlockElement.class, context);
            BlockElementRotation r = e.rotation != null ? new BlockElementRotation(e.rotation.origin, e.rotation.axis, angle, e.rotation.rescale) : null;
            return new BlockElement(from, to, e.faces, r, e.shade);
        }
    }
}
