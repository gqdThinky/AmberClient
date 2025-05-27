package com.amberclient.screens;

import com.amberclient.utils.Module;
import com.amberclient.utils.ModuleManager;
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

    public ClickGUI() {
        super(Text.literal("Amber Client - by @gqdThinky"));
        lastTime = System.currentTimeMillis();

        // Initialize categories from ModuleManager
        initializeCategories();
    }

    private void initializeCategories() {
        // Retrieve modules from ModuleManager
        List<Module> modules = ModuleManager.getInstance().getModules();

        // Group modules by category
        Map<String, List<Module>> categoryMap = new HashMap<>();
        for (Module module : modules) {
            String categoryName = module.getCategory();
            categoryMap.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(module);
        }

        // Create categories for TabGUI
        for (Map.Entry<String, List<Module>> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            List<Module> categoryModules = entry.getValue();
            List<ModuleWrapper> wrappedModules = new ArrayList<>();
            for (Module module : categoryModules) {
                wrappedModules.add(new ModuleWrapper(module));
            }
            categories.add(new Category(categoryName, wrappedModules));
        }

        // Sort categories by name for consistent order
        categories.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    @Override
    protected void init() {
        super.init();

        // Close button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> this.close())
                .dimensions(this.width - 25, 5, 20, 20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("Fermer")))
                .build());
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        // Opening animation
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTime;
        lastTime = currentTime;

        if (animationProgress < 1.0f) {
            animationProgress += timeDiff / 300.0f;
            animationProgress = MathHelper.clamp(animationProgress, 0.0f, 1.0f);
        }

        // Semi-transparent background
        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

        // Logo and title
        int logoSize = 32;
        int centerX = this.width / 2;
        int headerY = 15;

        context.drawCenteredTextWithShadow(this.textRenderer, "AMBER CLIENT", centerX, headerY + logoSize + 5, ACCENT_COLOR);

        // Main panel (with animation)
        int panelWidth = Math.min(this.width - 40, 800);
        int panelHeight = Math.min(this.height - 100, 400);
        int panelX = centerX - panelWidth / 2;
        int panelY = headerY + logoSize + 25;

        // Apply animation
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

        String statusText = "Amber Client v1.0 • MC 1.21.4";
        context.drawTextWithShadow(this.textRenderer, statusText, scaledX + 10, statusBarY + 6, TEXT_COLOR);

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

            int toggleX = Math.round(moduleX + moduleWidth - 24); // Increased toggle button size
            int toggleY = Math.round(moduleY + 5);
            int toggleWidth = 18; // Increased toggle button size
            int toggleHeight = 18; // Increased toggle button size

            // Check for hover
            boolean isHovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                    mouseY >= toggleY && mouseY <= toggleY + toggleHeight;

            // Check for click feedback
            boolean isClicked = clickedModules.contains(module) && (System.currentTimeMillis() - clickTime) < CLICK_FEEDBACK_DURATION;

            // Determine toggle button color
            int toggleColor;
            if (isClicked) {
                toggleColor = ACCENT_HOVER_COLOR;
            } else if (isHovered) {
                toggleColor = ACCENT_HOVER_COLOR;
            } else {
                toggleColor = new Color(245, 235, 216).getRGB();
            }

            // Draw outline for click feedback and hover effect
            context.fill(toggleX - 1, toggleY - 1, toggleX + toggleWidth + 1, toggleY, OUTLINE_COLOR); // Top
            context.fill(toggleX - 1, toggleY + toggleHeight, toggleX + toggleWidth + 1, toggleY + toggleHeight + 1, OUTLINE_COLOR); // Bottom
            context.fill(toggleX - 1, toggleY, toggleX, toggleY + toggleHeight, OUTLINE_COLOR); // Left
            context.fill(toggleX + toggleWidth, toggleY, toggleX + toggleWidth + 1, toggleY + toggleHeight, OUTLINE_COLOR); // Right

            context.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleColor);

            if (module.isEnabled()) {
                context.drawTextWithShadow(this.textRenderer, "✓", toggleX + 7, toggleY + 5, Color.WHITE.getRGB()); // Adjusted checkmark position
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



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (animationProgress < 1.0f) {
            return false;
        }

        // Calculate panel dimensions and position with scaling
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

        // Category click logic
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

        // Module click logic
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

                int toggleX = moduleAreaX + moduleWidth - 20;
                int toggleY = moduleY + 5;
                int toggleWidth = 15;
                int toggleHeight = 15;

                if (mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                        mouseY >= toggleY && mouseY <= toggleY + toggleHeight) {
                    if (!clickedModules.contains(module)) {
                        clickedModules.add(module);
                    }
                    clickTime = System.currentTimeMillis();
                    boolean wasEnabled = module.isEnabled();
                    ModuleManager.getInstance().toggleModule(module.getWrappedModule());
                    moduleClicked = true;
                    break;
                }
            }
        }

        // Scrollbar click logic
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
        clickedModules.clear();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            if (selectedCategory >= 0 && selectedCategory < categories.size()) {
                int panelWidth = Math.min(this.width - 40, 800);
                int panelHeight = Math.min(this.height - 100, 400);
                int centerX = this.width / 2;
                int headerY = 15;
                int logoSize = 32;
                int panelY = headerY + logoSize + 25;

                float scale = 0.8f + 0.2f * animationProgress;
                int scaledHeight = (int)(panelHeight * scale);
                int scaledY = panelY + (panelHeight - scaledHeight) / 2;

                int moduleAreaY = scaledY + 30;
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
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
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
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        super.close();
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

    // Prevents ClickGUI from being blurred
    @Override
    public void renderBackground(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }
}