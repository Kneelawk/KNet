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

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import com.kneelawk.knet.example.blockentity.FancyLightBlockEntity;

public class FancyLightBlockEntityRenderer implements BlockEntityRenderer<FancyLightBlockEntity> {
    public FancyLightBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(FancyLightBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());


    }

    private void face(VertexConsumer consumer, MatrixStack.Entry entry, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float u0, float v0, float u1, float v1, int red, int green, int blue, int light) {
        Matrix4f model = entry.getPositionMatrix();
        Matrix3f normal = entry.getNormalMatrix();

        float dx0 = x2 - x0;
        float dy0 = y2 - y0;
        float dz0 = z2 - z0;
        float dx1 = x3 - x1;
        float dy1 = y3 - y1;
        float dz1 = z3 - z1;

        float nx = dy1 * dz0 - dz1 * dy0;
        float ny = dz1 * dx0 - dx1 * dz0;
        float nz = dx1 * dy0 - dy1 * dx0;
        float div = MathHelper.inverseSqrt(nx * nx + ny * ny + nz * nz);
        nx *= div;
        ny *= div;
        nz *= div;

        consumer.vertex(model, x0, y0, z0).color(red, green, blue, 255).texture(u0, v0).light(light).normal(normal, nx, ny, nz).next();
    }
}
