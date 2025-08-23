package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.ConfigHandler;
import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIEatFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(value = EntityAIEatFood.class)
public abstract class EntityAIEatFoodMixin {

    @Shadow(remap = false)
    private static void registerFood(Item item, int hunger, int happy) {}

    @Shadow(remap = false)
    private static void registerFood(Item item, int hunger, int happy, BiConsumer<EntityVillagerTek, ItemStack> postEat) {}

    private static final Set<String> MOD_BLACKLIST = new HashSet<>();
    private static final Set<String> ITEM_BLACKLIST = new HashSet<>();
    private static BiConsumer<EntityVillagerTek, ItemStack> returnBowlConsumer;

    static {
        MOD_BLACKLIST.add("minecraft");
        MOD_BLACKLIST.add("tektopia");

        // Blacklist weapon items that might be incorrectly detected as food (bugfix: eating weapons)
        ITEM_BLACKLIST.add("tconstruct:rapier");
        ITEM_BLACKLIST.add("spartanweaponry:rapier");
        ITEM_BLACKLIST.add("spartantwilight:rapier");
        ITEM_BLACKLIST.add("spartanfire:rapier");
        ITEM_BLACKLIST.add("spartanweaponryarcana:rapier");

        // Add other common weapon patterns that might contain food-like names (bugfix: eating weapons)
        ITEM_BLACKLIST.add(":dagger");
        ITEM_BLACKLIST.add(":sword");
        ITEM_BLACKLIST.add(":axe");
        ITEM_BLACKLIST.add(":mace");
        ITEM_BLACKLIST.add(":spear");
        ITEM_BLACKLIST.add(":bow");
        ITEM_BLACKLIST.add(":crossbow");
        ITEM_BLACKLIST.add(":arrow");
        ITEM_BLACKLIST.add(":quiver");
        ITEM_BLACKLIST.add(":shield");
        ITEM_BLACKLIST.add(":hammer");
    }

    // First, capture the original returnBowl consumer from Tektopia's static block
    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/tangotek/tektopia/entities/ai/EntityAIEatFood;returnBowl:Ljava/util/function/BiConsumer;", shift = At.Shift.AFTER), remap = false)
    private static void captureReturnBowl(CallbackInfo ci) {
        try {
            // Use reflection to get the returnBowl field since shadowing might not work
            java.lang.reflect.Field returnBowlField = EntityAIEatFood.class.getDeclaredField("returnBowl");
            returnBowlField.setAccessible(true);
            returnBowlConsumer = (BiConsumer<EntityVillagerTek, ItemStack>) returnBowlField.get(null);
            TektopiaAddons.LOGGER.info("Successfully captured returnBowl consumer");
        } catch (Exception e) {
            TektopiaAddons.LOGGER.error("Failed to capture returnBowl consumer, using fallback", e);
            // Create fallback consumer
            returnBowlConsumer = (villager, stack) -> {
                if (!stack.isEmpty() && stack.getItem() == net.minecraft.init.Items.BOWL) {
                    villager.entityDropItem(new ItemStack(net.minecraft.init.Items.BOWL), 0.0F);
                }
            };
        }
    }

    // Then register modded foods after Tektopia's registration is complete
    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void registerModdedFoods(CallbackInfo ci) {
        TektopiaAddons.LOGGER.info("Starting modded food registration for Tektopia...");

        // First, preserve Tektopia's original food registration by keeping the old logic
        preserveOriginalRegistration();

        int registeredCount = 0;
        int harvestcraftCount = 0;

        // Special handling for HarvestCraft if installed
        if (Loader.isModLoaded("harvestcraft")) {
            TektopiaAddons.LOGGER.info("Pam's HarvestCraft detected - applying enhanced compatibility");
            harvestcraftCount = registerHarvestCraftFoods();
        }

        // Register all food items from all other mods
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null) continue;

            String modId = registryName.getNamespace();
            String itemId = registryName.toString();

