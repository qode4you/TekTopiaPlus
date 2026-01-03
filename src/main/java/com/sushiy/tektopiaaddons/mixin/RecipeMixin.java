package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.item.ItemStack;
import net.tangotek.tektopia.ItemTagType;
import net.tangotek.tektopia.ModItems;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.crafting.Recipe;
import net.tangotek.tektopia.storage.VillagerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = Recipe.class)
public abstract class RecipeMixin {
    @Shadow(remap = false)
    private @Final ProfessionType profession;
    @Shadow(remap = false)
    private @Final int skillChance;
    @Shadow(remap = false)
    private @Final ItemStack product;
    @Shadow(remap = false)
    private @Final List<Object> needs;

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public boolean hasItems(EntityVillagerTek villager) {
        for(Object needObj : this.needs) {
            if (needObj instanceof ItemStack) {
                ItemStack itemReq = (ItemStack) needObj;
                int reqCount = villager.getInventory().getItemCount(
                        (Predicate<ItemStack>)p -> p.getItem() == itemReq.getItem() && !p.isItemEnchanted());
                if (reqCount < itemReq.getCount()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public ItemStack craft(EntityVillagerTek villager) {
        boolean nonVillagerItems = false;

        for(Object needObj : this.needs) {
            if (needObj instanceof ItemStack) {
                ItemStack itemReq = (ItemStack) needObj;
                List<ItemStack> items = villager.getInventory().removeItems(
                        (Predicate<ItemStack>)p -> p.getItem() == itemReq.getItem(), itemReq.getCount());
                int total = VillagerInventory.countItems(items);
                if (total != itemReq.getCount()) {
                    return null;
                }

                nonVillagerItems |= items.stream().anyMatch((itemStack) -> !ModItems.isTaggedItem(itemStack, ItemTagType.VILLAGER));
            }
        }

        villager.tryAddSkill(this.profession, this.skillChance);
        villager.debugOut("has crafted: " + this.product.getItem().getTranslationKey());
        ItemStack result = this.product.copy();
        if (!nonVillagerItems) {
            ModItems.makeTaggedItem(result, ItemTagType.VILLAGER);
        }

        return result;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public Predicate<ItemStack> isNeed() {
        return p -> {
            for(Object needObj : this.needs) {
                if (needObj instanceof ItemStack) {
                    ItemStack need = (ItemStack) needObj;
                    if (need.getItem() == p.getItem()) {
                        return true;
                    }
                }
            }

            return false;
        };
    }

    @Accessor(value = "needs", remap = false)
    public abstract List<Object> getNeeds();
}
