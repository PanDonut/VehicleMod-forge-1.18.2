package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractTrailerRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.trailer.SeederTrailerEntity;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SeederTrailerRenderer extends AbstractTrailerRenderer<SeederTrailerEntity>
{
    protected final PropertyFunction<SeederTrailerEntity, StorageInventory> storageProperty = new PropertyFunction<>(SeederTrailerEntity::getInventory, null);

    public SeederTrailerRenderer(EntityType<SeederTrailerEntity> type, VehicleProperties defaultProperties)
    {
        super(type, defaultProperties);
    }

    @Override
    public void render(@Nullable SeederTrailerEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        //Render the body
        this.renderDamagedPart(vehicle, VehicleModels.SEEDER_TRAILER, matrixStack, renderTypeBuffer, light, partialTicks);

        StorageInventory inventory = this.storageProperty.get(vehicle);
        if(inventory != null)
        {
            int layer = 0;
            int index = 0;
            for(int i = 0; i < inventory.getContainerSize(); i++)
            {
                ItemStack stack = inventory.getItem(i);
                if(!stack.isEmpty())
                {
                    matrixStack.pushPose();
                    {
                        matrixStack.translate(-10.5 * 0.0625, -3 * 0.0625, -2 * 0.0625);
                        matrixStack.scale(0.45F, 0.45F, 0.45F);

                        int count = Math.max(1, stack.getCount() / 16);
                        int width = 4;
                        int maxLayerCount = 8;
                        for(int j = 0; j < count; j++)
                        {
                            matrixStack.pushPose();
                            {
                                int layerIndex = index % maxLayerCount;
                                //double yOffset = Math.sin(Math.PI * (((layerIndex + 0.5) % (double) width) / (double) width)) * 0.1;
                                //GlStateManager.translate(0, yOffset * ((double) layer / inventory.getSizeInventory()), 0);
                                matrixStack.translate(0, layer * 0.05, 0);
                                matrixStack.translate((layerIndex % width) * 0.75, 0, (float) (layerIndex / width) * 0.5);
                                matrixStack.translate(0.7 * (layer % 2), 0, 0);
                                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(90F));
                                matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees(47F * index));
                                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(2F * layerIndex));
                                matrixStack.translate(layer * 0.001, layer * 0.001, layer * 0.001); // Fixes Z fighting
                                MINECRAFT.getItemRenderer().renderStatic(stack,
                                        ItemTransforms.TransformType.NONE,
                                        light,
                                        OverlayTexture.NO_OVERLAY,
                                        matrixStack,
                                        renderTypeBuffer,
                                        vehicle.getId()
                                        );
                            }
                            matrixStack.popPose();
                            index++;
                            if(index % maxLayerCount == 0)
                            {
                                layer++;
                            }
                        }
                    }
                    matrixStack.popPose();
                }
            }
        }

        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, -12.0F * 0.0625F, partialTicks, light);
        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, -8.0F * 0.0625F, partialTicks, light);
        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, -4.0F * 0.0625F, partialTicks, light);
        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, 0.0F, partialTicks, light);
        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, 4.0F * 0.0625F, partialTicks, light);
        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, 8.0F * 0.0625F, partialTicks, light);
        this.renderSpike(vehicle, matrixStack, renderTypeBuffer, 12.0F * 0.0625F, partialTicks, light);
    }

    private void renderSpike(SeederTrailerEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, double offsetX, float partialTicks, int light)
    {
        matrixStack.pushPose();
        matrixStack.translate(offsetX, -0.65, 0.0);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-this.getWheelRotation(vehicle, null, partialTicks)));
        matrixStack.scale(0.75F, 0.75F, 0.75F);
        RenderUtil.renderColoredModel(VehicleModels.SEED_SPIKER.getBaseModel(), ItemTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.SEEDER_TRAILER, parts, transforms);
        };
    }
}
