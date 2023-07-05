package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractMotorcycleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.model.ChestModel;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.MopedEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class MopedRenderer extends AbstractMotorcycleRenderer<MopedEntity>
{
    private final ChestModel chestModel;
    protected final PropertyFunction<MopedEntity, Boolean> hasChestProperty = new PropertyFunction<>((Function<MopedEntity, Boolean>) MopedEntity::hasChest, false);
    protected final PropertyFunction<MopedEntity, Float> openProgressProperty = new PropertyFunction<>(MopedEntity::getOpenProgress, 0F);
    protected final PropertyFunction<MopedEntity, Float> prevOpenProgressProperty = new PropertyFunction<>(MopedEntity::getPrevOpenProgress, 0F);

    public MopedRenderer(EntityType<MopedEntity> type, VehicleProperties properties)
    {
        super(type, properties);
        this.chestModel = new ChestModel();
    }

    @Override
    public void render(@Nullable MopedEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.MOPED_BODY, matrixStack, renderTypeBuffer, light, partialTicks);

        matrixStack.pushPose();

        matrixStack.translate(0.0, 0.0, 11.5 * 0.0625);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-22.5F));
        float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(vehicle).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F;
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(steeringWheelRotation));
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(22.5F));
        matrixStack.translate(0.0, 0.0, -11.5 * 0.0625);

        //Render handles bars
        matrixStack.pushPose();
        matrixStack.translate(0, (12.2739 - 8) * 0.0625, (16.4071 - 8) * 0.0625);
        this.renderDamagedPart(vehicle, VehicleModels.MOPED_HANDLES, matrixStack, renderTypeBuffer, light, partialTicks);
        matrixStack.popPose();

        //Render front bar and mud guard
        matrixStack.pushPose();
        {
            matrixStack.translate(0, (4.1044 - 8) * 0.0625, (19.8181 - 8) * 0.0625);
            this.renderDamagedPart(vehicle, VehicleModels.MOPED_MUD_GUARD, matrixStack, renderTypeBuffer, light, partialTicks);
        }
        matrixStack.popPose();

        //Render front wheel
        ItemStack wheelStack = this.wheelStackProperty.get(vehicle);
        if(!wheelStack.isEmpty())
        {
            matrixStack.pushPose();
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            Wheel wheel = properties.getFirstFrontWheel();
            if(wheel != null)
            {
                matrixStack.translate(0.0, -8 * 0.0625, 0.0);
                matrixStack.translate(0.0, -properties.getAxleOffset() * 0.0625F, 0.0);
                matrixStack.translate(wheel.getOffsetX() * 0.0625, wheel.getOffsetY() * 0.0625, wheel.getOffsetZ() * 0.0625);
                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-this.getWheelRotation(vehicle, wheel, partialTicks)));
                matrixStack.scale(wheel.getScaleX(), wheel.getScaleY(), wheel.getScaleZ());
                BakedModel wheelModel = RenderUtil.getModel(wheelStack);
                int wheelColor = IDyeable.getColorFromStack(wheelStack);
                RenderUtil.renderColoredModel(wheelModel, ItemTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, wheelColor, light, OverlayTexture.NO_OVERLAY);
            }
            matrixStack.popPose();
        }

        matrixStack.popPose();

        if(this.hasChestProperty.get(vehicle))
        {
            matrixStack.pushPose();
            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(180F));
            matrixStack.translate(0, 0, 6.5 * 0.0625F);
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            matrixStack.translate(-0.5, 0, 0);
            this.chestModel.render(matrixStack, renderTypeBuffer, Pair.of(this.prevOpenProgressProperty.get(vehicle), this.openProgressProperty.get(vehicle)), light, partialTicks);
            matrixStack.popPose();
        }
    }

    @Override
    public void applyPlayerModel(MopedEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(entity).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelNormal = wheelAngle / maxSteeringAngle;
        float steeringWheelRotation = steeringWheelNormal * 25F / 2F;
        model.rightArm.xRot = (float) Math.toRadians(-65F - steeringWheelRotation);
        model.rightArm.yRot = (float) Math.toRadians(5F);
        model.rightArm.z -= 1;
        model.rightArm.z -= steeringWheelNormal * 2;
        model.leftArm.xRot = (float) Math.toRadians(-65F + steeringWheelRotation);
        model.leftArm.yRot = (float) Math.toRadians(-5F);
        model.leftArm.z -= 1;
        model.leftArm.z += steeringWheelNormal * 2;
        model.rightLeg.xRot = (float) Math.toRadians(-62F);
        model.leftLeg.xRot = (float) Math.toRadians(-62F);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.MOPED_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.MOPED_HANDLES, parts, transforms,
                    MatrixTransform.translate(0.0F, 4.2739F * 0.0625F, 8.4071F * 0.0625F));
            TransformHelper.createTransformListForPart(VehicleModels.MOPED_MUD_GUARD, parts, transforms,
                    MatrixTransform.translate(0.0F, -3.8956F * 0.0625F, 11.8181F * 0.0625F));
            TransformHelper.createFuelFillerTransforms(ModEntities.MOPED.get(), VehicleModels.FUEL_DOOR_CLOSED, parts, transforms);
        };
    }
}
