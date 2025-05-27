package com.amberclient.screens;

import com.amberclient.utils.Module;
import com.amberclient.utils.ModuleManager;
import com.amberclient.utils.ConfigurableModule;
import com.amberclient.utils.ModuleSetting;
import net.minecraft.client.gui.screen.Screen;
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
    private static final int BACKGROUND_COLOR = new Color(20, 20, 25, 200).getRGB();
    private static final int PANEL_COLOR = new Color(30, 30, 35, 255).getRGB();
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
            categoryMap.computeIfAbsent(categoryName, k -> new ArrayList<>()). add(module);
        }
        for (Map.Entry<String, List<Module>> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            List<Module> categoryModules = entry.getValue();
            List<ModuleWrapper> wrappedModules = new ArrayList<>();
            for (Module module : categoryModules) {
                wrappedModules.add(new ModuleWrapper(module));
            }
            categories.add(new Category(categoryName, wrappedModules));
        }
        categories.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> this.close())
                .dimensions(this.width - 25, 5, 20, 20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("Close")))
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

        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

        int logoSize = 32;
        int centerX = this.width / 2;
        int headerY = 15;
        context.drawCenteredTextWithShadow(this.textRenderer, "AMBER CLIENT", centerX, headerY + logoSize + 5, ACCENT_COLOR);

        int panelWidth = Math.min(this.width - 40, 800);
        int panelHeight = Math.min(this.height - 100, 400);
        int panelX = centerX - panelWidth / 2;
        int panelY = headerY + logoSize + 25;

        float scale = 0.8f + 0.2f * animationProgress;
        float alpha = animationProgress;

        int scaledWidth = (int)(panelWidth * scale);
        int scaledHeight = (int)(panelHeight * scale);
        int scaledX = centerX - scaledWidth / 2;
        int scaledY = panelY + (panelHeight - scaledHeight) / 2;

        scaledX = Math.round(scaledX);
        scaledY = Math.round(scaledY);

        int panelColorWithAlpha = new Color(
                (PANEL_COLOR >> 16) & 0xFF,
                (PANEL_COLOR >> 8) & 0xFF,
                PANEL_COLOR & 0xFF,
                (int)(((PANEL_COLOR >> 24) & 0xFF) * alpha)
        ).getRGB();

        context.fill(scaledX, scaledY, scaledX + scaledWidth, scaledY + scaledHeight, panelColorWithAlpha);

        int categoryWidth = 150;
        int separatorX = scaledX + categoryWidth;
        context.fill(separatorX, scaledY, separatorX + 2, scaledY + scaledHeight, ACCENT_COLOR);

        renderCategories(context, scaledX, scaledY, categoryWidth, scaledHeight, mouseX, mouseY);
        renderModules(context, separatorX + 10, scaledY, scaledWidth - categoryWidth - 10, scaledHeight, mouseX, mouseY);

        int statusBarY = scaledY + scaledHeight + 5;
        context.fill(scaledX, statusBarY, scaledX + scaledWidth, statusBarY + 20, PANEL_COLOR);

        String statusText = configModule != null ?
                "Configuring: " + configModule.getName() :
                "Amber Client v1.0 • MC 1.21.4";
        context.drawTextWithShadow(this.textRenderer, statusText, scaledX + 10, statusBarY + 6, TEXT_COLOR);

        // Render configuration panel
        if (configPanelAnimation > 0.0f && configModule != null) {
            renderConfigPanel(context, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCategories(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
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
                    (isHovered ? new Color(50, 50, 55, 220).getRGB() : PANEL_COLOR);

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
                    new Color(ACCENT_COLOR).darker().getRGB() :
                    new Color(40, 40, 45, 220).getRGB();

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
            context.fill(scrollbarX, scrollAreaTop, scrollbarX + scrollbarWidth, scrollAreaBottom, new Color(50, 50, 55).getRGB());
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

        int configPanelX = centerX + (this.width - panelWidth) / 2 + CONFIG_PANEL_OFFSET_X;
        int configPanelY = panelY;
        int configPanelWidth = panelWidth;
        int configPanelHeight = panelHeight;

        // Animation: slide from right
        int maxOffset = this.width;
        int currentOffset = (int)(maxOffset * (1.0f - configPanelAnimation));
        configPanelX -= currentOffset;

        int panelColorWithAlpha = new Color(
                (PANEL_COLOR >> 16) & 0xFF,
                (PANEL_COLOR >> 8) & 0xFF,
                PANEL_COLOR & 0xFF,
                (int)(255 * configPanelAnimation)
        ).getRGB();

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
        List<ModuleSetting> settings = ((ConfigurableModule)configModule.getWrappedModule()).getSettings();
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
            int settingY = settingsAreaTop + i * (settingHeight + spacing) - (int)configScrollOffset;

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

                // Background du toggle
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
            }
        }

        if (contentHeight > settingsAreaHeight) {
            int scrollbarX = configPanelX + configPanelWidth - 15;
            int scrollbarWidth = 5;
            context.fill(scrollbarX, settingsAreaTop, scrollbarX + scrollbarWidth, settingsAreaBottom, new Color(50, 50, 55).getRGB());
            float scrollRatio = (float) settingsAreaHeight / contentHeight;
            int thumbHeight = Math.max(20, (int)(settingsAreaHeight * scrollRatio));
            int thumbY = settingsAreaTop + (int)((settingsAreaHeight - thumbHeight) * (configScrollOffset / maxScroll));
            context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, ACCENT_COLOR);
        }

        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (animationProgress < 1.0f) {
            return false;
        }

        // Handle config panel clicks
        if (configModule != null && configPanelAnimation > 0.0f) {
            int panelWidth = Math.min(this.width - 40, 300);
            int panelHeight = Math.min(this.height - 100, 400);
            int centerX = this.width / 2;
            int headerY = 15;
            int logoSize = 32;
            int panelY = headerY + logoSize + 25;

            int configPanelX = centerX + (this.width - panelWidth) / 2 + CONFIG_PANEL_OFFSET_X;
            int maxOffset = this.width;
            int currentOffset = (int)(maxOffset * (1.0f - configPanelAnimation));
            configPanelX -= currentOffset;
            int configPanelY = panelY;

            // Close button
            int closeButtonX = configPanelX + panelWidth - 25;
            int closeButtonY = configPanelY + 5;
            if (mouseX >= closeButtonX && mouseX <= closeButtonX + 20 &&
                    mouseY >= closeButtonY && mouseY <= closeButtonY + 20) {
                configModule = null;
                configScrollOffset = 0.0f;
                return true;
            }

            // Settings clicks
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

                // Vérifier si le setting est visible
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
                }
            }

            // Scrollbar
            int scrollbarX = configPanelX + panelWidth - 15;
            int scrollbarWidth = 5;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                    mouseY >= settingsAreaTop && mouseY <= settingsAreaTop + settingsAreaHeight) {
                isConfigDragging = true;
                configDragStartY = (int) mouseY;
                configDragStartOffset = configScrollOffset;
                return true;
            }

            // No action for clicks inside the panel; only close on close button
            if (mouseX >= configPanelX && mouseX <= configPanelX + panelWidth &&
                    mouseY >= configPanelY && mouseY <= configPanelY + panelHeight) {
                return true;
            }

            // Clicks outside the panel are ignored
            return false;
        }

        // Existing panel click logic
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

                // Right-click or config icon click
                if (module.getWrappedModule() instanceof ConfigurableModule) {
                    if (button == 1 && // Right-click
                            mouseX >= moduleX && mouseX <= moduleX + moduleWidth &&
                            mouseY >= moduleY && mouseY <= moduleY + moduleHeight) {
                        openConfigPanel(module);
                        return true;
                    }
                    if (button == 0) { // Left-click on config icon
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
        clickedModules.clear();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isConfigDragging && configModule != null) {
            int panelHeight = Math.min(this.height - 100, 400);
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
            int configPanelX = centerX + (this.width - panelWidth) / 2 - (int)(this.width * (1.0f - configPanelAnimation));
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
        super.close();
    }

    private void openConfigPanel(ModuleWrapper module) {
        if (module.getWrappedModule() instanceof ConfigurableModule) {
            configModule = module;
            configPanelAnimation = 0.0f;
            configScrollOffset = 0.0f;
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

    @Override
    public void renderBackground(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }
}