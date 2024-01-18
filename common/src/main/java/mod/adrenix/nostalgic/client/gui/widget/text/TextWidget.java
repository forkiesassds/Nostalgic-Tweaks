package mod.adrenix.nostalgic.client.gui.widget.text;

import mod.adrenix.nostalgic.client.gui.widget.dynamic.*;
import mod.adrenix.nostalgic.util.client.KeyboardUtil;
import mod.adrenix.nostalgic.util.client.animate.Animation;
import mod.adrenix.nostalgic.util.client.gui.DrawText;
import mod.adrenix.nostalgic.util.client.gui.GuiUtil;
import mod.adrenix.nostalgic.util.client.renderer.RenderUtil;
import mod.adrenix.nostalgic.util.common.CollectionUtil;
import mod.adrenix.nostalgic.util.common.color.Color;
import mod.adrenix.nostalgic.util.common.data.CacheHolder;
import mod.adrenix.nostalgic.util.common.lang.Translation;
import mod.adrenix.nostalgic.util.common.math.MathUtil;
import mod.adrenix.nostalgic.util.common.text.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TextWidget extends DynamicWidget<TextBuilder, TextWidget>
{
    /* Builders */

    /**
     * Begin the process of building a new text widget.
     *
     * @param supplier A supplier that provides a text component that will be shown by a {@link MultiLineLabel}.
     * @return A new {@link TextBuilder} instance.
     */
    public static TextBuilder create(Supplier<Component> supplier)
    {
        return new TextBuilder(supplier);
    }

    /**
     * Begin the process of building a new text widget.
     *
     * @param text The text to be shown by the {@link MultiLineLabel}.
     * @return A new {@link TextBuilder} instance.
     */
    public static TextBuilder create(Component text)
    {
        return new TextBuilder(text);
    }

    /**
     * Begin the process of building a new text widget.
     *
     * @param langKey A {@link Translation} instance to get a translation component from.
     * @return A new {@link TextBuilder} instance.
     */
    public static TextBuilder create(Translation langKey)
    {
        return create(langKey.get());
    }

    /**
     * Begin the process of building a new text widget.
     *
     * @param text The literal text of the widget.
     * @return A new {@link TextBuilder} instance.
     */
    public static TextBuilder create(String text)
    {
        return create(Component.literal(text));
    }

    /* Fields */

    private final LinkedHashSet<ChatFormatting> formatting;
    private final IconManager<TextWidget> iconManager;
    private MultiLineText text;

    /* Constructor */

    protected TextWidget(TextBuilder builder)
    {
        super(builder);

        this.formatting = new LinkedHashSet<>();
        this.iconManager = new IconManager<>(this);

        class SyncText implements DynamicFunction<TextBuilder, TextWidget>
        {
            private final CacheHolder<Component> textSupplier;
            private final CacheHolder<Integer> maxEndX;

            SyncText()
            {
                this.textSupplier = CacheHolder.create(builder.text);
                this.maxEndX = CacheHolder.nullable(builder.maxEndX, IntSupplier::getAsInt, 0);
            }

            @Override
            public void apply(TextWidget text, TextBuilder builder)
            {
                text.createMultiLine();
                this.textSupplier.update();
                this.maxEndX.update();
            }

            @Override
            public boolean isReapplyNeeded(TextWidget text, TextBuilder builder, WidgetCache cache)
            {
                return CacheHolder.isAnyExpired(cache.width, this.textSupplier, this.maxEndX);
            }

            @Override
            public List<DynamicField> getManaging(TextBuilder builder)
            {
                if (builder.useTextWidth)
                    return List.of(DynamicField.WIDTH);

                return List.of();
            }

            @Override
            public DynamicPriority priority()
            {
                return DynamicPriority.HIGH;
            }
        }

        this.getBuilder().addFunction(new SyncText());
        this.createMultiLine();
    }

    /* Methods */

    /**
     * Creates a new {@link MultiLineText} using properties defined in the text's builder and this widget's current
     * layout context.
     */
    public void createMultiLine()
    {
        Component component = this.getBuilder().text.get();

        if (this.getBuilder().useEllipsis)
            component = TextUtil.ellipsis(GuiUtil.font()::width, component, this.getWidth());

        Component cleaned = component.getString().contains("§") ? this.getCleanText(component) : component;
        FormattedText format = cleaned.copy().withStyle(this.formatting.toArray(ChatFormatting[]::new));

        if (this.getBuilder().useTextWidth)
        {
            int maxLineWidth = format.getString().lines().mapToInt(GuiUtil.font()::width).max().orElse(0);
            int maxIconWidth = this.getIconWidth();
            int maxWidth = maxLineWidth + maxIconWidth;

            if (this.getBuilder().maxEndX != null)
            {
                int maxEndX = this.getBuilder().maxEndX.getAsInt();

                if (maxEndX < this.getX() + maxWidth)
                    maxWidth = Math.abs(maxEndX - this.getX());
            }

            this.setWidth(maxWidth);
        }

        this.setAndUpdate(MultiLineText.create(format, this.width - this.getIconWidth()));
    }

    /**
     * Get a properly formatted component for {@link MultiLineText}. If a translation string includes {@code §r}, then
     * the formatting will be broken. This method's purpose is to replace {@code §r} with {@code §r§f} since this fixes
     * broken formatting and replicates the intended action.
     *
     * @param text The {@link Component} text to format.
     * @return A properly formatted {@link Component} for the {@link MultiLineText} label.
     */
    private Component getCleanText(Component text)
    {
        return Component.literal(text.getString().replaceAll("§r", "§r§f"));
    }

    /**
     * @return Get the width of the assigned icon and its margin spacing.
     */
    private int getIconWidth()
    {
        if (this.iconManager.isEmpty())
            return 0;

        return this.iconManager.getWidth() + this.getBuilder().iconMargin;
    }

    /**
     * Set this widget's text and update the widget's height.
     *
     * @param text A {@link MultiLineText} instance.
     */
    private void setAndUpdate(MultiLineText text)
    {
        this.text = text;

        this.setHeight(this.text.getCount() * this.getBuilder().lineHeight);
    }

    /**
     * @return Whether this widget is underlined.
     */
    public boolean isUnderlined()
    {
        if (this.getBuilder().disableUnderline)
            return false;

        return this.isHoveredOrFocused() && this.getBuilder().onPress != null;
    }

    /**
     * @return Whether this widget is using italics.
     */
    public boolean isItalic()
    {
        return this.getBuilder().italic.getAsBoolean();
    }

    /**
     * Get the font color to use during rendering.
     *
     * @param isHighlightPass Whether this is highlight render pass.
     * @return A {@link Color} instance.
     */
    private Color getTextColor(boolean isHighlightPass)
    {
        if (this.getBuilder().highlighter == null)
        {
            if (this.getBuilder().onPress != null && this.isHoveredOrFocused())
                return this.getBuilder().clickableColor;

            return this.getBuilder().fontColor;
        }

        if (isHighlightPass)
        {
            Animation animation = this.getBuilder().highlighter;

            if (this.isHoveredOrFocused())
                animation.play();
            else
                animation.rewind();

            double override = this.isFocused() ? 1.0D : 0.0D;
            double value = this.getBuilder().highlightIf.getAsBoolean() ? animation.getValue() : override;
            double alpha = Mth.clamp((float) value, 0.0F, 1.0F);

            // The game adjusts font colors so that they aren't fully transparent - this accounts for that
            if (alpha > (4.0D / 255.0D))
                return this.getBuilder().clickableColor.fromAlpha(alpha);
        }

        return this.getBuilder().fontColor;
    }

    /**
     * Center a multi-text line based on width. The result will not be relative to the x-coordinate of this widget.
     *
     * @param line  A {@link MultiLineText.Line} instance.
     * @param index The index of the line within the lines list.
     * @return A centered float using this widget's width and the line's width.
     */
    private float getCenteredLine(MultiLineText.Line line, int index)
    {
        int lineWidth = line.getWidth();

        if (index == 0 && this.iconManager.isPresent())
            lineWidth -= this.getIconWidth();

        return (this.getWidth() / 2.0F) - lineWidth / 2.0F;
    }

    /**
     * Add formatting to the text label.
     *
     * @param formatting A chat formatting varargs.
     */
    private void addFormatting(ChatFormatting... formatting)
    {
        this.formatting.addAll(List.of(formatting));
        this.createMultiLine();
    }

    /**
     * Clears all formatting from the text label.
     */
    private void clearFormatting()
    {
        this.formatting.clear();
        this.createMultiLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (KeyboardUtil.isEnter(keyCode) && this.isFocused() && this.getBuilder().onPress != null)
        {
            if (this.getBuilder().useClickSound)
                GuiUtil.playClick();

            this.getBuilder().onPress.run();

            return true;
        }

        return false;
    }

    /**
     * @return Whether the mouse is within the press area of this text widget.
     */
    protected boolean isMouseInPressArea()
    {
        if (this.getBuilder().pressArea == null)
            return false;

        int aX = this.getBuilder().pressArea.getX();
        int aY = this.getBuilder().pressArea.getY();
        int aW = this.getBuilder().pressArea.getWidth();
        int aH = this.getBuilder().pressArea.getHeight();

        return MathUtil.isWithinBox(this.getMouseX(), this.getMouseY(), aX, aY, aW, aH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return this.isMouseInPressArea() || super.isMouseOver(mouseX, mouseY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.getBuilder().onPress == null || this.isInvalidClick(mouseX, mouseY, button))
            return false;

        if (this.getBuilder().intersections.stream().anyMatch(widget -> widget.mouseClicked(mouseX, mouseY, button)))
            return false;

        boolean isAreaClicked = this.isMouseInPressArea();
        boolean isTextClicked = this.isValidClick(mouseX, mouseY, button);

        if (isAreaClicked || isTextClicked)
        {
            if (this.getBuilder().useClickSound)
                GuiUtil.playClick();

            this.getBuilder().onPress.run();

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHoveredOrFocused()
    {
        if (CollectionUtil.isNotEmpty(this.getBuilder().intersections))
        {
            boolean isOverIntersection = this.getBuilder().intersections.stream()
                .anyMatch(widget -> widget.isMouseOver(this.getMouseX(), this.getMouseY()));

            if (isOverIntersection)
                return false;
        }

        return super.isHoveredOrFocused();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (this.isInvisible())
            return;

        RenderUtil.beginBatching();

        if (this.getBuilder().backgroundColor != null)
            RenderUtil.fill(graphics, this.getX(), this.getY(), this.getEndX(), this.getEndY(), this.getBuilder().backgroundColor);

        this.renderText(graphics, mouseX, mouseY, partialTick, false);

        if (this.getBuilder().highlighter != null)
            this.renderText(graphics, mouseX, mouseY, partialTick, true);

        RenderUtil.endBatching();

        this.renderDebug(graphics);
    }

    /**
     * Handler method that prevents background rendering of a text widget.
     *
     * @param graphics        A {@link GuiGraphics} instance.
     * @param mouseX          The x-position of the mouse.
     * @param mouseY          The y-position of the mouse.
     * @param partialTick     The normalized progress between two ticks [0.0F, 1.0F].
     * @param isHighlightPass Whether this is a highlighter render pass.
     */
    private void renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, boolean isHighlightPass)
    {
        if (this.isUnderlined())
            this.addFormatting(ChatFormatting.UNDERLINE);

        if (this.isItalic())
            this.addFormatting(ChatFormatting.ITALIC);

        this.iconManager.pushCache();

        if (this.getBuilder().isCenterAligned)
        {
            float centerY = MathUtil.center(this.getY(), this.text.getCount() * GuiUtil.textHeight(), this.getHeight());

            graphics.pose().pushPose();
            graphics.pose().translate(this.getX(), centerY, 0.0F);

            CollectionUtil.forLoop(this.text.getLines(), (line, index) -> DrawText.begin(graphics, line.getText())
                .pos(this.getCenteredLine(line, index), index * this.getBuilder().lineHeight)
                .color(this.getTextColor(isHighlightPass))
                .draw());

            if (!this.text.getLines().isEmpty())
            {
                int posX = (int) this.getCenteredLine(this.text.getLines().get(0), 0) - this.getIconWidth();

                this.iconManager.pos(posX, 0);
                this.iconManager.render(graphics, mouseX, mouseY, partialTick);
            }

            graphics.pose().popPose();
        }
        else
        {
            this.iconManager.get().pos(this.getX(), this.getY());
            this.iconManager.render(graphics, mouseX, mouseY, partialTick);

            graphics.pose().pushPose();
            graphics.pose().translate(this.getX() + this.getIconWidth(), this.getY(), 0.0F);

            CollectionUtil.forLoop(this.text.getLines(), (line, index) -> DrawText.begin(graphics, line.getText())
                .posY(index * this.getBuilder().lineHeight)
                .color(this.getTextColor(isHighlightPass))
                .draw());

            graphics.pose().popPose();
        }

        if (this.getBuilder().useSeparator)
        {
            Color color = this.getBuilder().separatorColor;
            int barHeight = this.getBuilder().separatorHeight.getAsInt();
            int textWidth = this.iconManager.getWidth() + this.text.maxWidth();
            float centerY = MathUtil.center(this.getY(), barHeight, this.getHeight());
            float padding = 4.0F;

            if (this.getBuilder().isCenterAligned)
            {
                int barWidth = Math.abs(this.getWidth() - textWidth) / 2;

                graphics.pose().pushPose();
                graphics.pose().translate(this.getX(), centerY, 0.0F);

                RenderUtil.fill(graphics, 0, 0, barWidth - padding, barHeight, color);

                graphics.pose().translate(barWidth + textWidth + padding, 0.0F, 0.0F);

                RenderUtil.fill(graphics, 0, 0, barWidth - padding, barHeight, color);

                graphics.pose().popPose();
            }
            else
                RenderUtil.fill(graphics, this.getX() + textWidth + padding, centerY, this.getEndX(), centerY + barHeight, color);
        }

        if (CollectionUtil.isNotEmpty(this.formatting))
            this.clearFormatting();

        this.iconManager.popCache();
    }
}
