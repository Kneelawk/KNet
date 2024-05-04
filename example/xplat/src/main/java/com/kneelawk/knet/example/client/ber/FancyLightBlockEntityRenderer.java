/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.knet.example.client.ber;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.kneelawk.knet.example.blockentity.FancyLightBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static com.kneelawk.knet.example.KNetExample.id;

public class FancyLightBlockEntityRenderer implements BlockEntityRenderer<FancyLightBlockEntity> {
    private static final ResourceLocation OVERLAY_TEXTURE = id("block/fancy_light_overlay");

    public FancyLightBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(FancyLightBlockEntity entity, float tickDelta, PoseStack matrices,
                       MultiBufferSource vertexConsumers, int light, int overlay) {
        TextureAtlasSprite sprite =
            Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(OVERLAY_TEXTURE);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.cutout());

        int r = entity.getRed();
        int g = entity.getGreen();
        int b = entity.getBlue();

        face(consumer, matrices.last(), 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 1f, sprite, r, g, b, light);
        face(consumer, matrices.last(), 0f, 1f, 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, sprite, r, g, b, light);
        face(consumer, matrices.last(), 1f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, sprite, r, g, b, light);
        face(consumer, matrices.last(), 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, sprite, r, g, b, light);
        face(consumer, matrices.last(), 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, sprite, r, g, b, light);
        face(consumer, matrices.last(), 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, sprite, r, g, b, light);
    }

    private void face(VertexConsumer consumer, PoseStack.Pose entry, float x0, float y0, float z0, float x1,
                      float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, TextureAtlasSprite sprite,
                      int red, int green, int blue, int light) {
        Matrix4f model = entry.pose();
        Matrix3f normal = entry.normal();

        float dx0 = x2 - x0;
        float dy0 = y2 - y0;
        float dz0 = z2 - z0;
        float dx1 = x3 - x1;
        float dy1 = y3 - y1;
        float dz1 = z3 - z1;

        float nx = dy1 * dz0 - dz1 * dy0;
        float ny = dz1 * dx0 - dx1 * dz0;
        float nz = dx1 * dy0 - dy1 * dx0;
        float div = Mth.invSqrt(nx * nx + ny * ny + nz * nz);
        nx *= div;
        ny *= div;
        nz *= div;

        consumer.vertex(model, x0, y0, z0).color(red, green, blue, 255).uv(sprite.getU0(), sprite.getV0())
            .uv2(light).normal(entry, nx, ny, nz).endVertex();
        consumer.vertex(model, x1, y1, z1).color(red, green, blue, 255).uv(sprite.getU0(), sprite.getV1())
            .uv2(light).normal(entry, nx, ny, nz).endVertex();
        consumer.vertex(model, x2, y2, z2).color(red, green, blue, 255).uv(sprite.getU1(), sprite.getV1())
            .uv2(light).normal(entry, nx, ny, nz).endVertex();
        consumer.vertex(model, x3, y3, z3).color(red, green, blue, 255).uv(sprite.getU1(), sprite.getV0())
            .uv2(light).normal(entry, nx, ny, nz).endVertex();
    }
}
