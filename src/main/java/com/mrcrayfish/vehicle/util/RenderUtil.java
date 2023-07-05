package com.mrcrayfish.vehicle.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mrcrayfish.vehicle.client.util.OptifineHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class RenderUtil
{
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors (ARGB format).
     */
    public static void drawGradientRectHorizontal(int left, int top, int right, int bottom, int leftColor, int rightColor)
    {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(right, top, 0).color(rightColor).endVertex();
        bufferbuilder.vertex(left, top, 0).color(leftColor).endVertex();
        bufferbuilder.vertex(left, bottom, 0).color(leftColor).endVertex();
        bufferbuilder.vertex(right, bottom, 0).color(rightColor).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void scissor(int x, int y, int width, int height) //TODO might need fixing. I believe I rewrote this in a another mod
    {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        GL11.glScissor(x * scale, mc.getWindow().getScreenHeight() - y * scale - height * scale, Math.max(0, width * scale), Math.max(0, height * scale));
    }

    public static BakedModel getModel(ItemStack stack)
    {
        return MINECRAFT.getItemRenderer().getItemModelShaper().getItemModel(stack);
    }

    //TODO: All models are bright, is this the lightmap?, does it need a light engine?, or is it something else?
    public static void renderColoredModel(BakedModel model, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int color, int lightTexture, int overlayTexture)
    {
        matrixStack.pushPose();
        ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, leftHanded);
        matrixStack.translate(-0.5, -0.5, -0.5);
        if(!model.isCustomRenderer())
        {
            VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(Sheets.cutoutBlockSheet());
            renderModel(model, ItemStack.EMPTY, color, lightTexture, overlayTexture, matrixStack, vertexBuilder);
        }
        matrixStack.popPose();
    }

    public static void renderDamagedVehicleModel(BakedModel model, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack matrixStack, int stage, int color, int lightTexture, int overlayTexture)
    {
        matrixStack.pushPose();
        ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, leftHanded);
        matrixStack.translate(-0.5, -0.5, -0.5);
        if(!model.isCustomRenderer())
        {
            PoseStack.Pose entry = matrixStack.last();
            VertexConsumer vertexBuilder = new SheetedDecalTextureGenerator(MINECRAFT.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(stage)), entry.pose(), entry.normal());
            renderModel(model, ItemStack.EMPTY, color, lightTexture, overlayTexture, matrixStack, vertexBuilder);
        }
        matrixStack.popPose();
    }

    public static void renderModel(ItemStack stack, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int lightTexture, int overlayTexture, BakedModel model)
    {
        if(!stack.isEmpty())
        {
            matrixStack.pushPose();
            boolean isGui = transformType == ItemTransforms.TransformType.GUI;
            boolean tridentFlag = isGui || transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.FIXED;

            if(stack.is(Items.TRIDENT) && tridentFlag)
            {
                model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
            }
            else if (stack.is(Items.SPYGLASS))
            {
                model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
            }

            model = ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, leftHanded);
            matrixStack.translate(-0.5, -0.5, -0.5);
            if(!model.isCustomRenderer() && (!stack.is(Items.TRIDENT) || tridentFlag))
            {
                boolean fabulous = true;
                if (!isGui && !transformType.firstPerson() && stack.getItem() instanceof BlockItem)
                {
                    Block block = ((BlockItem) stack.getItem()).getBlock();
                    fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                }

                if(model.isLayered())
                {
                    for(Pair<BakedModel,RenderType> layerModel : model.getLayerModels(stack, fabulous))
                    {
                        RenderType type = layerModel.getSecond();
                        ForgeHooksClient.setRenderType(type);

                        VertexConsumer buffer = fabulous ?
                                ItemRenderer.getFoilBufferDirect(renderTypeBuffer, type, true, stack.hasFoil()) :
                                ItemRenderer.getFoilBuffer(renderTypeBuffer, type, true, stack.hasFoil());

                        renderModel(model, stack, -1, lightTexture, overlayTexture, matrixStack, buffer);
                    }
                    ForgeHooksClient.setRenderType(null);
                }
                else
                {
                    RenderType type = ItemBlockRenderTypes.getRenderType(stack, fabulous);
                    ForgeHooksClient.setRenderType(type);

                    VertexConsumer buffer = fabulous ?
                            ItemRenderer.getFoilBufferDirect(renderTypeBuffer, type, true, stack.hasFoil()) :
                            ItemRenderer.getFoilBuffer(renderTypeBuffer, type, true, stack.hasFoil());

                    renderModel(model, stack, -1, lightTexture, overlayTexture, matrixStack, buffer);
                    ForgeHooksClient.setRenderType(null);
                }
            }
            else
            {
                RenderProperties.get(stack).getItemStackRenderer().renderByItem(stack, transformType, matrixStack, renderTypeBuffer, lightTexture, overlayTexture);
            }

            matrixStack.popPose();
        }
    }

    private static void renderModel(BakedModel model, ItemStack stack, int color, int lightTexture, int overlayTexture, PoseStack matrixStack, VertexConsumer vertexBuilder)
    {
        Random random = new Random();
        for(Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderQuads(matrixStack, vertexBuilder, model.getQuads(null, direction, random, EmptyModelData.INSTANCE), stack, color, lightTexture, overlayTexture);
        }
        random.setSeed(42L);
        renderQuads(matrixStack, vertexBuilder, model.getQuads(null, null, random, EmptyModelData.INSTANCE), stack, color, lightTexture, overlayTexture);
    }

    private static void renderQuads(PoseStack matrixStack, VertexConsumer vertexBuilder, List<BakedQuad> quads, ItemStack stack, int color, int lightTexture, int overlayTexture)
    {
        boolean useItemColor = !stack.isEmpty() && color == -1;
        PoseStack.Pose entry = matrixStack.last();

        // noinspection ForLoopReplaceableByForEach
        for (int i = 0, quadsSize = quads.size(); i < quadsSize; i++)
        {
            BakedQuad quad = quads.get(i);
            int tintColor = 0xFFFFFFFF;

            if(OptifineHelper.isEmissiveTexturesEnabled())
            {
                quad = OptifineHelper.castAsEmissive(quad);

                if (quad == null)
                {
                    continue;
                }
            }

            if(quad.isTinted())
            {
                if(useItemColor)
                {
                    tintColor = MINECRAFT.getItemColors().getColor(stack, quad.getTintIndex());
                }
                else
                {
                    tintColor = color;
                }

                if (OptifineHelper.isCustomColorsEnabled())
                {
                    tintColor = OptifineHelper.castAsCustomColor(stack, quad.getTintIndex(), tintColor);
                }
            }

            float red = normalise(tintColor >> 16);
            float green = normalise(tintColor >> 8);
            float blue = normalise(tintColor);
            vertexBuilder.putBulkData(entry, quad, red, green, blue, lightTexture, overlayTexture, true);
        }
    }

    protected static float normalise(int value)
    {
        return (value & 0xFF) * (1F / 255.0F);
    }

    public static List<Component> lines(FormattedText text, int maxWidth)
    {
        List<FormattedText> lines = MINECRAFT.font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);
        return lines.stream().map(t -> new TextComponent(t.getString()).withStyle(ChatFormatting.GRAY)).collect(Collectors.toList());
    }
}
