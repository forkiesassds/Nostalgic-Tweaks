package mod.adrenix.nostalgic.fabric.event;

import mod.adrenix.nostalgic.fabric.event.server.ServerEvents;

/**
 * Registers server-side Fabric events.
 */

public abstract class ServerEventHandler
{
    /**
     * Invokes the registration methods of various event group helper classes.
     * Extra instructions may be included in these helper classes.
     */
    public static void register() { ServerEvents.register(); }
}
