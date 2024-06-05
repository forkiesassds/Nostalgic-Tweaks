package mod.adrenix.nostalgic.mixin.tweak.candy.item_tooltip;

import mod.adrenix.nostalgic.tweak.config.CandyTweak;
import mod.adrenix.nostalgic.tweak.config.ModTweak;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin
{
    /**
     * Controls which parts of the multiline item tooltip will be added to the tooltip.
     */
    @Inject(
        method = "addToTooltip",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nt_item_tooltip$shouldShowInTooltip(DataComponentType<?> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag, CallbackInfo ci)
    {
        boolean show = !ModTweak.ENABLED.get() || component == DataComponents.UNBREAKABLE || component == DataComponents.STORED_ENCHANTMENTS;

        if (component == DataComponents.DYED_COLOR)
            show = CandyTweak.SHOW_DYE_TIP.get();

        if (component == DataComponents.ENCHANTMENTS)
            show = CandyTweak.SHOW_ENCHANTMENT_TIP.get();

        if (!show)
            ci.cancel();
    }

    /**
     * Controls if the attribute tooltip should be added to the tooltip.
     */
    @Inject(
        method = "addAttributeTooltips",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nt_item_tooltip$shouldShowAttributeInTooltip(CallbackInfo ci)
    {
        if (!CandyTweak.SHOW_MODIFIER_TIP.get())
            ci.cancel();
    }
}
