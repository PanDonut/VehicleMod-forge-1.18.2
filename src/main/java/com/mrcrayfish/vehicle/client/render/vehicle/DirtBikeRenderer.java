package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractMotorcycleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.DirtBikeEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class DirtBikeRenderer extends AbstractMotorcycleRenderer<DirtBikeEntity>
{
    public DirtBikeRenderer(EntityType<DirtBikeEntity> type, VehicleProperties defaultProperties)
    {
        super(type, defaultProperties);
    }

    @Override
    protected void render(@Nullable DirtBikeEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.DIRT_BIKE_BODY, matrixStack, renderTypeBuffer, light, partialTicks);

        //Render the handles bars
        matrixStack.pushPose();

        matrixStack.translate(0.0, 0.0, 10.5 * 0.0625);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-22.5F));

        float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(vehicle).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F;
        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(steeringWheelRotation));

        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(22.5F));
        matrixStack.translate(0.0, 0.0, -10.5 * 0.0625);

        this.renderDamagedPart(vehicle, VehicleModels.DIRT_BIKE_HANDLES, matrixStack, renderTypeBuffer, light, partialTicks);

        ItemStack wheelStack = this.wheelStackProperty.get(vehicle);
        if(!wheelStack.isEmpty())
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            Wheel wheel = properties.getFirstFrontWheel();
            if(wheel != null)
            {
                matrixStack.pushPose();
                matrixStack.translate(0, -0.5, 0);
                matrixStack.translate(wheel.getOffsetX() * 0.0625, wheel.getOffsetY() * 0.0625, wheel.getOffsetZ() * 0.0625);
                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-this.getWheelRotation(vehicle, wheel, partialTicks)));
                matrixStack.scale(wheel.getScaleX(), wheel.getScaleY(), wheel.getScaleZ());
                matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(180F));
                RenderUtil.renderColoredModel(RenderUtil.getModel(wheelStack), ItemTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
                matrixStack.popPose();
            }
        }

        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(DirtBikeEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        int index = entity.getSeatTracker().getSeatIndex(player.getUUID());
        if(index == 0)
        {
            float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
            float maxSteeringAngle = this.vehiclePropertiesProperty.get(entity).getExtended(PoweredProperties.class).getMaxSteeringAngle();
            float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F;
            model.rightArm.xRot = (float) Math.toRadians(-55F - steeringWheelRotation);
            model.leftArm.xRot = (float) Math.toRadians(-55F + steeringWheelRotation);
        }
        else if(index == 1)
        {
            model.rightArm.xRot = (float) Math.toRadians(-45F);
            model.rightArm.zRot = (float) Math.toRadians(-10F);
            model.leftArm.xRot = (float) Math.toRadians(-45F);
            model.leftArm.zRot = (float) Math.toRadians(10F);
        }

        model.rightLeg.xRot = (float) Math.toRadians(-45F);
        model.rightLeg.yRot = (float) Math.toRadians(30F);
        model.leftLeg.xRot = (float) Math.toRadians(-45F);
        model.leftLeg.yRot = (float) Math.toRadians(-30F);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (entityRayTracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.DIRT_BIKE_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.DIRT_BIKE_HANDLES, parts, transforms);
            TransformHelper.createFuelFillerTransforms(ModEntities.DIRT_BIKE.get(), VehicleModels.SMALL_FUEL_DOOR_CLOSED, parts, transforms);
        };
    }
}
