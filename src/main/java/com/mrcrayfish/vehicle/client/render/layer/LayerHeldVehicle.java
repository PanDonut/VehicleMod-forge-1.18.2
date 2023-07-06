package com.mrcrayfish.vehicle.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.handler.HeldVehicleHandler;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class LayerHeldVehicle extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private VehicleEntity vehicle;
    private CachedVehicle cachedVehicle;
    private float width = -1.0F;

    public LayerHeldVehicle(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super(renderer);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource buffers, int light, AbstractClientPlayer player, float v, float v1, float delta, float v3, float v4, float v5)
    {
        VehicleEntity vehicle = HeldVehicleDataHandler.getHeldVehicle(player);
        if(vehicle != null)
        {
            if(this.cachedVehicle == null)
            {
                    EntityType<?> entityType = vehicle.getType();
                    Entity entity = entityType.create(player.level);
                    if(entity instanceof VehicleEntity)
                    {
                        this.vehicle = (VehicleEntity) entity;
                        this.width = entity.getBbWidth();
                        this.cachedVehicle = new CachedVehicle(this.vehicle);
                    }
            }
            if(this.cachedVehicle != null)
            {
                matrices.pushPose();
                HeldVehicleHandler.AnimationCounter counter = HeldVehicleHandler.idToCounter.get(player.getUUID());
                if(counter != null)
                {
                    float width = this.width / 2;
                    matrices.translate(0F, 1F - counter.getProgress(delta), -0.5F * Math.sin(Math.PI * counter.getProgress(delta)) - width * (1.0F - counter.getProgress(delta)));
                }
                Vec3 heldOffset = this.cachedVehicle.getProperties().getHeldOffset();
                matrices.translate(heldOffset.x * 0.0625D, heldOffset.y * 0.0625D, heldOffset.z * 0.0625D);
                matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(180F));
                matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(-90F));
                matrices.translate(0F, player.isCrouching() ? 0.3125F : 0.5625F, 0F);
                ((AbstractVehicleRenderer<VehicleEntity>)this.cachedVehicle.getRenderer()).setupTransformsAndRender(this.vehicle, matrices, buffers, delta, light);
                matrices.popPose();
            }
        }
        else
        {
            this.cachedVehicle = null;
            this.width = -1.0F;
        }
    }
}
