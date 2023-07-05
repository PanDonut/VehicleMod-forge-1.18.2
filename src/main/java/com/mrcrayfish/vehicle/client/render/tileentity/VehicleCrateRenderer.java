package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.block.RotatedObjectBlock;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.tileentity.VehicleCrateTileEntity;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class VehicleCrateRenderer implements BlockEntityRenderer<VehicleCrateTileEntity>
{
    private final Minecraft minecraft;

    public VehicleCrateRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void render(VehicleCrateTileEntity entity, float delta, PoseStack matrices, MultiBufferSource buffers, int light, int overlay)
    {
        BlockState state = entity.getLevel().getBlockState(entity.getBlockPos());
        if(state.getBlock() != ModBlocks.VEHICLE_CRATE.get())
            return;

        matrices.pushPose();

        Direction facing = state.getValue(RotatedObjectBlock.DIRECTION);
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(facing.get2DDataValue() * -90F + 180F));
        matrices.translate(-0.5, -0.5, -0.5);

        this.minecraft.textureManager.bindForSetup(InventoryMenu.BLOCK_ATLAS);

        matrices.pushPose();

        light = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above()); //TODO figure out the correct way to calculate light

        if(entity.isOpened() && entity.getTimer() > 150)
        {
            double progress = Math.min(1.0F, Math.max(0, (entity.getTimer() - 150 + 5 * delta)) / 50.0);
            matrices.translate(0, (-4 * 0.0625) * progress, 0);
        }

        //Sides panels
        for(int i = 0; i < 4; i++)
        {
            matrices.pushPose();
            matrices.translate(0.5, 0, 0.5);
            matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(90F * i));
            matrices.translate(0, 0, 8 * 0.0625);

            if(entity.isOpened())
            {
                double progress = Math.min(1.0, Math.max(0, entity.getTimer() - (i * 20) + 5 * delta) / 90.0);
                double angle = (progress * progress) * 90F;
                double rotation = 1.0 - Math.cos(Math.toRadians(angle));
                matrices.mulPose(Axis.POSITIVE_X.rotationDegrees((float) rotation * 90F));
            }
            matrices.translate(0.0, 0.5, 0.0);
            matrices.translate(0, 0, -1.999 * 0.0625);
            //if(i % 2 == 0) matrixStack.scale(-1, 1, 1);
            RenderUtil.renderColoredModel(VehicleModels.VEHICLE_CRATE_SIDE.getBaseModel(), ItemTransforms.TransformType.NONE, false, matrices, buffers, -1, light, OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }

        //Render top panel
        if(!entity.isOpened())
        {
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(-90F));
            matrices.translate(0, 0, (6.001 * 0.0625));
            RenderUtil.renderColoredModel(VehicleModels.VEHICLE_CRATE_TOP.getBaseModel(), ItemTransforms.TransformType.NONE, false, matrices, buffers, -1, light, OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }

        //Render bottom panel
        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(90F));
        matrices.translate(0, 0, (6 * 0.0625) * 0.998);
        RenderUtil.renderColoredModel(VehicleModels.VEHICLE_CRATE_SIDE.getBaseModel(), ItemTransforms.TransformType.NONE, false, matrices, buffers, -1, light, OverlayTexture.NO_OVERLAY);
        matrices.popPose();

        matrices.popPose();

        if(entity.getEntity() != null && entity.isOpened())
        {
            matrices.translate(0.5F, 0.0F, 0.5F);

            double progress = Math.min(1.0F, Math.max(0, (entity.getTimer() - 150 + 5 * delta)) / 100.0);
            Pair<Float, Float> scaleAndOffset = EntityRayTracer.instance().getCrateScaleAndOffset((EntityType<? extends VehicleEntity>) entity.getEntity().getType());
            float scaleStart = scaleAndOffset.getLeft();
            float scale = scaleStart + (1 - scaleStart) * (float) progress;
            matrices.translate(0, 0, scaleAndOffset.getRight() * (1 - progress) * scale);

            if(entity.getTimer() >= 150)
            {
                matrices.translate(0, Math.sin(Math.PI * progress) * 5, 0);
                matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) (720F * progress)));
            }

            matrices.translate(0, (2 * 0.0625F) * (1.0F - progress), 0);
            matrices.scale(scale, scale, scale);

            EntityRenderer<? extends Entity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity.getEntity());
            renderer.render(entity.getEntity(), 0.0F, delta, matrices, buffers, light);
        }
        matrices.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull VehicleCrateTileEntity entity)
    {
        return true;
    }

    @Override
    public int getViewDistance()
    {
        return 65535;
    }
}
