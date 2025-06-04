package com.amberclient.screens;

import com.amberclient.AmberClient;
import com.amberclient.modules.hud.Transparency;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleManager;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGUI extends Screen {
    // Theme colors
    private static final int BASE_BACKGROUND_COLOR = new Color(20, 20, 25, 200).getRGB();
    private static final int BASE_PANEL_COLOR = new Color(30, 30, 35, 255).getRGB();
    private static final int ACCENT_COLOR = new Color(255, 165, 0).getRGB();
    private static final int ACCENT_HOVER_COLOR = new Color(255, 190, 50).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();
    private static final int TEXT_SHADOW_COLOR = new Color(0, 0, 0, 100).getRGB();
    private static final int OUTLINE_COLOR = new Color(255, 255, 255, 180).getRGB();

    // Animation
    private float animationProgress = 0.0f;
    private long lastTime;

    // Categories
    private final List<Category> categories = new ArrayList<>();
    private int selectedCategory = 0;

    // For scrolling
    private float scrollOffset = 0.0f;
    private boolean isDragging = false;
    private int dragStartY;
    private float dragStartOffset;

    // Track clicked modules for feedback
    private final List<ModuleWrapper> clickedModules = new ArrayList<>();
    private long clickTime = 0;
    private static final long CLICK_FEEDBACK_DURATION = 300;

    // Configuration panel
    private ModuleWrapper configModule = null;
    private float configPanelAnimation = 0.0f;
    private float configScrollOffset = 0.0f;
    private boolean isConfigDragging = false;
    private int configDragStartY;
    private float configDragStartOffset;
    private static final int CONFIG_PANEL_OFFSET_X = -350;

    // Configuration panel dragging
    private boolean isConfigPanelDragging = false;
    private int configPanelDragStartX;
    private int configPanelDragStartY;
    private int configPanelOffsetX = CONFIG_PANEL_OFFSET_X;
    private int configPanelOffsetY = 0;

    // Slider dragging for DOUBLE settings
    private ModuleSetting draggedSetting = null;
    private double sliderStartValue;

    public ClickGUI() {
        super(Text.literal("Amber Client - by @gqdThinky"));
        lastTime = System.currentTimeMillis();
        initializeCategories();
    }

    private void initializeCategories() {
        List<Module> modules = ModuleManager.getInstance().getModules();
        Map<String, List<Module>> categoryMap = new HashMap<>();
        for (Module module : modules) {
            String categoryName = module.getCategory();
            categoryMap.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(module);
        }

        // Create categories
        for (Map.Entry<String, List<Module>> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            List<Module> categoryModules = entry.getValue();
            List<ModuleWrapper> wrappedModules = new ArrayList<>();
            for (Module module : categoryModules) {
                wrappedModules.add(new ModuleWrapper(module));
            }
            categories.add(new Category(categoryName, wrappedModules));
        }

        Category hudCategory = null;
        for (Category category : categories) {
            if (category.getName().equals("HUD")) {
                hudCategory = category;
                break;
            }
        }
        if (hudCategory != null) {
            categories.remove(hudCategory);
            categories.add(hudCategory);
        }

        categories.sort((c1, c2) -> {
            if (c1.getName().equals("HUD") || c2.getName().equals("HUD")) {
                return 0;
            }
            return c1.getName().compareToIgnoreCase(c2.getName());
        });
    }

    private Transparency getTransparencyModule() {
        for (Module module : ModuleManager.getInstance().getModules()) {
            if (module instanceof Transparency) {
                return (Transparency) module;
            }
        }
        return null;
    }

    private int applyTransparency(int baseColor, float transparency) {
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;
        int a = (int) (((baseColor >> 24) & 0xFF) * transparency);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> this.close())
                .dimensions(this.width - 25, 5, 20, 20)
                .tooltip(Tooltip.of(Text.literal("Close")))
                .build());
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTime;
        lastTime = currentTime;

        if (animationProgress < 1.0f) {
            animationProgress += timeDiff / 300.0f;
            animationProgress = MathHelper.clamp(animationProgress, 0.0f, 1.0f);
        }
        if (configModule != null && configPanelAnimation < 1.0f) {
            configPanelAnimation += timeDiff / 200.0f;
            configPanelAnimation = MathHelper.clamp(configPanelAnimation, 0.0f, 1.0f);
        } else if (configModule == null && configPanelAnimation > 0.0f) {
            configPanelAnimation -= timeDiff / 200.0f;
            configPanelAnimation = MathHelper.clamp(configPanelAnimation, 0.0f, 1.0f);
        }

        // Get transparency level from Transparency module
        float transparency = 1.0f;
        Transparency transparencyModule = getTransparencyModule();
        if (transparencyModule != null && transparencyModule.isEnabled()) {
            transparency = (float) transparencyModule.getTransparencyLevel();
        }

        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, this.width, this.height, applyTransparency(BASE_BACKGROUND_COLOR, transparency));

        int logoSize = 32;
        int centerX = this.width / 2;
        int headerY = 15;
        context.drawCenteredTextWithShadow(this.textRenderer, "AMBER CLIENT", centerX, headerY + logoSize + 5, ACCENT_COLOR);

        int panelWidth = Math.min(this.width - 40, 800);
        int panelHeight = Math.min(this.height - 100, 400);
        int panelX = centerX - panelWidth / 2;
        int panelY = headerY + logoSize + 25;

        float scale = 0.8f + 0.2f * animationProgress;
        float alpha = animationProgress * transparency;

        int scaledWidth = (int)(panelWidth * scale);
        int scaledHeight = (int)(panelHeight * scale);
        int scaledX = centerX - scaledWidth / 2;
        int scaledY = panelY + (panelHeight - scaledHeight) / 2;

        scaledX = Math.round(scaledX);
        scaledY = Math.round(scaledY);

        int panelColorWithAlpha = applyTransparency(BASE_PANEL_COLOR, alpha);

        context.fill(scaledX, scaledY, scaledX + scaledWidth, scaledY + scaledHeight, panelColorWithAlpha);

        int categoryWidth = 150;
        int separatorX = scaledX + categoryWidth;
        context.fill(separatorX, scaledY, separatorX + 2, scaledY + scaledHeight, ACCENT_COLOR);

        renderCategories(context, scaledX, scaledY, categoryWidth, scaledHeight, mouseX, mouseY);
        renderModules(context, separatorX + 10, scaledY, scaledWidth - categoryWidth - 10, scaledHeight, mouseX, mouseY);

        int statusBarY = scaledY + scaledHeight + 5;
        context.fill(scaledX, statusBarY, scaledX + scaledWidth, statusBarY + 20, applyTransparency(BASE_PANEL_COLOR, transparency));

        String statusText = configModule != null ?
                "Configuring: " + configModule.getName() :
                "Amber Client " + getModVersion() + " • MC 1.21.4";
        context.drawTextWithShadow(this.textRenderer, statusText, scaledX + 10, statusBarY + 6, TEXT_COLOR);

        if (configPanelAnimation > 0.0f && configModule != null) {
            renderConfigPanel(context, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCategories(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Get transparency level for categories
        float transparency = 1.0f;
        Transparency transparencyModule = getTransparencyModule();
        if (transparencyModule != null && transparencyModule.isEnabled()) {
            transparency = (float) transparencyModule.getTransparencyLevel();
        }

        int categoryHeight = 40;
        int spacing = 5;
        int totalHeight = categories.size() * (categoryHeight + spacing) - spacing;
        int startY = y + (height - totalHeight) / 2;

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            int categoryY = startY + i * (categoryHeight + spacing);
            int categoryX = x + 10;
            int categoryWidth = width - 20;

            boolean isHovered = mouseX >= categoryX && mouseX <= categoryX + categoryWidth &&
                    mouseY >= categoryY && mouseY <= categoryY + categoryHeight;

            int bgColor = (selectedCategory == i) ? ACCENT_COLOR :
                    (isHovered ? applyTransparency(new Color(50, 50, 55, 220).getRGB(), transparency) : applyTransparency(BASE_PANEL_COLOR, transparency));

            categoryX = Math.round(categoryX);
            categoryY = Math.round(categoryY);
            context.fill(categoryX, categoryY, categoryX + categoryWidth, categoryY + categoryHeight, bgColor);

            int textColor = (selectedCategory == i) ? Color.WHITE.getRGB() : TEXT_COLOR;
            String name = category.getName();

            int textX = categoryX + categoryWidth / 2;
            int textY = categoryY + (categoryHeight - 8) / 2;

            context.drawCenteredTextWithShadow(this.textRenderer, name, textX, textY, textColor);
        }
    }

    private void renderModules(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (selectedCategory < 0 || selectedCategory >= categories.size()) {
            return;
        }

        // Get transparency level for modules
        float transparency = 1.0f;
        Transparency transparencyModule = getTransparencyModule();
        if (transparencyModule != null && transparencyModule.isEnabled()) {
            transparency = (float) transparencyModule.getTransparencyLevel();
        }

        Category category = categories.get(selectedCategory);
        List<ModuleWrapper> modules = category.getModules();

        String title = category.getName().toUpperCase();
        context.drawTextWithShadow(this.textRenderer, title, Math.round(x), Math.round(y + 10), ACCENT_COLOR);

        int scrollAreaTop = Math.round(y + 30);
        int scrollAreaHeight = Math.round(height - 40);
        int scrollAreaBottom = scrollAreaTop + scrollAreaHeight;

        context.enableScissor(x, scrollAreaTop, x + width, scrollAreaBottom);

        int moduleHeight = 30;
        int spacing = 5;
        int moduleWidth = width - 30;

        int contentHeight = modules.size() * (moduleHeight + spacing) - spacing;
        int maxScroll = Math.max(0, contentHeight - scrollAreaHeight);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);

        for (int i = 0; i < modules.size(); i++) {
            ModuleWrapper module = modules.get(i);
            int moduleY = Math.round(scrollAreaTop + i * (moduleHeight + spacing) - (int)scrollOffset);

            if (moduleY + moduleHeight < scrollAreaTop || moduleY > scrollAreaBottom) {
                continue;
            }

            int moduleBgColor = module.isEnabled() ?
                    new Color(ACCENT_COLOR).darker().getRGB() : // Pas de transparence pour les modules activés
                    applyTransparency(new Color(40, 40, 45, 220).getRGB(), transparency);

            int moduleX = Math.round(x);
            context.fill(moduleX, moduleY, moduleX + moduleWidth, moduleY + moduleHeight, moduleBgColor);

            context.drawTextWithShadow(this.textRenderer, module.getName(), moduleX + 10, moduleY + 7, TEXT_COLOR);
            context.drawTextWithShadow(this.textRenderer, module.getDescription(), moduleX + 10, moduleY + 20, new Color(180, 180, 180).getRGB());

            if (module.getWrappedModule() instanceof ConfigurableModule) {
                int configX = moduleX + moduleWidth - 50;
                context.drawTextWithShadow(this.textRenderer, "⚙", configX, moduleY + 7, Color.WHITE.getRGB());
            }

            int toggleX = Math.round(moduleX + moduleWidth - 24);
            int toggleY = Math.round(moduleY + 5);
            int toggleWidth = 18;
            int toggleHeight = 18;

            boolean isHovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                    mouseY >= toggleY && mouseY <= toggleY + toggleHeight;

            boolean isClicked = clickedModules.contains(module) && (System.currentTimeMillis() - clickTime) < CLICK_FEEDBACK_DURATION;

            int toggleColor;
            if (isClicked) {
                toggleColor = ACCENT_HOVER_COLOR;
            } else if (isHovered) {
                toggleColor = ACCENT_HOVER_COLOR;
            } else {
                toggleColor = new Color(245, 235, 216).getRGB();
            }

            context.fill(toggleX - 1, toggleY - 1, toggleX + toggleWidth + 1, toggleY, OUTLINE_COLOR);
            context.fill(toggleX - 1, toggleY + toggleHeight, toggleX + toggleWidth + 1, toggleY + toggleHeight + 1, OUTLINE_COLOR);
            context.fill(toggleX - 1, toggleY, toggleX, toggleY + toggleHeight, OUTLINE_COLOR);
            context.fill(toggleX + toggleWidth, toggleY, toggleX + toggleWidth + 1, toggleY + toggleHeight, OUTLINE_COLOR);

            context.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleColor);

            if (module.isEnabled()) {
                context.drawTextWithShadow(this.textRenderer, "✓", toggleX + 7, toggleY + 5, Color.WHITE.getRGB());
            }
        }

        if (contentHeight > scrollAreaHeight) {
            int scrollbarX = Math.round(x + width - 20);
            int scrollbarWidth = 10;
            context.fill(scrollbarX, scrollAreaTop, scrollbarX + scrollbarWidth, scrollAreaBottom, applyTransparency(new Color(50, 50, 55).getRGB(), transparency));
            float scrollRatio = (float) scrollAreaHeight / contentHeight;
            int thumbHeight = Math.max(20, (int)(scrollAreaHeight * scrollRatio));
            int thumbY = Math.round(scrollAreaTop + (int)((scrollAreaHeight - thumbHeight) * (scrollOffset / maxScroll)));
            context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, ACCENT_COLOR);
        }

        context.disableScissor();
    }

    private void renderConfigPanel(DrawContext context, int mouseX, int mouseY) {
        int panelWidth = Math.min(this.width - 40, 300);
        int panelHeight = Math.min(this.height - 100, 400);
        int centerX = this.width / 2;
        int headerY = 15;
        int logoSize = 32;
        int panelY = headerY + logoSize + 25;

        int configPanelX = centerX + (this.width - panelWidth) / 2 + configPanelOffsetX;
        int configPanelY = panelY + configPanelOffsetY;
        int configPanelWidth = panelWidth;
        int configPanelHeight = panelHeight;

        // Animation: slide from right
        int maxOffset = this.width;
        int currentOffset = (int) (maxOffset * (1.0f - configPanelAnimation));
        configPanelX -= currentOffset;

        // Use base panel color without transparency
        int panelColorWithAlpha = BASE_PANEL_COLOR;

        context.fill(configPanelX, configPanelY, configPanelX + configPanelWidth, configPanelY + configPanelHeight, panelColorWithAlpha);

        // Header
        context.fill(configPanelX, configPanelY, configPanelX + configPanelWidth, configPanelY + 30, ACCENT_COLOR);
        context.drawTextWithShadow(this.textRenderer, configModule.getName() + " Settings", configPanelX + 10, configPanelY + 10, Color.WHITE.getRGB());

        // Close button
        int closeButtonX = configPanelX + configPanelWidth - 25;
        int closeButtonY = configPanelY + 5;
        boolean closeHovered = mouseX >= closeButtonX && mouseX <= closeButtonX + 20 &&
                mouseY >= closeButtonY && mouseY <= closeButtonY + 20;

        int closeButtonColor = closeHovered ? new Color(255, 80, 80).getRGB() : new Color(200, 50, 50).getRGB();
        context.fill(closeButtonX, closeButtonY, closeButtonX + 20, closeButtonY + 20, closeButtonColor);
        context.drawTextWithShadow(this.textRenderer, "×", closeButtonX + 7, closeButtonY + 5, Color.WHITE.getRGB());

        // Settings list
        List<ModuleSetting> settings = ((ConfigurableModule) configModule.getWrappedModule()).getSettings();
        int settingsAreaTop = configPanelY + 40;
        int settingsAreaHeight = configPanelHeight - 50;
        int settingsAreaBottom = settingsAreaTop + settingsAreaHeight;

        context.enableScissor(configPanelX, settingsAreaTop, configPanelX + configPanelWidth, settingsAreaBottom);

        int settingHeight = 40;
        int spacing = 5;
        int settingWidth = configPanelWidth - 20;
        int contentHeight = settings.size() * (settingHeight + spacing) - spacing;
        int maxScroll = Math.max(0, contentHeight - settingsAreaHeight);
        configScrollOffset = MathHelper.clamp(configScrollOffset, 0, maxScroll);

        for (int i = 0; i < settings.size(); i++) {
            ModuleSetting setting = settings.get(i);
            int settingY = settingsAreaTop + i * (settingHeight + spacing) - (int) configScrollOffset;

            if (settingY + settingHeight < settingsAreaTop || settingY > settingsAreaBottom) {
                continue;
            }

            int settingX = configPanelX + 10;
            context.fill(settingX, settingY, settingX + settingWidth, settingY + settingHeight, new Color(40, 40, 45, 220).getRGB());

            context.drawTextWithShadow(this.textRenderer, setting.getName(), settingX + 10, settingY + 10, TEXT_COLOR);
            context.drawTextWithShadow(this.textRenderer, setting.getDescription(), settingX + 10, settingY + 25, new Color(180, 180, 180).getRGB());

            if (setting.getType() == ModuleSetting.SettingType.BOOLEAN) {
                int toggleX = settingX + settingWidth - 60;
                int toggleY = settingY + 10;
                int toggleWidth = 40;
                int toggleHeight = 20;

                boolean isOn = setting.getBooleanValue();
                boolean toggleHovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                        mouseY >= toggleY && mouseY <= toggleY + toggleHeight;

                // Toggle background
                int toggleBgColor = toggleHovered ?
                        (isOn ? new Color(255, 190, 50).getRGB() : new Color(120, 120, 120).getRGB()) :
                        (isOn ? ACCENT_COLOR : new Color(100, 100, 100).getRGB());

                context.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleBgColor);

                // Border
                context.fill(toggleX - 1, toggleY - 1, toggleX + toggleWidth + 1, toggleY, OUTLINE_COLOR);
                context.fill(toggleX - 1, toggleY + toggleHeight, toggleX + toggleWidth + 1, toggleY + toggleHeight + 1, OUTLINE_COLOR);
                context.fill(toggleX - 1, toggleY, toggleX, toggleY + toggleHeight, OUTLINE_COLOR);
                context.fill(toggleX + toggleWidth, toggleY, toggleX + toggleWidth + 1, toggleY + toggleHeight, OUTLINE_COLOR);

                // Text
                String toggleText = isOn ? "ON" : "OFF";
                int textWidth = this.textRenderer.getWidth(toggleText);
                int textX = toggleX + (toggleWidth - textWidth) / 2;
                int textY = toggleY + (toggleHeight - 8) / 2;
                context.drawTextWithShadow(this.textRenderer, toggleText, textX, textY, Color.WHITE.getRGB());
            } else if (setting.getType() == ModuleSetting.SettingType.DOUBLE && setting.hasRange()) {
                int sliderX = settingX + settingWidth - 100;
                int sliderY = settingY + 10;
                int sliderWidth = 80;
                int sliderHeight = 10;

                double min = setting.getMinValue().doubleValue();
                double max = setting.getMaxValue().doubleValue();
                double value = setting.getDoubleValue();
                double range = max - min;
                double normalizedValue = (value - min) / range;
                int filledWidth = (int) (sliderWidth * normalizedValue);

                // Slider background
                context.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, new Color(100, 100, 100).getRGB());
                // Filled portion
                context.fill(sliderX, sliderY, sliderX + filledWidth, sliderY + sliderHeight, ACCENT_COLOR);
                // Border
                context.fill(sliderX - 1, sliderY - 1, sliderX + sliderWidth + 1, sliderY, OUTLINE_COLOR);
                context.fill(sliderX - 1, sliderY + sliderHeight, sliderX + sliderWidth + 1, sliderY + sliderHeight + 1, OUTLINE_COLOR);
                context.fill(sliderX - 1, sliderY, sliderX, sliderY + sliderHeight, OUTLINE_COLOR);
                context.fill(sliderX + sliderWidth, sliderY, sliderX + sliderWidth + 1, sliderY + sliderHeight, OUTLINE_COLOR);

                // Display current value
                String valueText = String.format("%.2f", value);
                context.drawTextWithShadow(this.textRenderer, valueText, sliderX + sliderWidth + 5, sliderY + 1, TEXT_COLOR);
            }
        }

        if (contentHeight > settingsAreaHeight) {
            int scrollbarX = configPanelX + configPanelWidth - 15;
            int scrollbarWidth = 5;
            context.fill(scrollbarX, settingsAreaTop, scrollbarX + scrollbarWidth, settingsAreaBottom, new Color(50, 50, 55).getRGB());
            float scrollRatio = (float) settingsAreaHeight / contentHeight;
            int thumbHeight = Math.max(20, (int) (settingsAreaHeight * scrollRatio));
            int thumbY = settingsAreaTop + (int) ((settingsAreaHeight - thumbHeight) * (configScrollOffset / maxScroll));
            context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, ACCENT_COLOR);
        }

        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (animationProgress < 1.0f) {
            return false;
        }

        // Handle clicks on the configuration panel first
        if (configModule != null && configPanelAnimation > 0.0f) {
            int panelWidth = Math.min(this.width - 40, 300);
            int panelHeight = Math.min(this.height - 100, 400);
            int centerX = this.width / 2;
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;

            int configPanelX = centerX + (this.width - panelWidth) / 2 + configPanelOffsetX;
            int maxOffset = this.width;
            int currentOffset = (int)(maxOffset * (1.0f - configPanelAnimation));
            configPanelX -= currentOffset;
            int configPanelY = panelY + configPanelOffsetY;

            if (mouseX >= configPanelX && mouseX <= configPanelX + panelWidth &&
                    mouseY >= configPanelY && mouseY <= configPanelY + panelHeight) {
                // Close button
                int closeButtonX = configPanelX + panelWidth - 25;
                int closeButtonY = configPanelY + 5;
                if (mouseX >= closeButtonX && mouseX <= closeButtonX + 20 &&
                        mouseY >= closeButtonY && mouseY <= closeButtonY + 20) {
                    configModule = null;
                    configScrollOffset = 0.0f;
                    draggedSetting = null;
                    return true;
                }

                // Dragging the header
                if (mouseX >= configPanelX && mouseX <= configPanelX + panelWidth &&
                        mouseY >= configPanelY && mouseY <= configPanelY + 30) {
                    isConfigPanelDragging = true;
                    configPanelDragStartX = (int) mouseX;
                    configPanelDragStartY = (int) mouseY;
                    return true;
                }

                // Clicks on settings
                List<ModuleSetting> settings = ((ConfigurableModule)configModule.getWrappedModule()).getSettings();
                int settingsAreaTop = configPanelY + 40;
                int settingsAreaHeight = panelHeight - 50;
                int settingHeight = 40;
                int spacing = 5;
                int settingWidth = panelWidth - 20;

                for (int i = 0; i < settings.size(); i++) {
                    ModuleSetting setting = settings.get(i);
                    int settingY = settingsAreaTop + i * (settingHeight + spacing) - (int)configScrollOffset;
                    int settingX = configPanelX + 10;

                    if (settingY + settingHeight < settingsAreaTop || settingY > settingsAreaTop + settingsAreaHeight) {
                        continue;
                    }

                    if (setting.getType() == ModuleSetting.SettingType.BOOLEAN) {
                        int toggleX = settingX + settingWidth - 60;
                        int toggleY = settingY + 10;
                        int toggleWidth = 40;
                        int toggleHeight = 20;

                        if (mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                                mouseY >= toggleY && mouseY <= toggleY + toggleHeight) {
                            setting.setBooleanValue(!setting.getBooleanValue());
                            ((ConfigurableModule)configModule.getWrappedModule()).onSettingChanged(setting);
                            return true;
                        }
                    } else if (setting.getType() == ModuleSetting.SettingType.DOUBLE && setting.hasRange()) {
                        int sliderX = settingX + settingWidth - 100;
                        int sliderY = settingY + 10;
                        int sliderWidth = 80;
                        int sliderHeight = 10;

                        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth &&
                                mouseY >= sliderY && mouseY <= sliderY + sliderHeight) {
                            draggedSetting = setting;
                            sliderStartValue = setting.getDoubleValue();

                            double min = setting.getMinValue().doubleValue();
                            double max = setting.getMaxValue().doubleValue();
                            double range = max - min;
                            double normalizedValue = ((mouseX - sliderX) / sliderWidth) * range + min;
                            double step = setting.getStepValue().doubleValue();
                            normalizedValue = Math.round(normalizedValue / step) * step;
                            normalizedValue = MathHelper.clamp(normalizedValue, min, max);
                            setting.setDoubleValue(normalizedValue);
                            ((ConfigurableModule)configModule.getWrappedModule()).onSettingChanged(setting);
                            return true;
                        }
                    }
                }

                // Scrollbar
                int scrollbarX = configPanelX + configPanelY - 15;
                int scrollbarWidth = 5;
                if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                        mouseY >= settingsAreaTop && mouseY <= settingsAreaTop + settingsAreaHeight) {
                    isConfigDragging = true;
                    configDragStartY = (int) mouseY;
                    configDragStartOffset = configScrollOffset;
                    return true;
                }

                // Consume the event if the click is within the panel
                return true;
            }
        }

        // Handle widgets (like the main close button)
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Handle clicks on the main panel
        int panelWidth = Math.min(this.width - 40, 800);
        int panelHeight = Math.min(this.height - 100, 400);
        int centerX = this.width / 2;
        int headerY = 15;
        int logoSize = 32;
        int panelY = headerY + logoSize + 25;

        float scale = 0.8f + 0.2f * animationProgress;
        int scaledWidth = (int)(panelWidth * scale);
        int scaledHeight = (int)(panelHeight * scale);
        int scaledX = centerX - scaledWidth / 2;
        int scaledY = panelY + (panelHeight - scaledHeight) / 2;

        int categoryWidth = 150;
        int categoryHeight = 40;
        int spacing = 5;
        int totalCategoryHeight = categories.size() * (categoryHeight + spacing) - spacing;
        int startCategoryY = scaledY + (scaledHeight - totalCategoryHeight) / 2;

        for (int i = 0; i < categories.size(); i++) {
            int categoryY = startCategoryY + i * (categoryHeight + spacing);
            int categoryX = scaledX + 10;
            int catWidth = categoryWidth - 20;

            if (mouseX >= categoryX && mouseX <= categoryX + catWidth &&
                    mouseY >= categoryY && mouseY <= categoryY + categoryHeight) {
                selectedCategory = i;
                scrollOffset = 0;
                return true;
            }
        }

        int separatorX = scaledX + categoryWidth;
        int moduleAreaX = separatorX + 10;
        int moduleAreaY = scaledY + 30;
        int moduleAreaWidth = scaledWidth - categoryWidth - 10;
        int moduleAreaHeight = scaledHeight - 40;
        int scrollAreaTop = moduleAreaY;
        int scrollAreaBottom = moduleAreaY + moduleAreaHeight;

        boolean moduleClicked = false;

        if (selectedCategory >= 0 && selectedCategory < categories.size()) {
            Category category = categories.get(selectedCategory);
            List<ModuleWrapper> modules = category.getModules();

            int moduleHeight = 30;
            int moduleSpacing = 5;
            int moduleWidth = moduleAreaWidth - 30;

            for (int i = 0; i < modules.size(); i++) {
                ModuleWrapper module = modules.get(i);
                int moduleY = scrollAreaTop + i * (moduleHeight + moduleSpacing) - (int)scrollOffset;

                if (moduleY + moduleHeight < scrollAreaTop || moduleY > scrollAreaBottom) {
                    continue;
                }

                int moduleX = moduleAreaX;

                if (module.getWrappedModule() instanceof ConfigurableModule) {
                    if (button == 1 && // Right-click
                            mouseX >= moduleX && mouseX <= moduleX + moduleWidth &&
                            mouseY >= moduleY && mouseY <= moduleY + moduleHeight) {
                        openConfigPanel(module);
                        return true;
                    }
                    if (button == 0) { // Left-click on the config icon
                        int configX = moduleX + moduleWidth - 50;
                        int configY = moduleY + 7;
                        int configSize = 10;
                        if (mouseX >= configX && mouseX <= configX + configSize &&
                                mouseY >= configY && mouseY <= configY + configSize) {
                            openConfigPanel(module);
                            return true;
                        }
                    }
                }

                int toggleX = moduleAreaX + moduleWidth - 24;
                int toggleY = moduleY + 5;
                int toggleWidth = 18;
                int toggleHeight = 18;

                if (mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                        mouseY >= toggleY && mouseY <= toggleY + toggleHeight) {
                    if (!clickedModules.contains(module)) {
                        clickedModules.add(module);
                    }
                    clickTime = System.currentTimeMillis();
                    ModuleManager.getInstance().toggleModule(module.getWrappedModule());
                    moduleClicked = true;
                    break;
                }
            }
        }

        int scrollbarX = moduleAreaX + moduleAreaWidth - 20;
        int scrollbarWidth = 10;

        if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= moduleAreaY && mouseY <= moduleAreaY + moduleAreaHeight) {
            isDragging = true;
            dragStartY = (int) mouseY;
            dragStartOffset = scrollOffset;
            return true;
        }

        if (!moduleClicked) {
            clickedModules.clear();
        }

        return moduleClicked;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        isConfigDragging = false;
        isConfigPanelDragging = false;
        draggedSetting = null;
        clickedModules.clear();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isConfigPanelDragging && configModule != null) {
            int deltaX = (int) mouseX - configPanelDragStartX;
            int deltaY = (int) mouseY - configPanelDragStartY;

            configPanelOffsetX += deltaX;
            configPanelOffsetY += deltaY;

            // Limit to prevent panel from going off-screen
            int panelWidth = Math.min(this.width - 40, 300);
            int panelHeight = Math.min(this.height - 100, 400);
            configPanelOffsetX = MathHelper.clamp(configPanelOffsetX, -this.width + 50, this.width - 50);
            configPanelOffsetY = MathHelper.clamp(configPanelOffsetY, -panelHeight + 30, this.height - 30);

            configPanelDragStartX = (int) mouseX;
            configPanelDragStartY = (int) mouseY;
            return true;
        }

        if (draggedSetting != null && draggedSetting.getType() == ModuleSetting.SettingType.DOUBLE) {
            int panelWidth = Math.min(this.width - 40, 300);
            int centerX = this.width / 2;
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;
            int configPanelX = centerX + (this.width - panelWidth) / 2 + configPanelOffsetX;
            int maxOffset = this.width;
            int currentOffset = (int)(maxOffset * (1.0f - configPanelAnimation));
            configPanelX -= currentOffset;
            int settingsAreaTop = panelY + 40;
            int settingWidth = panelWidth - 20;

            List<ModuleSetting> settings = ((ConfigurableModule)configModule.getWrappedModule()).getSettings();
            int settingHeight = 40;
            int spacing = 5;
            for (int i = 0; i < settings.size(); i++) {
                ModuleSetting setting = settings.get(i);
                if (setting != draggedSetting) continue;

                int settingY = settingsAreaTop + i * (settingHeight + spacing) - (int)configScrollOffset;
                int settingX = configPanelX + 10;
                int sliderX = settingX + settingWidth - 100;
                int sliderWidth = 80;

                double min = setting.getMinValue().doubleValue();
                double max = setting.getMaxValue().doubleValue();
                double range = max - min;
                double normalizedValue = ((mouseX - sliderX) / sliderWidth) * range + min;
                double step = setting.getStepValue().doubleValue();
                normalizedValue = Math.round(normalizedValue / step) * step;
                normalizedValue = MathHelper.clamp(normalizedValue, min, max);
                setting.setDoubleValue(normalizedValue);
                ((ConfigurableModule)configModule.getWrappedModule()).onSettingChanged(setting);
                return true;
            }
        }

        if (isConfigDragging && configModule != null) {
            int panelHeight = Math.min(this.width - 40, 400);
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;
            int settingsAreaHeight = panelHeight - 50;
            List<ModuleSetting> settings = ((ConfigurableModule)configModule.getWrappedModule()).getSettings();
            int settingHeight = 40;
            int spacing = 5;
            int contentHeight = settings.size() * (settingHeight + spacing) - spacing;
            int maxScroll = Math.max(0, contentHeight - settingsAreaHeight);

            int dragDelta = (int) mouseY - configDragStartY;
            float scrollRatio = (float) maxScroll / (settingsAreaHeight - 20);
            configScrollOffset = configDragStartOffset + dragDelta * scrollRatio;
            configScrollOffset = MathHelper.clamp(configScrollOffset, 0, maxScroll);
            return true;
        }

        if (isDragging && selectedCategory >= 0 && selectedCategory < categories.size()) {
            int panelWidth = Math.min(this.width - 40, 800);
            int panelHeight = Math.min(this.height - 100, 400);
            int centerX = this.width / 2;
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;
            float scale = 0.8f + 0.2f * animationProgress;
            int scaledHeight = (int)(panelHeight * scale);
            int moduleAreaHeight = scaledHeight - 40;
            List<ModuleWrapper> modules = categories.get(selectedCategory).getModules();
            int moduleHeight = 30;
            int spacing = 5;
            int contentHeight = modules.size() * (moduleHeight + spacing) - spacing;
            int maxScroll = Math.max(0, contentHeight - moduleAreaHeight);
            int dragDelta = (int) mouseY - dragStartY;
            float scrollRatio = (float) maxScroll / (moduleAreaHeight - 20);
            scrollOffset = dragStartOffset + dragDelta * scrollRatio;
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (configModule != null && configPanelAnimation > 0.0f) {
            int panelWidth = Math.min(this.width - 40, 300);
            int panelHeight = Math.min(this.height - 100, 400);
            int centerX = this.width / 2;
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;
            int configPanelX = centerX + (this.width - panelWidth) / 2 + configPanelOffsetX;
            int settingsAreaTop = panelY + 40;
            int settingsAreaHeight = panelHeight - 50;

            if (mouseX >= configPanelX && mouseX <= configPanelX + panelWidth &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight) {
                configScrollOffset -= scrollY * 15;
                List<ModuleSetting> settings = ((ConfigurableModule)configModule.getWrappedModule()).getSettings();
                int settingHeight = 40;
                int spacing = 5;
                int contentHeight = settings.size() * (settingHeight + spacing) - spacing;
                int maxScroll = Math.max(0, contentHeight - settingsAreaHeight);
                configScrollOffset = MathHelper.clamp(configScrollOffset, 0, maxScroll);
                return true;
            }
        }

        scrollOffset -= scrollY * 15;
        if (selectedCategory >= 0 && selectedCategory < categories.size()) {
            int panelWidth = Math.min(this.width - 40, 800);
            int panelHeight = Math.min(this.height - 100, 400);
            int centerX = this.width / 2;
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;
            float scale = 0.8f + 0.2f * animationProgress;
            int scaledHeight = (int)(panelHeight * scale);
            int moduleAreaHeight = scaledHeight - 40;
            List<ModuleWrapper> modules = categories.get(selectedCategory).getModules();
            int moduleHeight = 30;
            int spacing = 5;
            int contentHeight = modules.size() * (moduleHeight + spacing) - spacing;
            int maxScroll = Math.max(0, contentHeight - moduleAreaHeight);
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
        } else {
            scrollOffset = 0;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (configModule != null) {
                configModule = null;
                configScrollOffset = 0.0f;
                draggedSetting = null;
                return true;
            }
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        configModule = null;
        configScrollOffset = 0.0f;
        draggedSetting = null;
        super.close();
    }

    private void openConfigPanel(ModuleWrapper module) {
        if (module.getWrappedModule() instanceof ConfigurableModule) {
            configModule = module;
            configPanelAnimation = 0.0f;
            configScrollOffset = 0.0f;
            configPanelOffsetX = CONFIG_PANEL_OFFSET_X;
            configPanelOffsetY = 0;
        }
    }

    private static class Category {
        private final String name;
        private final List<ModuleWrapper> modules;

        public Category(String name, List<ModuleWrapper> modules) {
            this.name = name;
            this.modules = modules;
        }

        public String getName() {
            return name;
        }

        public List<ModuleWrapper> getModules() {
            return modules;
        }
    }

    private static class ModuleWrapper {
        private final Module module;
        public ModuleWrapper(Module module) {
            this.module = module;
        }
        public String getName() {
            return module.getName();
        }
        public String getDescription() {
            return module.getDescription();
        }
        public boolean isEnabled() {
            return module.isEnabled();
        }
        public void toggle() {
            module.toggle();
        }
        public Module getWrappedModule() {
            return module;
        }
    }

    private static String getModVersion(){
        return AmberClient.MOD_VERSION;
    }

    @Override
    public void renderBackground(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }
}