            // Skip blacklisted mods and already registered HarvestCraft items
            if (MOD_BLACKLIST.contains(modId) || (modId.equals("harvestcraft") && harvestcraftCount > 0)) {
                continue;
            }

            // Check if item is blacklisted (weapons, tools, etc.)
            if (isBlacklistedItem(itemId)) {
                TektopiaAddons.LOGGER.debug("Skipping blacklisted item: {}", itemId);
                continue;
            }

            // Check if item is food (more comprehensive check with weapon filtering)
            if (isFoodItem(item)) {
                registerFoodItem(item);
                registeredCount++;
            }
        }

        TektopiaAddons.LOGGER.info("Registered {} modded food items for Tektopia ({} from HarvestCraft)",
                registeredCount + harvestcraftCount, harvestcraftCount);
    }

    /**
     * Check if an item should be blacklisted from food registration (bugfix: eating weapons)
     */
    private static boolean isBlacklistedItem(String itemId) {
        for (String blacklistedPattern : ITEM_BLACKLIST) {
            if (itemId.contains(blacklistedPattern)) {
                return true;
            }
        }

        // Additional weapon detection logic (bugfix: eating weapons)
        if (itemId.contains("weapon") || itemId.contains("tool") || itemId.contains("armor") ||
                itemId.contains("_sword") || itemId.contains("_axe") || itemId.contains("_bow") ||
                itemId.contains("rapier") || itemId.contains("dagger") || itemId.contains("spear") ||
                itemId.contains("mace") || itemId.contains("hammer") || itemId.contains("shield")) {
            return true;
        }

        return false;
    }

    /**
     * Preserve the original registration logic from the old code
     */
    private static void preserveOriginalRegistration() {
        // Register standard food items from the old code
        for(ItemFood food : TektopiaAddons.standardFoodItems) {
            if(food.getRegistryName().getNamespace().equals("minecraft")) continue;

            ItemStack stack = new ItemStack(food);
            float happyVal = food.getHealAmount(stack) * 0.5f * food.getSaturationModifier(stack);
            happyVal = happyVal * happyVal;
            happyVal = Math.min(happyVal * ConfigHandler.MODDED_FOOD_HAPPINESS_MULTIPLIER, 100);

            if(TektopiaAddons.cropItems.contains(food)) {
                happyVal = -1;
            }

            float hungerVal = food.getHealAmount(stack) * 0.5f;
            hungerVal = Math.max(hungerVal * 5, hungerVal * hungerVal) * ConfigHandler.MODDED_FOOD_HUNGER_MULTIPLIER;
            hungerVal = Math.min(hungerVal, 100);

            boolean usesBowl = false;
            for(IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
                if(recipe.getRecipeOutput().getItem() == food) {
                    if(recipe.getIngredients().stream().anyMatch(x ->
                            Arrays.stream(x.getMatchingStacks()).anyMatch(y -> y.getItem() == net.minecraft.init.Items.BOWL))) {
                        usesBowl = true;
                        break;
                    }
                }
            }

            if(usesBowl) {
                registerFood(food, Math.round(hungerVal), Math.round(happyVal), returnBowlConsumer);
            } else {
                registerFood(food, Math.round(hungerVal), Math.round(happyVal));
            }
        }

        // Register config food items
        for(Item customfood : TektopiaAddons.configFoodItems.keySet()) {
            registerFood(customfood, TektopiaAddons.configFoodItems.get(customfood).hunger,
                    TektopiaAddons.configFoodItems.get(customfood).happiness);
        }
    }

    /**
     * Special handling for Pam's HarvestCraft foods (bugfix: modded food)
     */
    private static int registerHarvestCraftFoods() {
        int count = 0;
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null || !registryName.getNamespace().equals("harvestcraft")) {
                continue;
            }

            // Use more aggressive detection for HarvestCraft items
            if (isHarvestCraftFood(item)) {
                try {
                    registerFoodItemWithFallback(item, 25, 15);
                    count++;
                } catch (Exception e) {
                    TektopiaAddons.LOGGER.warn("Failed to register HarvestCraft food: {}", registryName, e);
                }
            }
        }
        return count;
    }

    private static boolean isHarvestCraftFood(Item item) {
        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) return false;

        String path = registryName.getPath().toLowerCase();

        // First check if it's a weapon (HarvestCraft might have some tools)
        if (path.contains("tool") || path.contains("weapon") || path.contains("knife") ||
                path.contains("utensil") || path.contains("pot") || path.contains("pan")) {
            return false;
        }

        return path.contains("food") || path.contains("meal") || path.contains("dish") ||
                path.contains("soup") || path.contains("stew") || path.contains("pie") ||
                path.contains("cake") || path.contains("bread") || path.contains("fruit") ||
                path.contains("vegetable") || path.contains("meat") || path.contains("fish") ||
                path.contains("sandwich") || path.contains("burger") || path.contains("pizza") ||
                path.contains("salad") || path.contains("sauce") || path.contains("juice") ||
                path.contains("smoothie") || path.contains("icecream") || path.contains("candy") ||
                path.contains("chocolate") || path.contains("toast") || path.contains("waffle") ||
                path.contains("pancake") || path.contains("donut") || path.contains("cookie");
    }

    private static boolean isFoodItem(Item item) {
        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) return false;

        String itemId = registryName.toString();

        // First check blacklist
        if (isBlacklistedItem(itemId)) {
            return false;
        }

        // Check if it's a standard food item
        if (item instanceof ItemFood) {
            return true;
        }

        ItemStack testStack = new ItemStack(item);

        // Method 1: Reflection check - but only if it's not a weapon
        try {
            Method getHealAmount = item.getClass().getMethod("getHealAmount", ItemStack.class);
            float healAmount = (Float) getHealAmount.invoke(item, testStack);
            if (healAmount > 0) {
                // Additional check: if it has durability, it's probably not food
                if (item.isDamageable()) {
                    TektopiaAddons.LOGGER.debug("Skipping durable item that returns heal amount: {}", itemId);
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            // Continue
        }

        // Method 2: Name pattern check with better filtering
        if (registryName != null) {
            String name = registryName.getPath().toLowerCase();

            // Skip items that contain weapon/tool related words
            if (name.contains("sword") || name.contains("axe") || name.contains("bow") ||
                    name.contains("arrow") || name.contains("shield") || name.contains("dagger") ||
                    name.contains("spear") || name.contains("mace") || name.contains("hammer") ||
                    name.contains("rapier") || name.contains("tool") || name.contains("weapon") ||
                    name.contains("armor") || name.contains("quiver") || name.contains("crossbow")) {
                return false;
            }

            // Only register if it contains clear food indicators
            if (name.contains("food") || name.contains("meal") || name.contains("dish") ||
                    name.contains("soup") || name.contains("stew") ||
                    name.contains("cake") || name.contains("bread") || name.contains("fruit") ||
                    name.contains("vegetable") || name.contains("meat") || name.contains("fish")) {
                return true;
            }

            // For "pie" specifically, make sure it's not part of a weapon name
            if (name.contains("pie") && !name.contains("rapier") && !name.contains("spier")) {
                return true;
            }
        }

        return false;
    }

    private static void registerFoodItemWithFallback(Item item, int defaultHunger, int defaultHappiness) {
        try {
            ItemStack stack = new ItemStack(item);

            int hunger;
            int happiness;

            try {
                hunger = calculateHungerValue(item, stack);
                happiness = calculateHappinessValue(item, stack);
            } catch (Exception e) {
                hunger = defaultHunger;
                happiness = defaultHappiness;
            }

            boolean usesBowl = checkIfUsesBowl(item);

            TektopiaAddons.LOGGER.debug("Registering food: {} - Hunger: {}, Happiness: {}, Uses Bowl: {}",
                    item.getRegistryName(), hunger, happiness, usesBowl);

            if (usesBowl) {
                registerFood(item, hunger, happiness, returnBowlConsumer);
            } else {
                registerFood(item, hunger, happiness);
            }

        } catch (Exception e) {
            TektopiaAddons.LOGGER.error("Failed to register food item: {}", item.getRegistryName(), e);
        }
    }

    private static void registerFoodItem(Item item) {
        registerFoodItemWithFallback(item, 20, 10);
    }

    private static int calculateHungerValue(Item item, ItemStack stack) {
        try {
            float baseHunger = 0;

            if (item instanceof ItemFood) {
                baseHunger = ((ItemFood) item).getHealAmount(stack);
            } else {
                try {
                    Method getHealAmount = item.getClass().getMethod("getHealAmount", ItemStack.class);
                    baseHunger = (Float) getHealAmount.invoke(item, stack);
                } catch (Exception e) {
                    try {
                        Method getFoodLevel = item.getClass().getMethod("getFoodLevel", ItemStack.class);
                        baseHunger = (Integer) getFoodLevel.invoke(item, stack);
                    } catch (Exception ex) {
                        baseHunger = 4;
                    }
                }
            }

            float hunger = baseHunger * 0.5f;
            hunger = Math.max(hunger * 5, hunger * hunger) * ConfigHandler.MODDED_FOOD_HUNGER_MULTIPLIER;
            return Math.min(Math.round(hunger), 100);

        } catch (Exception e) {
            return 20;
        }
    }

    private static int calculateHappinessValue(Item item, ItemStack stack) {
        try {
            float saturation = 0;
            float healAmount = 0;

            if (item instanceof ItemFood) {
                ItemFood food = (ItemFood) item;
                healAmount = food.getHealAmount(stack);
                saturation = food.getSaturationModifier(stack);
            } else {
                try {
                    Method getHealAmount = item.getClass().getMethod("getHealAmount", ItemStack.class);
                    Method getSaturation = item.getClass().getMethod("getSaturationModifier", ItemStack.class);
                    healAmount = (Float) getHealAmount.invoke(item, stack);
                    saturation = (Float) getSaturation.invoke(item, stack);
                } catch (Exception e) {
                    try {
                        Method getFoodLevel = item.getClass().getMethod("getFoodLevel", ItemStack.class);
                        Method getSaturationLevel = item.getClass().getMethod("getSaturation", ItemStack.class);
                        healAmount = (Integer) getFoodLevel.invoke(item, stack);
                        saturation = (Float) getSaturationLevel.invoke(item, stack);
                    } catch (Exception ex) {
                        healAmount = 4;
                        saturation = 0.6f;
                    }
                }
            }

            float happy = healAmount * 0.5f * saturation;
            happy = happy * happy;
            happy = Math.min(happy * ConfigHandler.MODDED_FOOD_HAPPINESS_MULTIPLIER, 100);

            if (TektopiaAddons.cropItems.contains(item)) {
                happy = -1;
            }

            return Math.round(happy);

        } catch (Exception e) {
            return 10;
        }
    }

    private static boolean checkIfUsesBowl(Item item) {
        try {
            for (IRecipe recipe : ForgeRegistries.RECIPES) {
                ItemStack output = recipe.getRecipeOutput();
                if (!output.isEmpty() && output.getItem() == item) {
                    return recipe.getIngredients().stream()
                            .anyMatch(ingredient -> {
                                ItemStack[] matching = ingredient.getMatchingStacks();
                                return matching.length > 0 &&
                                        net.minecraft.init.Items.BOWL == matching[0].getItem();
                            });
                }
            }
        } catch (Exception e) {
            TektopiaAddons.LOGGER.warn("Error checking bowl usage for: {}", item.getRegistryName(), e);
        }

        ResourceLocation registryName = item.getRegistryName();
        if (registryName != null && registryName.getNamespace().equals("harvestcraft")) {
            String path = registryName.getPath().toLowerCase();
            return path.contains("soup") || path.contains("stew") || path.contains("bowl");
        }

        return false;
    }
}