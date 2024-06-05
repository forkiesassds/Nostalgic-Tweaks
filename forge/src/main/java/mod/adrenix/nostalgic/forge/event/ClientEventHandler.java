package mod.adrenix.nostalgic.forge.event;

import mod.adrenix.nostalgic.NostalgicTweaks;
import mod.adrenix.nostalgic.mixin.util.candy.world.fog.OverworldFogRenderer;
import mod.adrenix.nostalgic.mixin.util.candy.world.fog.VoidFogRenderer;
import mod.adrenix.nostalgic.mixin.util.candy.world.fog.WaterFogRenderer;
import mod.adrenix.nostalgic.tweak.config.CandyTweak;
import mod.adrenix.nostalgic.tweak.config.ModTweak;
import mod.adrenix.nostalgic.util.client.gui.GuiUtil;
import mod.adrenix.nostalgic.util.common.data.FlagHolder;
import mod.adrenix.nostalgic.util.common.data.NullableResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(
    modid = NostalgicTweaks.MOD_ID,
    bus = EventBusSubscriber.Bus.GAME,
    value = Dist.CLIENT
)
public abstract class ClientEventHandler
{
    /* State Holders */

    private static final FlagHolder ARMOR_LEVEL_PUSHED = FlagHolder.off();
    private static final FlagHolder AIR_LEVEL_PUSHED = FlagHolder.off();

    /**
     * Prevents various gui overlays from rendering depending on tweak context.
     *
     * @param event The {@link RenderGuiLayerEvent.Pre} event instance.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void setupHighestGuiLayerPre(RenderGuiLayerEvent.Pre event)
    {
        ResourceLocation name = event.getName();
        GuiGraphics graphics = event.getGuiGraphics();
        LocalPlayer player = Minecraft.getInstance().player;
        Gui gui = Minecraft.getInstance().gui;

        boolean isExperienceOff = CandyTweak.HIDE_EXPERIENCE_BAR.get();
        boolean isFoodOff = CandyTweak.HIDE_HUNGER_BAR.get();
        boolean isMounted = NullableResult.getOrElse(player, false, local -> local.jumpableVehicle() != null);

        if (name == VanillaGuiLayers.HOTBAR)
        {
            if (isExperienceOff)
            {
                gui.leftHeight -= 7;
                gui.rightHeight -= 7;

                if (isMounted)
                {
                    gui.leftHeight += 7;
                    gui.rightHeight += 7;
                }
            }
        }

        if (name == VanillaGuiLayers.EXPERIENCE_BAR && isExperienceOff)
            event.setCanceled(true);

        if (name == VanillaGuiLayers.EXPERIENCE_LEVEL && isExperienceOff)
            event.setCanceled(true);

        if (name == VanillaGuiLayers.FOOD_LEVEL && isFoodOff)
            event.setCanceled(true);

        if (name == VanillaGuiLayers.ARMOR_LEVEL && isFoodOff)
        {
            graphics.pose().pushPose();
            graphics.pose().translate((float) (GuiUtil.getGuiWidth() / 2 + 90), 0.0F, 0.0F);

            ARMOR_LEVEL_PUSHED.enable();
        }

        if (name == VanillaGuiLayers.AIR_LEVEL && isFoodOff)
        {
            graphics.pose().pushPose();
            graphics.pose().translate((float) (GuiUtil.getGuiWidth() / 2 - 100), 0.0F, 0.0F);

            AIR_LEVEL_PUSHED.enable();
        }
    }

    /**
     * Handles the tear-down of previous graphics changes during the overlay pre-phase.
     *
     * @param event The {@link RenderGuiLayerEvent.Post} event instance.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void setupGuiLayerPost(RenderGuiLayerEvent.Post event)
    {
        GuiGraphics graphics = event.getGuiGraphics();

        if (ARMOR_LEVEL_PUSHED.ifEnabledThenDisable())
            graphics.pose().popPose();

        if (AIR_LEVEL_PUSHED.ifEnabledThenDisable())
            graphics.pose().popPose();
    }

    /**
     * Changes various aspects of the world's fog depending on tweak context.
     *
     * @param event The {@link ViewportEvent.RenderFog} event instance.
     */
    @SubscribeEvent
    public static void renderFog(ViewportEvent.RenderFog event)
    {
        if (!ModTweak.ENABLED.get())
            return;

        if (OverworldFogRenderer.setupFog(event.getCamera(), event.getMode(), event::getNearPlaneDistance, event::getFarPlaneDistance, event::setFogShape, event::setNearPlaneDistance, event::setFarPlaneDistance))
            event.setCanceled(true);

        if (WaterFogRenderer.setupFog(event.getCamera(), event::setFogShape, event::setNearPlaneDistance, event::setFarPlaneDistance))
            event.setCanceled(true);

        if (VoidFogRenderer.setupFog(event.getCamera(), event.getMode(), event::getNearPlaneDistance, event::getFarPlaneDistance, event::setNearPlaneDistance, event::setFarPlaneDistance))
            event.setCanceled(true);
    }

    /**
     * Changes the fog color depending on tweak context.
     *
     * @param event The {@link ViewportEvent.ComputeFogColor} event instance.
     */
    @SubscribeEvent
    public static void computeFogColor(ViewportEvent.ComputeFogColor event)
    {
        if (!ModTweak.ENABLED.get())
            return;

        if (WaterFogRenderer.setupColor(event.getCamera(), event::setRed, event::setGreen, event::setBlue))
            return;

        OverworldFogRenderer.setupColor(event.getCamera(), event::getRed, event::getGreen, event::getBlue, event::setRed, event::setGreen, event::setBlue);
        VoidFogRenderer.setupColor(event.getCamera(), event::getRed, event::getGreen, event::getBlue, event::setRed, event::setGreen, event::setBlue);
    }
}
