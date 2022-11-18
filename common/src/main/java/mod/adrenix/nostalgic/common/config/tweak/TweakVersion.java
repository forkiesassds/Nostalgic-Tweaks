package mod.adrenix.nostalgic.common.config.tweak;

import mod.adrenix.nostalgic.util.common.LangUtil;
import net.minecraft.network.chat.Component;

/**
 * This class provides enumeration types that are considered a "change by Minecraft version".
 * This enumeration should be kept server safe, so keep vanilla client-only code out.
 *
 * For tweak types that should be a "change by Minecraft version" see {@link TweakType}.
 *
 * Any enumeration that is used by the server will need to be updated in the serializer.
 * Failing to do so will result in an invalid packet and the tweak can never be changed in multiplayer.
 *
 * @see mod.adrenix.nostalgic.common.config.tweak.TweakSerializer
 */

public abstract class TweakVersion
{
    /**
     * The generic enumeration is used by tweaks that were generally changed between alpha, beta, and modern versions
     * of Minecraft.
     */
    public enum Generic implements DisabledTweak<Generic>
    {
        /* Enumerations */

        ALPHA(LangUtil.Gui.SETTINGS_ALPHA),
        BETA(LangUtil.Gui.SETTINGS_BETA),
        MODERN(LangUtil.Gui.SETTINGS_MODERN);

        /* Fields */

        private final String langKey;

        /* Constructor */

        /**
         * Enumeration constructor.
         * @param langKey A language file key.
         */
        Generic(String langKey) { this.langKey = langKey; }

        /* Methods */

        /**
         * Get the language file key associated with a generic enumeration value.
         * @return A language file key.
         */
        public String getLangKey() { return this.langKey; }

        /**
         * Overrides the toString method so that it returns a translation.
         * @return A translation from a language file.
         */
        @Override
        public String toString() { return Component.translatable(this.langKey).getString(); }

        /**
         * The default generic enumeration is modern.
         * @return A disabled generic enumeration value.
         */
        @Override
        public Generic getDisabledValue() { return MODERN; }
    }

    /**
     * The overlay enumeration is used by tweaks that makes changes to the game's loading overlay. This overlay appears
     * during the game's startup, and when applying resource pack changes.
     */
    public enum Overlay implements DisabledTweak<Overlay>
    {
        /* Enumerations */

        ALPHA(Generic.ALPHA.getLangKey()),
        BETA(Generic.BETA.getLangKey()),
        RELEASE_ORANGE("§61.0§r - §61.6.4"),
        RELEASE_BLACK("§61.7§r - §61.15"),
        MODERN(Generic.MODERN.getLangKey());

        /* Fields */

        private final String langKey;

        /* Constructor */

        /**
         * Enumeration constructor.
         * @param langKey A language file key.
         */
        Overlay(String langKey) { this.langKey = langKey; }

        /* Methods */

        /**
         * Overrides the toString method so that it returns a translation.
         * @return A translation from a language file.
         */
        @Override
        public String toString() { return Component.translatable(this.langKey).getString(); }

        /**
         * The default overlay enumeration is modern.
         * @return A disabled overlay enumeration value.
         */
        @Override
        public Overlay getDisabledValue() { return MODERN; }
    }

    /**
     * The title layout enumeration is used by tweaks that change the button layout of the game's main title screen.
     * Some layouts may differ by just a single button or simple change in text capitalization.
     */
    public enum TitleLayout implements DisabledTweak<TitleLayout>
    {
        /* Enumerations */

        ALPHA(Generic.ALPHA.getLangKey()),
        BETA(Generic.BETA.getLangKey()),
        RELEASE_TEXTURE_PACK("§61.0§r - §61.4.7"),
        RELEASE_NO_TEXTURE_PACK("§61.5.2§r - §61.7.9"),
        MODERN(Generic.MODERN.getLangKey());

        /* Fields */

        private final String langKey;

        /* Constructor */

        /**
         * Enumeration constructor.
         * @param langKey A language file key.
         */
        TitleLayout(String langKey) { this.langKey = langKey; }

        /* Methods */

        /**
         * Overrides the toString method so that it returns a translation.
         * @return A translation from a language file.
         */
        @Override
        public String toString() { return Component.translatable(this.langKey).getString(); }

        /**
         * The default title layout enumeration is modern.
         * @return A disabled title layout enumeration value.
         */
        @Override
        public TitleLayout getDisabledValue() { return MODERN; }
    }

    /**
     * The pause layout enumeration is used by tweaks that change the button layout of the game's pause screen.
     * Some layouts may differ by just a single button or a simple change in text capitalization.
     */
    public enum PauseLayout implements DisabledTweak<PauseLayout>
    {
        /* Enumerations */

        ALPHA_BETA("§aAlpha§r - §eb1.4_01"),
        ACHIEVE_LOWER("§eb1.5§r - §61.0"),
        ACHIEVE_UPPER("§61.1§r - §61.2.5"),
        LAN("§61.3§r - §61.11"),
        ADVANCEMENT("§61.12§r - §61.13.2"),
        MODERN(Generic.MODERN.getLangKey());

        /* Fields */

        private final String langKey;

        /* Constructor */

        /**
         * Enumeration constructor.
         * @param langKey A language file key.
         */
        PauseLayout(String langKey) { this.langKey = langKey; }

        /* Methods */

        /**
         * Overrides the toString method so that it returns a translation.
         * @return A translation from a language file.
         */
        @Override
        public String toString() { return Component.translatable(this.langKey).getString(); }

        /**
         * The default pause layout enumeration is modern.
         * @return A disabled pause layout enumeration value.
         */
        @Override
        public PauseLayout getDisabledValue() { return MODERN; }
    }

    /**
     * The hotbar enumeration is used by tweaks that change the default items loaded into a player's empty inventory
     * after joining a level in creative mode.
     */
    public enum Hotbar implements DisabledTweak<Hotbar>
    {
        /* Enumerations */

        CLASSIC(LangUtil.Gui.SETTINGS_CLASSIC),
        BETA(Generic.BETA.getLangKey()),
        MODERN(Generic.MODERN.getLangKey());

        /* Fields */

        private final String langKey;

        /* Constructor */

        /**
         * Enumeration constructor.
         * @param langKey A language file key.
         */
        Hotbar(String langKey) { this.langKey = langKey; }

        /* Methods */

        /**
         * Overrides the toString method so that it returns a translation.
         * @return A translation from a language file.
         */
        @Override
        public String toString() { return Component.translatable(this.langKey).getString(); }

        /**
         * The default hotbar enumeration is modern.
         * @return A disabled hotbar enumeration value.
         */
        @Override
        public Hotbar getDisabledValue() { return MODERN; }
    }
}
