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

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.kneelawk.knet.example.screen.FancyLightScreenHandler;

import static com.kneelawk.knet.example.KNetExample.id;
import static com.kneelawk.knet.example.KNetExample.tt;

public class FancyLightScreen extends HandledScreen<FancyLightScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE = id("container/background");

    public FancyLightScreen(FancyLightScreenHandler handler, PlayerInventory inventory,
                            Text title) {
        super(handler, inventory, title);

        backgroundHeight = 8 + 10 + 10 + 20 + 20 + 10 + 20 + 20;
        backgroundWidth = 8 + 120;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        addButtonsForColor(x + 4, y + 4 + 20, 0);
        addButtonsForColor(x + 4 + 40, y + 4 + 20, 1);
        addButtonsForColor(x + 4 + 40 + 40, y + 4 + 20, 2);
    }

    private void addButtonsForColor(int x, int y, int index) {
        addDrawableChild(new IncrementButton(x, y, 40, 20, Text.literal("++"), index, 10));
        addDrawableChild(new IncrementButton(x, y + 20, 40, 20, Text.literal("+"), index, 1));
        addDrawableChild(new IncrementButton(x, y + 20 + 20 + 10, 40, 20, Text.literal("-"), index, -1));
        addDrawableChild(new IncrementButton(x, y + 20 + 20 + 10 + 20, 40, 20, Text.literal("--"), index, -10));
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, titleX, titleY, 0x404040, false);

        Text redText = tt("gui", "red");
        context.drawText(textRenderer, redText, 4 + (40 - textRenderer.getWidth(redText)) / 2, 4 + 10 + 1, 0x404040,
            false);
        Text greenText = tt("gui", "green");
        context.drawText(textRenderer, greenText, 4 + 40 + (40 - textRenderer.getWidth(greenText)) / 2, 4 + 10 + 1,
            0x404040, false);
        Text blueText = tt("gui", "blue");
        context.drawText(textRenderer, blueText, 4 + 40 + 40 + (40 - textRenderer.getWidth(blueText)) / 2, 4 + 10 + 1,
            0x404040, false);

        Text redValue = Text.literal(String.valueOf(handler.getRed()));
        context.drawText(textRenderer, redValue, 4 + (40 - textRenderer.getWidth(redValue)) / 2, 4 + 20 + 40 + 1,
            0x404040, false);
        Text greenValue = Text.literal(String.valueOf(handler.getGreen()));
        context.drawText(textRenderer, greenValue, 4 + 40 + (40 - textRenderer.getWidth(greenValue)) / 2,
            4 + 20 + 40 + 1, 0x404040, false);
        Text blueValue = Text.literal(String.valueOf(handler.getBlue()));
        context.drawText(textRenderer, blueValue, 4 + 40 + 40 + (40 - textRenderer.getWidth(blueValue)) / 2,
            4 + 20 + 40 + 1, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, backgroundWidth, backgroundHeight);
    }

    private class IncrementButton extends PressableWidget {
        private final int index;
        private final int amount;

        public IncrementButton(int x, int y, int w, int h, Text text, int index, int amount) {
            super(x, y, w, h, text);
            this.index = index;
            this.amount = amount;
        }

        @Override
        public void onPress() {
            handler.updateValue(MathHelper.clamp(handler.getValue(index) + amount, 0, 255), index);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }
}
