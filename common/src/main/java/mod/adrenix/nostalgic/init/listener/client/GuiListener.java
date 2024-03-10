package mod.adrenix.nostalgic.init.listener.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import mod.adrenix.nostalgic.client.gui.MouseManager;
import mod.adrenix.nostalgic.client.gui.overlay.Overlay;
import mod.adrenix.nostalgic.client.gui.screen.DynamicScreen;
import mod.adrenix.nostalgic.client.gui.screen.vanilla.pause.NostalgicPauseScreen;
import mod.adrenix.nostalgic.client.gui.toast.ModToast;
import mod.adrenix.nostalgic.client.gui.tooltip.Tooltip;
import mod.adrenix.nostalgic.tweak.config.CandyTweak;
import mod.adrenix.nostalgic.tweak.config.GameplayTweak;
import mod.adrenix.nostalgic.tweak.enums.PauseLayout;
import mod.adrenix.nostalgic.util.client.GameUtil;
import mod.adrenix.nostalgic.util.client.KeyboardUtil;
import mod.adrenix.nostalgic.util.client.gui.CornerManager;
import mod.adrenix.nostalgic.util.client.gui.GuiUtil;
import mod.adrenix.nostalgic.util.client.renderer.RenderUtil;
import mod.adrenix.nostalgic.util.common.text.TextUtil;
import mod.adrenix.nostalgic.util.common.world.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public abstract class GuiListener
{
    /**
     * Registers client gui events.
     */
    public static void register()
    {
        ClientGuiEvent.RENDER_PRE.register(GuiListener::setMousePosition);
        ClientGuiEvent.RENDER_POST.register(GuiListener::renderModGraphics);
        ClientGuiEvent.RENDER_HUD.register(GuiListener::renderTextOverlay);
        ClientGuiEvent.SET_SCREEN.register(GuiListener::rerouteScreen);
    }

    /**
     * Reroutes screens to a nostalgic alternative if applicable.
     *
     * @param screen The {@link Screen} that is about to be set.
     * @return A {@link CompoundEventResult} instance.
     */
    private static CompoundEventResult<Screen> rerouteScreen(Screen screen)
    {
        if (screen instanceof PauseScreen)
        {
            boolean isHoldingF3 = KeyboardUtil.isDown(InputConstants.KEY_F3);

            if (CandyTweak.OLD_PAUSE_MENU.get() != PauseLayout.MODERN && !isHoldingF3)
                return CompoundEventResult.interruptTrue(new NostalgicPauseScreen());
        }

        return CompoundEventResult.pass();
    }

    /**
     * Sets the mouse position in {@link MouseManager}.
     *
     * @param screen      The current {@link Screen}.
     * @param graphics    The {@link GuiGraphics} instance.
     * @param mouseX      The x-coordinate of the mouse.
     * @param mouseY      The y-coordinate of the mouse.
     * @param partialTick The normalized progress between two ticks [0.0F, 1.0F].
     * @return The {@link EventResult}.
     */
    private static EventResult setMousePosition(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        MouseManager.setPosition(mouseX, mouseY);

        return EventResult.pass();
    }

    /**
     * Renders any extra graphics provided by the mod onto the screen.
     *
     * @param screen      The current {@link Screen}.
     * @param graphics    The {@link GuiGraphics} instance.
     * @param mouseX      The x-coordinate of the mouse.
     * @param mouseY      The y-coordinate of the mouse.
     * @param partialTick The normalized progress between two ticks [0.0F, 1.0F].
     */
    private static void renderModGraphics(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        Tooltip.render(screen, graphics);
        ModToast.update(screen);

        if (screen instanceof DynamicScreen<?> || screen instanceof Overlay)
            GuiUtil.renderDebug(graphics);

        RenderUtil.flush();
    }

    /**
     * Apply a Minecraft color section code to the given food value.
     *
     * @param food The food value to check.
     * @return The given food value with a Minecraft color § code attached and a reset section code appended.
     */
    private static String getFoodColor(int food)
    {
        if (food <= 2)
            return "§4" + food + "§r";
        else if (food <= 6)
            return "§c" + food + "§r";
        else if (food <= 10)
            return "§6" + food + "§r";
        else if (food <= 15)
            return "§e" + food + "§r";
        else if (food < 20)
            return "§2" + food + "§r";

        return "§a" + food + "§r";
    }

    /**
     * Renders text overlay to the HUD if such tweaks are enabled to do so.
     *
     * @param graphics    The {@link GuiGraphics} instance.
     * @param partialTick The normalized progress between two ticks [0.0F, 1.0F].
     */
    private static void renderTextOverlay(GuiGraphics graphics, float partialTick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen())
            return;

        CornerManager corner = new CornerManager();
        boolean isCreative = PlayerUtil.isCreativeOrSpectator(player);
        boolean isExperienceLevelCreative = isCreative && GameplayTweak.SHOW_XP_LEVEL_IN_CREATIVE.get();
        boolean isExperienceProgressCreative = isCreative && GameplayTweak.SHOW_XP_PROGRESS_IN_CREATIVE.get();

        int foodLevel = player.getFoodData().getFoodLevel();
        int experiencePercent = (int) (player.experienceProgress * 100.0F);
        int saturationPercent = (int) ((player.getFoodData().getSaturationLevel() / 20.0F) * 100.0F);

        RenderUtil.beginBatching();

        if (CandyTweak.OLD_VERSION_OVERLAY.get())
        {
            String text = CandyTweak.OLD_OVERLAY_TEXT.parse(GameUtil.getVersion());
            corner.drawText(graphics, text, CandyTweak.OLD_OVERLAY_CORNER.get());
        }

        if (GameplayTweak.SHOW_XP_LEVEL_TEXT.get() && (!isCreative || isExperienceLevelCreative))
        {
            String text = GameplayTweak.ALT_XP_LEVEL_TEXT.parse(Integer.toString(player.experienceLevel));
            corner.drawText(graphics, text, GameplayTweak.ALT_XP_LEVEL_CORNER.get());
        }

        if (GameplayTweak.SHOW_XP_PROGRESS_TEXT.get() && (!isCreative || isExperienceProgressCreative))
        {
            String percent = GameplayTweak.USE_DYNAMIC_PROGRESS_COLOR.get() ? TextUtil.getPercentColorLow(experiencePercent) : Integer.toString(experiencePercent);
            String text = GameplayTweak.ALT_XP_PROGRESS_TEXT.parse(percent);

            corner.drawText(graphics, text, GameplayTweak.ALT_XP_PROGRESS_CORNER.get());
        }

        if (GameplayTweak.SHOW_HUNGER_FOOD_TEXT.get() && !isCreative)
        {
            String food = GameplayTweak.USE_DYNAMIC_FOOD_COLOR.get() ? getFoodColor(foodLevel) : Integer.toString(foodLevel);
            String text = GameplayTweak.ALT_HUNGER_FOOD_TEXT.parse(food);

            corner.drawText(graphics, text, GameplayTweak.ALT_HUNGER_FOOD_CORNER.get());
        }

        if (GameplayTweak.SHOW_HUNGER_SATURATION_TEXT.get() && !isCreative)
        {
            String saturation = GameplayTweak.USE_DYNAMIC_SATURATION_COLOR.get() ? TextUtil.getPercentColorLow(saturationPercent) : Integer.toString(saturationPercent);
            String text = GameplayTweak.ALT_HUNGER_SATURATION_TEXT.parse(saturation);

            corner.drawText(graphics, text, GameplayTweak.ALT_HUNGER_SATURATION_CORNER.get());
        }

        RenderUtil.endBatching();
    }
}
