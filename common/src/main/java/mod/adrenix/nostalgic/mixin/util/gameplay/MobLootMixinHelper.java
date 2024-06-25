package mod.adrenix.nostalgic.mixin.util.gameplay;

import mod.adrenix.nostalgic.mixin.access.SheepAccess;
import mod.adrenix.nostalgic.tweak.config.GameplayTweak;
import mod.adrenix.nostalgic.tweak.factory.TweakFlag;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This utility class is used by both the client and server.
 */
public abstract class MobLootMixinHelper
{
    /**
     * Caches an entity type and its associated loot table, so it does not have to be recalculated again.
     */
    private static final Map<EntityType<?>, EntityLoot> LOOT_MAP = new HashMap<>();

    /**
     * Helper record that associates an entity with its tweak controller and old loot table.
     *
     * @param entityType The {@link EntityType} instance.
     * @param tweak      The {@link TweakFlag} controller.
     * @param lootTable  The old {@link LootTable} instance.
     */
    private record EntityLoot(EntityType<?> entityType, TweakFlag tweak, LootTable lootTable)
    {
        EntityLoot
        {
            LOOT_MAP.put(entityType, this);
        }

        LootTable getTable(LootTable vanilla)
        {
            if (this.tweak.get())
                return this.lootTable;

            return vanilla;
        }
    }

    /**
     * Add an item to a loot table builder.
     *
     * @param provider  The {@link HolderLookup.Provider} instance.
     * @param builder   The {@link LootTable.Builder} instance.
     * @param item      The {@link ItemLike} instance to add.
     * @param max       The maximum number of items that can drop from a roll.
     * @param canSmelt  Whether the item should smelt if the entity died while on fire.
     * @return The given {@link LootTable.Builder} instance with the new item entry.
     */
    private static LootTable.Builder addToTable(HolderLookup.Provider provider, LootTable.Builder builder, ItemLike item, int max, boolean canSmelt)
    {
        if (canSmelt)
        {
            return builder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, max)))
                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(provider, UniformGenerator.between(0.0F, 1.0F)))
                    .apply(SmeltItemFunction.smelted()
                        .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                            .flags(EntityFlagsPredicate.Builder.flags().setOnFire(true)))))));
        }

        return builder.withPool(LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, max)))
                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(provider, UniformGenerator.between(0.0F, 1.0F)))));
    }

    /**
     * Make a simple loot table with a single item and a maximum amount to drop.
     *
     * @param item The {@link ItemLike} loot.
     * @param max  The maximum number of items that can drop.
     * @return An old {@link LootTable} instance.
     */
    private static LootTable makeTable(HolderLookup.Provider provider, ItemLike item, int max)
    {
        return addToTable(provider, LootTable.lootTable(), item, max, false).build();
    }

    /**
     * @return A custom old {@link LootTable} instance for pork chops.
     */
    private static LootTable makePorkTable(HolderLookup.Provider provider)
    {
        return addToTable(provider, LootTable.lootTable(), Items.PORKCHOP, 2, true).build();
    }

    /**
     * @return A custom old {@link LootTable} instance for skeletons.
     */
    private static LootTable makeSkeletonTable(HolderLookup.Provider provider)
    {
        LootTable.Builder builder = LootTable.lootTable();

        addToTable(provider, builder, Items.ARROW, 2, false);
        addToTable(provider, builder, Items.BONE, 2, false);

        return builder.build();
    }

    /* Item Tables */

    private static Function<ItemLike, LootTable> WOOL_TABLE;

    /* Helpers */

    /**
     * Initialize the old entity loot tables.
     */
    public static void init(ServerLevel level)
    {
        HolderLookup.Provider provider = level.registryAccess();

        WOOL_TABLE = Util.memoize(item -> makeTable(provider, item, 1));
        LootTable COOKED_PORK_CHOP_TABLE = makeTable(provider, Items.COOKED_PORKCHOP, 2);
        LootTable FEATHER_TABLE = makeTable(provider, Items.FEATHER, 2);
        LootTable STRING_TABLE = makeTable(provider, Items.STRING, 2);
        LootTable LEATHER_TABLE = makeTable(provider, Items.LEATHER, 2);
        LootTable RABBIT_HIDE_TABLE = makeTable(provider, Items.RABBIT_HIDE, 1);
        LootTable PORK_CHOP_TABLE = makePorkTable(provider);
        LootTable ARROW_BONE_TABLE = makeSkeletonTable(provider);

        new EntityLoot(EntityType.ZOMBIFIED_PIGLIN, GameplayTweak.OLD_ZOMBIE_PIGMEN_DROPS, COOKED_PORK_CHOP_TABLE);
        new EntityLoot(EntityType.ZOMBIE, GameplayTweak.OLD_ZOMBIE_DROPS, FEATHER_TABLE);
        new EntityLoot(EntityType.ZOMBIE_VILLAGER, GameplayTweak.OLD_STYLE_ZOMBIE_VILLAGER_DROPS, FEATHER_TABLE);
        new EntityLoot(EntityType.DROWNED, GameplayTweak.OLD_STYLE_DROWNED_DROPS, FEATHER_TABLE);
        new EntityLoot(EntityType.HUSK, GameplayTweak.OLD_STYLE_HUSK_DROPS, FEATHER_TABLE);
        new EntityLoot(EntityType.SPIDER, GameplayTweak.OLD_SPIDER_DROPS, STRING_TABLE);
        new EntityLoot(EntityType.CAVE_SPIDER, GameplayTweak.OLD_STYLE_CAVE_SPIDER_DROPS, STRING_TABLE);
        new EntityLoot(EntityType.COW, GameplayTweak.OLD_COW_DROPS, LEATHER_TABLE);
        new EntityLoot(EntityType.MOOSHROOM, GameplayTweak.OLD_STYLE_MOOSHROOM_DROPS, LEATHER_TABLE);
        new EntityLoot(EntityType.RABBIT, GameplayTweak.OLD_STYLE_RABBIT_DROPS, RABBIT_HIDE_TABLE);
        new EntityLoot(EntityType.CHICKEN, GameplayTweak.OLD_CHICKEN_DROPS, FEATHER_TABLE);
        new EntityLoot(EntityType.PIG, GameplayTweak.OLD_PIG_DROPS, PORK_CHOP_TABLE);
        new EntityLoot(EntityType.STRAY, GameplayTweak.OLD_STYLE_STRAY_DROPS, ARROW_BONE_TABLE);
    }

    /**
     * Get an old entity loot table, or the vanilla loot table if the tweak context is not applicable.
     *
     * @param entity  The {@link Entity} instance to get context from.
     * @param vanilla The entity's original {@link LootTable} instance.
     * @return A {@link LootTable} instance to use.
     */
    public static LootTable getTable(Entity entity, LootTable vanilla)
    {
        EntityType<?> entityType = entity.getType();

        if (LOOT_MAP.containsKey(entityType))
            return LOOT_MAP.get(entityType).getTable(vanilla);

        if (entityType == EntityType.SHEEP && GameplayTweak.OLD_SHEEP_DROPS.get())
        {
            Sheep sheep = (Sheep) entityType.tryCast(entity);

            if (sheep != null && !sheep.isSheared())
                return WOOL_TABLE.apply(SheepAccess.NT$ITEM_BY_DYE().get(sheep.getColor()));
            else
                return LootTable.EMPTY;
        }

        return vanilla;
    }
}
