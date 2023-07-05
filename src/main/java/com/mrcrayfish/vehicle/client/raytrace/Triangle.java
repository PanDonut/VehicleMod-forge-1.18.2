package com.mrcrayfish.vehicle.client.raytrace;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

/**
 * Author: MrCrayfish
 */
public record Triangle(float[] vertices) 
{
    public void draw(PoseStack matrixStack, VertexConsumer builder, float red, float green, float blue, float alpha)
    {
        Matrix4f matrix = matrixStack.last().pose();
        builder.vertex(matrix, this.vertices[6], this.vertices[7], this.vertices[8]).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, this.vertices[0], this.vertices[1], this.vertices[2]).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, this.vertices[0], this.vertices[1], this.vertices[2]).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, this.vertices[3], this.vertices[4], this.vertices[5]).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, this.vertices[3], this.vertices[4], this.vertices[5]).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, this.vertices[6], this.vertices[7], this.vertices[8]).color(red, green, blue, alpha).endVertex();
    }
}
