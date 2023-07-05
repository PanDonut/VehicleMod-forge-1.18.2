package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
public class EditVehicleScreen extends AbstractContainerScreen<EditVehicleContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("vehicle:textures/gui/edit_vehicle.png");

    private final Inventory playerInventory;
    private final Container vehicleInventory;
    private final CachedVehicle cachedVehicle;

    private RenderTarget framebuffer;
    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public EditVehicleScreen(EditVehicleContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.vehicleInventory = container.getVehicleInventory();
        this.cachedVehicle = new CachedVehicle(container.getVehicle());
        this.imageHeight = 184;
    }

    @Override
    protected void renderBg(@NotNull PoseStack matrices, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        this.blit(matrices, left, top, 0, 0, this.imageWidth, this.imageHeight);

        if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
        {
            if(this.vehicleInventory.getItem(0).isEmpty())
            {
                this.blit(matrices, left + 8, top + 17, 176, 0, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(0).isEmpty())
        {
            this.blit(matrices, left + 8, top + 17, 176, 32, 16, 16);
        }

        if(this.cachedVehicle.getProperties().canChangeWheels())
        {
            if(this.vehicleInventory.getItem(1).isEmpty())
            {
                this.blit(matrices, left + 8, top + 35, 176, 16, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(1).isEmpty())
        {
            this.blit(matrices, left + 8, top + 35, 176, 32, 16, 16);
        }

        if(this.framebuffer != null)
        {
            this.framebuffer.bindRead();
            int startX = left + 26;
            int startY = top + 17;
            RenderSystem.disableCull();
            Matrix4f pose = matrices.last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(pose, startX, startY, this.getBlitOffset()).uv(0, 1).endVertex();
            builder.vertex(pose, startX, startY + 70, this.getBlitOffset()).uv(0, 0).endVertex();
            builder.vertex(pose, startX + 142, startY + 70, this.getBlitOffset()).uv(1, 0).endVertex();
            builder.vertex(pose, startX + 142, startY, this.getBlitOffset()).uv(1, 1).endVertex();
            tesselator.end();
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.font.draw(matrixStack, this.title.getString(), 8, 6, 4210752);
        minecraft.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);

        if(this.showHelp)
        {
            matrixStack.pushPose();
            {
                matrixStack.scale(0.5F, 0.5F, 0.5F);
                minecraft.font.draw(matrixStack, I18n.get("container.edit_vehicle.window_help"), 56, 38, 0xFFFFFF);
            }
            matrixStack.popPose();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderVehicleToBuffer(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        matrixStack.pushPose();
        {
            Lighting.setupLevel(matrixStack.last().pose());

            AbstractVehicleRenderer renderer = this.cachedVehicle.getRenderer();
            if(renderer != null)
            {
                this.bindFrameBuffer();

                matrixStack.pushPose();
                PoseStack.Pose last = matrixStack.last();
                last.pose().setIdentity();
                last.normal().setIdentity();
                matrixStack.translate(0, -20, -150);
                matrixStack.translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
                matrixStack.translate(0, this.windowY - (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);

                Quaternion quaternion = Axis.POSITIVE_X.rotationDegrees(20F);
                quaternion.mul(Axis.NEGATIVE_X.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
                quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
                quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(45F));
                matrixStack.mulPose(quaternion);

                matrixStack.scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
                matrixStack.scale(22F, 22F, 22F);

                Transform position = this.cachedVehicle.getProperties().getDisplayTransform();
                matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees((float) position.getRotX()));
                matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) position.getRotY()));
                matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees((float) position.getRotZ()));
                matrixStack.translate(position.getX(), position.getY(), position.getZ());

                MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
                renderer.setupTransformsAndRender(this.menu.getVehicle(), matrixStack, renderTypeBuffer, Minecraft.getInstance().getFrameTime(), 15728880);
                renderTypeBuffer.endBatch();

                matrixStack.popPose();

                this.unbindFrameBuffer();
            }
        }
        matrixStack.popPose();

        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(scroll < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scroll > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX += (mouseX - this.mouseClickedX);
                this.windowY -= (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (mouseX - this.mouseClickedX);
                this.windowRotationY -= (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderVehicleToBuffer(matrixStack, mouseX, mouseY, partialTicks);
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        this.renderTooltip(matrixStack, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.vehicleInventory.getItem(0).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 16, 18, 18))
            {
                if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(new TextComponent("Engine")), Component::getVisualOrderText), mouseX, mouseY); //TODO localise
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(new TextComponent("Engine"), new TextComponent(ChatFormatting.GRAY + "Not applicable")), Component::getVisualOrderText), mouseX, mouseY); //TODO localise
                }
            }
        }

        if(this.vehicleInventory.getItem(1).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 34, 18, 18))
            {
                if(this.cachedVehicle.getProperties().canChangeWheels())
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(new TextComponent("Wheels")), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(new TextComponent("Wheels"), new TextComponent(ChatFormatting.GRAY + "Not applicable")), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }
    }

    private void bindFrameBuffer()
    {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int windowWidth = (int) (142 * window.getGuiScale());
        int windowHeight = (int) (70 * window.getGuiScale());
        if(this.framebuffer == null)
        {
            this.framebuffer = new MainTarget(windowWidth, windowHeight);
            this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        else if(this.framebuffer.width != windowWidth || this.framebuffer.height != windowHeight)
        {
            this.framebuffer.destroyBuffers();
            this.framebuffer.resize(windowWidth, windowHeight, Minecraft.ON_OSX);
        }
        this.framebuffer.clear(Minecraft.ON_OSX);
        this.framebuffer.bindWrite(true);
    }

    private void unbindFrameBuffer()
    {
        if(this.framebuffer != null)
        {
            this.framebuffer.unbindWrite();
        }
        // Rebind the main buffer
        this.minecraft.getMainRenderTarget().bindWrite(true);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        if(this.framebuffer != null)
        {
            this.framebuffer.destroyBuffers();
        }
    }
}
