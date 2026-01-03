package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.OreDictStack;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityLumberjack;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.crafting.Recipe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EntityLumberjack.class)
public abstract class EntityLumberjackMixin extends EntityVillagerTek {

    public EntityLumberjackMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> CHOP_TREE_ACACIA;
    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> CHOP_TREE_DARK_OAK;
    @Shadow(remap = false)
    protected static boolean hasAxe(EntityVillagerTek villager) {
        return false;
    };

    @Inject(method = "entityInit", at = @At("TAIL"))
    protected void entityInit(CallbackInfo ci) {
        this.registerAIFilter("chop_tree_acacia", CHOP_TREE_ACACIA);
        this.registerAIFilter("chop_tree_dark_oak", CHOP_TREE_DARK_OAK);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticBlock(CallbackInfo ci) {
        CHOP_TREE_ACACIA = EntityDataManager.createKey(EntityLumberjack.class, DataSerializers.BOOLEAN);
        CHOP_TREE_DARK_OAK = EntityDataManager.createKey(EntityLumberjack.class, DataSerializers.BOOLEAN);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    private static List<Recipe> buildCraftSet() {
        List<Recipe> recipes = new ArrayList<>();
        List<OreDictStack> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("logWood"));
        Recipe recipe = new Recipe(ProfessionType.LUMBERJACK, "craft_wooden_axe", 3, new ItemStack(Items.WOODEN_AXE, 1), (List<ItemStack>)(List<?>) ingredients, 1, 1, (v) -> v.getSkillLerp(ProfessionType.LUMBERJACK, 11, 2), 1, (v) -> !hasAxe(v)) {
            public ItemStack craft(EntityVillagerTek villager) {
                ItemStack result = super.craft(villager);
                villager.modifyHappy(-5);
                return result;
            }
        };
        recipes.add(recipe);
        return recipes;
    }
}
