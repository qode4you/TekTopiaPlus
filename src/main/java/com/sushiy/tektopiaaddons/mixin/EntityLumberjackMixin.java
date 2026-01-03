package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityLumberjack;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
