package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.OreDictStack;
import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityBlacksmith;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.crafting.Recipe;
import net.tangotek.tektopia.storage.ItemDesire;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = EntityBlacksmith.class)
public abstract class EntityBlacksmithMixin extends EntityVillagerTek{


    public EntityBlacksmithMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @Shadow(remap = false)
    private static List<Recipe> craftSetAnvil;

    @Shadow(remap = false) protected abstract boolean canVillagerPickupItem(ItemStack itemIn);

    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> SMELT_MODDED;

    @Inject(method = "entityInit", at = @At("TAIL"))
    protected void entityInit(CallbackInfo ci) {
        this.registerAIFilter("smelt_modded", SMELT_MODDED);
    }
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticBlock(CallbackInfo ci) {
        SMELT_MODDED = EntityDataManager.createKey(EntityBlacksmith.class, DataSerializers.BOOLEAN);
    }

    @Inject(method = "initEntityAI", at = @At("TAIL"))
    protected void initEntityAI(CallbackInfo ci)
    {
        for(ItemStack input : FurnaceRecipes.instance().getSmeltingList().keySet())
        {
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
            if(!result.isEmpty())
            {
                //Check if it is either an ore or an ingot
                if((TektopiaAddons.oreItems.contains(input.getItem()) || TektopiaAddons.ingotItems.contains(input.getItem())))
                {
                    this.getDesireSet().addItemDesire(new ItemDesire(input.getItem(), 0, 8, 16, (x) -> x.isAIFilterEnabled("smelt_modded")));
                    TektopiaAddons.LOGGER.info(TektopiaAddons.MODID + " added itemdesire for blacksmith " + input.getItem().getRegistryName() + "->" + result.getItem().getRegistryName());
                }
            }
        }
    }

    /**
     * @author Sushiy
     * @reason adding a second filter would have been equally viable, but more tasks makes it less clear what the AI is doing
     */
    @Overwrite(remap = false)
    private static Function<ItemStack, Integer> bestSmeltable(EntityVillagerTek villager) {
        return (p) -> {
            if(villager.isAIFilterEnabled("smelt_modded"))
            {
                //Check if there is a smelting recipe for this
                if(FurnaceRecipes.instance().getSmeltingList().containsKey(p))
                {
                    ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(p);
                    if(!stack.isEmpty())
                    {
                        //Check if the result is an ingot and itself is either an ore or an ingot
                        if((TektopiaAddons.oreItems.contains(p.getItem()) || TektopiaAddons.ingotItems.contains(p.getItem())))
                        {
                            if(TektopiaAddons.smithIngotPriority.containsKey(stack.getItem()))
                            {
                                return TektopiaAddons.smithIngotPriority.get(stack.getItem());
                            }
                            return 1;
                        }
                    }
                    return 0;
                }
            }
            if (p.getItem() == Item.getItemFromBlock(Blocks.IRON_ORE) && villager.isAIFilterEnabled("smelt_iron")) {
                return 3;
            } else {
                return p.getItem() == Item.getItemFromBlock(Blocks.IRON_ORE) && villager.isAIFilterEnabled("smelt_gold") ? 2 : 0;
            }
        };
    }


    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    protected Predicate<ItemStack> isDeliverable() {
        return (p) -> craftSetAnvil.stream().anyMatch((e) -> ItemStack.areItemsEqual(e.getProduct(), p) || TektopiaAddons.ingotItems.contains(p.getItem()));
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 0
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondSwordIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 2));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - diamondSword is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 1
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondBootsIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 4));
        System.out.println("buildCraftSetAnvil called - diamondBoots is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 2
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondChestplateIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 8));
        System.out.println("buildCraftSetAnvil called - diamondChestplate is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 3
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondLeggingsIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 7));
        System.out.println("buildCraftSetAnvil called - diamondLeggings is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 4
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondHelmetIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 5));
        System.out.println("buildCraftSetAnvil called - diamondHelmet is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 5
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondAxeIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 3));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - diamondAxe is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 6
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> diamondPickaxeIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("gemDiamond", 3));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - diamondPickaxe is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 7
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironAxeIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 3));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - ironAxe is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 8
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironPickaxeIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 3));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - ironPickaxe is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 9
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironSwordIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 2));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - ironSword is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 10
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironHoeIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 2));
        ingredients.add(new OreDictStack("logWood"));
        System.out.println("buildCraftSetAnvil called - ironHoe is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 11
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> bucketIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 3));
        System.out.println("buildCraftSetAnvil called - bucket is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 12
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> shearsIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 2));
        System.out.println("buildCraftSetAnvil called - shears is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 13
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironBootsIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 4));
        System.out.println("buildCraftSetAnvil called - ironBoots is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 14
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironChestplateIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 8));
        System.out.println("buildCraftSetAnvil called - ironChestplate is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 15
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironLeggingsIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 7));
        System.out.println("buildCraftSetAnvil called - ironLeggings is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "buildCraftSetAnvil",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;I)V",
                    ordinal = 16
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> ironHelmetIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("ingotIron", 5));
        System.out.println("buildCraftSetAnvil called - ironHelmet is done!");

        return (List<ItemStack>)(List<?>) ingredients;
    }
}
