package mod.adrenix.nostalgic.mixin.access;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(WorldSelectionList.class)
public interface WorldSelectionListAccess
{
    @Accessor("currentlyDisplayedLevels")
    List<LevelSummary> nt$getCurrentlyDisplayedLevels();
    @Accessor("loadingHeader")
    WorldSelectionList.LoadingHeader nt$getLoadingHeader();
    @Nullable
    @Invoker("pollLevelsIgnoreErrors")
    List<LevelSummary> nt$pollLevelsIgnoreErrors();
    @Invoker("handleNewLevels")
    void nt$handleNewLevels(@Nullable List<LevelSummary> levels);
}
