package mod.adrenix.nostalgic.mixin.access;

import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TitleScreen.class)
public interface TitleScreenAccess
{
    @Accessor("splash")
    SplashRenderer nt$getSplash();

    @Accessor("realmsNotificationsScreen")
    RealmsNotificationsScreen nt$getRealmsNotificationsScreen();

    @Accessor("panoramaFade")
    float nt$getPanoramaFade();

    @Accessor("COPYRIGHT_TEXT")
    Component nt$getCopyrightText();

    @Invoker("realmsNotificationsEnabled")
    boolean nt$getRealmsNotificationsEnabled();
}
