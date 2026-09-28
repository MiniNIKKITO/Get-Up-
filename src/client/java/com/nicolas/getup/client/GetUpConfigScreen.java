package com.nicolas.getup.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Vanilla-style configuration screen exposed through Mod Menu.
 * Only timing values are configurable for now.
 */
public final class GetUpConfigScreen extends Screen {
    private static final float MIN_SECONDS = 0.05f;
    private static final float MAX_SECONDS = 3600.0f;

    private final Screen parent;

    private EditBox youDiedField;
    private EditBox getUpField;
    private EditBox whiteField;
    private EditBox exposureField;
    private Button nextDayButton;
    private Button enabledButton;

    public GetUpConfigScreen(Screen parent) {
        super(Component.literal("Get Up!"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int rowStartY = Math.max(70, this.height / 2 - 105);
        int rowSpacing = 31;
        int labelX = this.width / 2 - 135;
        int fieldX = this.width / 2 + 35;
        int fieldWidth = 100;
        int fieldHeight = 20;

        this.enabledButton = Button.builder(
                        enabledLabel(),
                        button -> {
                            GetUpConfig.enabled = !GetUpConfig.enabled;
                            this.enabledButton.setMessage(enabledLabel());
                        })
                .bounds(fieldX, rowStartY - 6, fieldWidth, 20)
                .build();
        this.addRenderableWidget(this.enabledButton);

        this.youDiedField = createField(fieldX, rowStartY + rowSpacing, fieldWidth, fieldHeight, GetUpConfig.youDiedSeconds);
        this.getUpField = createField(fieldX, rowStartY + rowSpacing * 2, fieldWidth, fieldHeight, GetUpConfig.getUpSeconds);
        this.whiteField = createField(fieldX, rowStartY + rowSpacing * 3, fieldWidth, fieldHeight, GetUpConfig.whiteFadeSeconds);
        this.exposureField = createField(fieldX, rowStartY + rowSpacing * 4, fieldWidth, fieldHeight, GetUpConfig.exposureFadeSeconds);
        this.addRenderableWidget(this.youDiedField);
        this.addRenderableWidget(this.getUpField);
        this.addRenderableWidget(this.whiteField);
        this.addRenderableWidget(this.exposureField);
        int optionY = rowStartY + rowSpacing * 5 + 6;
        this.nextDayButton = Button.builder(
                        nextDayLabel(),
                        button -> {
                            GetUpConfig.advanceToNextDay = !GetUpConfig.advanceToNextDay;
                            this.nextDayButton.setMessage(nextDayLabel());
                        })
                .bounds(fieldX, optionY - 6, 100, 20)
                .build();
        this.addRenderableWidget(this.nextDayButton);

        int buttonY = rowStartY + rowSpacing * 6 + 10;
        int buttonWidth = 120;
        int gap = 8;
        int leftButtonX = this.width / 2 - buttonWidth - gap / 2;
        int rightButtonX = this.width / 2 + gap / 2;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Reset defaults"),
                        button -> resetFields()
                )
                .bounds(leftButtonX, buttonY, buttonWidth, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Done"),
                        button -> saveAndClose()
                )
                .bounds(rightButtonX, buttonY, buttonWidth, 20)
                .build());
    }

    private EditBox createField(int x, int y, int width, int height, float value) {
        EditBox field = new EditBox(this.font, x, y, width, height, Component.literal("seconds"));
        field.setValue(formatSeconds(value));
        field.setMaxLength(12);
        return field;
    }

    private void resetFields() {
        GetUpConfig.reset();
        this.youDiedField.setValue(formatSeconds(GetUpConfig.youDiedSeconds));
        this.getUpField.setValue(formatSeconds(GetUpConfig.getUpSeconds));
        this.whiteField.setValue(formatSeconds(GetUpConfig.whiteFadeSeconds));
        this.exposureField.setValue(formatSeconds(GetUpConfig.exposureFadeSeconds));
        this.nextDayButton.setMessage(nextDayLabel());
        this.enabledButton.setMessage(enabledLabel());
        GetUpConfig.save();
    }

    private Component enabledLabel() {
        return Component.literal("Mod: " + (GetUpConfig.enabled ? "ON" : "OFF"));
    }

    private void saveAndClose() {
        GetUpConfig.youDiedSeconds = parseSeconds(this.youDiedField, GetUpConfig.youDiedSeconds);
        GetUpConfig.getUpSeconds = parseSeconds(this.getUpField, GetUpConfig.getUpSeconds);
        GetUpConfig.whiteFadeSeconds = parseSeconds(this.whiteField, GetUpConfig.whiteFadeSeconds);
        GetUpConfig.exposureFadeSeconds = parseSeconds(this.exposureField, GetUpConfig.exposureFadeSeconds);
        GetUpConfig.save();
        closeToParent();
    }

    private float parseSeconds(EditBox field, float fallback) {
        try {
            float value = Float.parseFloat(field.getValue().trim().replace(',', '.'));
            if (!Float.isFinite(value) || value < MIN_SECONDS || value > MAX_SECONDS) {
                field.setValue(formatSeconds(fallback));
                return fallback;
            }
            return value;
        } catch (NumberFormatException ignored) {
            field.setValue(formatSeconds(fallback));
            return fallback;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);

        int rowStartY = Math.max(70, this.height / 2 - 105);
        int rowSpacing = 31;
        int labelX = this.width / 2 - 135;

        graphics.text(this.font, Component.literal("Enable mod"), labelX, rowStartY + 6, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.literal("You died delay"), labelX, rowStartY + rowSpacing + 6, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.literal("Get up delay"), labelX, rowStartY + rowSpacing * 2 + 6, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.literal("Black to white"), labelX, rowStartY + rowSpacing * 3 + 6, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.literal("Exposure recovery"), labelX, rowStartY + rowSpacing * 4 + 6, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.literal("Advance to next day"), labelX, rowStartY + rowSpacing * 5 + 6, 0xFFFFFFFF, false);

        graphics.text(this.font, Component.literal("seconds"), this.width / 2 + 143, rowStartY + rowSpacing + 6, 0xFFAAAAAA, false);
        graphics.text(this.font, Component.literal("seconds"), this.width / 2 + 143, rowStartY + rowSpacing * 2 + 6, 0xFFAAAAAA, false);
        graphics.text(this.font, Component.literal("seconds"), this.width / 2 + 143, rowStartY + rowSpacing * 3 + 6, 0xFFAAAAAA, false);
        graphics.text(this.font, Component.literal("seconds"), this.width / 2 + 143, rowStartY + rowSpacing * 4 + 6, 0xFFAAAAAA, false);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    private void closeToParent() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private Component nextDayLabel() {
        return Component.literal(GetUpConfig.advanceToNextDay ? "ON" : "OFF");
    }

    private static String formatSeconds(float value) {
        if (value == Math.round(value)) {
            return Integer.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
