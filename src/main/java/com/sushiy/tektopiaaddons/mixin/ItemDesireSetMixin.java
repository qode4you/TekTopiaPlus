package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.item.ItemStack;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.crafting.Recipe;
import net.tangotek.tektopia.storage.ItemDesire;
import net.tangotek.tektopia.storage.ItemDesireSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(value = ItemDesireSet.class)
public abstract class ItemDesireSetMixin {
    @Shadow(remap = false)
    public void addItemDesire(ItemDesire desire){}

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public void addRecipeDesire(Recipe r) {
        for (Object needObj : r.getNeeds()) {
            if (needObj instanceof ItemStack) {
                ItemStack need = (ItemStack) needObj;
                Predicate<ItemStack> pred;
                if (need.getMetadata() == 99) {
                    pred = p -> p.getItem() == need.getItem() && !p.isItemEnchanted();
                } else {
                    pred = p -> ItemStack.areItemsEqual(p, need) && !p.isItemEnchanted();
                }

                Predicate<EntityVillagerTek> shouldNeed = p -> p.isAIFilterEnabled(r.getAiFilter());
                if (r.shouldCraft != null) {
                    shouldNeed = shouldNeed.and(r.shouldCraft);
                }

                this.addItemDesire(new ItemDesire(need.getItem().getTranslationKey(), pred, need.getCount(), need.getCount() * r.idealCount, need.getCount() * r.limitCount, shouldNeed));
            }
        }

    }
}
