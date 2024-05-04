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

package com.kneelawk.knet.example.client.screen;

import com.kneelawk.knet.example.screen.FancyLightScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import static com.kneelawk.knet.example.KNetExample.id;
import static com.kneelawk.knet.example.KNetExample.tt;

public class FancyLightScreen extends AbstractContainerScreen<FancyLightScreenHandler> {
    private static final ResourceLocation BACKGROUND_TEXTURE = id("container/background");

    public FancyLightScreen(FancyLightScreenHandler handler, Inventory inventory,
                            Component title) {
        super(handler, inventory, title);

        imageHeight = 8 + 10 + 10 + 20 + 20 + 10 + 20 + 20;
        imageWidth = 8 + 120;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addButtonsForColor(x + 4, y + 4 + 20, 0);
        addButtonsForColor(x + 4 + 40, y + 4 + 20, 1);
        addButtonsForColor(x + 4 + 40 + 40, y + 4 + 20, 2);
    }

    private void addButtonsForColor(int x, int y, int index) {
        addRenderableWidget(new IncrementButton(x, y, 40, 20, Component.literal("++"), index, 10));
        addRenderableWidget(new IncrementButton(x, y + 20, 40, 20, Component.literal("+"), index, 1));
        addRenderableWidget(new IncrementButton(x, y + 20 + 20 + 10, 40, 20, Component.literal("-"), index, -1));
        addRenderableWidget(new IncrementButton(x, y + 20 + 20 + 10 + 20, 40, 20, Component.literal("--"), index, -10));
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        context.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);

        Component redText = tt("gui", "red");
        context.drawString(font, redText, 4 + (40 - font.width(redText)) / 2, 4 + 10 + 1, 0x404040,
            false);
        Component greenText = tt("gui", "green");
        context.drawString(font, greenText, 4 + 40 + (40 - font.width(greenText)) / 2, 4 + 10 + 1,
            0x404040, false);
        Component blueText = tt("gui", "blue");
        context.drawString(font, blueText, 4 + 40 + 40 + (40 - font.width(blueText)) / 2, 4 + 10 + 1,
            0x404040, false);

        Component redValue = Component.literal(String.valueOf(menu.getRed()));
        context.drawString(font, redValue, 4 + (40 - font.width(redValue)) / 2, 4 + 20 + 40 + 1,
            0x404040, false);
        Component greenValue = Component.literal(String.valueOf(menu.getGreen()));
        context.drawString(font, greenValue, 4 + 40 + (40 - font.width(greenValue)) / 2,
            4 + 20 + 40 + 1, 0x404040, false);
        Component blueValue = Component.literal(String.valueOf(menu.getBlue()));
        context.drawString(font, blueValue, 4 + 40 + 40 + (40 - font.width(blueValue)) / 2,
            4 + 20 + 40 + 1, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        context.blitSprite(INVENTORY_LOCATION, x, y, imageWidth, imageHeight);
    }

    private class IncrementButton extends AbstractButton {
        private final int index;
        private final int amount;

        public IncrementButton(int x, int y, int w, int h, Component text, int index, int amount) {
            super(x, y, w, h, text);
            this.index = index;
            this.amount = amount;
        }

        @Override
        public void onPress() {
            menu.updateValue(Mth.clamp(menu.getValue(index) + amount, 0, 255), index);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            defaultButtonNarrationText(builder);
        }
    }
}
