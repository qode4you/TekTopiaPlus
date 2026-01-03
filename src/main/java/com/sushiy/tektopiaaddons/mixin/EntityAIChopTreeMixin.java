package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.IOreFinderVillage;
import com.sushiy.tektopiaaddons.oredictfinder.TreeOreScanner;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityLumberjack;
import net.tangotek.tektopia.entities.EntityVillageNavigator;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIChopTree;
import net.tangotek.tektopia.entities.ai.EntityAIMoveToBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = EntityAIChopTree.class)
public abstract class EntityAIChopTreeMixin extends EntityAIMoveToBlock {

    public EntityAIChopTreeMixin(EntityVillageNavigator v) {
        super(v);
    }

    @Shadow(remap = false)
    private @Final EntityVillagerTek villager;
    @Shadow(remap = false)
    private BlockPlanks.EnumType treeType;
    @Shadow(remap = false)
    private BlockPos treePos;
    @Shadow(remap = false)
    private ItemStack bestAxe;
    @Shadow(remap = false)
    private void releaseTreeClaim(){}
    @Shadow(remap = false)
    private int chopTreeOak(BlockPos treePos) {
        return 0;
    }
    @Shadow(remap = false)
    private int chopTreeStraight(BlockPos treePos) {
        return 0;
    }

    protected BlockPos getDestinationBlock() {
        if (this.villager.getVillage() != null) {
            this.releaseTreeClaim();
            BlockPos treePos = ((IOreFinderVillage)this.villager.getVillage()).requestOreDict("logWood");
            if (treePos != null) {
                IBlockState blockState = this.villager.world.getBlockState(treePos);
                String filterName;
                if (blockState.getBlock() == Blocks.LOG) {
                    filterName = "chop_tree_" + blockState.getValue(BlockOldLog.VARIANT).getName();
                }
                else if (blockState.getBlock() == Blocks.LOG2) {
                    filterName = "chop_tree_" + blockState.getValue(BlockNewLog.VARIANT).getName();
                }
                else {
                    filterName = "chop_tree_modded";
                }

                if (this.villager.isAIFilterEnabled(filterName)) {
                    return treePos;
                }
            }
        }

        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    private void chopTree() {
        int logsChopped = 0;
        IBlockState blockState = this.villager.world.getBlockState(this.treePos);

        if (TreeOreScanner.isLog(blockState)) {
            if (blockState.getBlock() == Blocks.LOG) {
                this.treeType = blockState.getValue(BlockOldLog.VARIANT);
            }
            else {
                this.treeType = blockState.getValue(BlockNewLog.VARIANT);
            }

            switch (this.treeType) {
                case OAK:
                    logsChopped = this.chopTreeOak(this.treePos);
                    break;
                case BIRCH:
                case JUNGLE:
                case SPRUCE:
                default:
                    logsChopped = this.chopTreeStraight(this.treePos);
                    break;
            }

            if (logsChopped > 0) {
                this.villager.throttledSadness(-2);
                this.villager.tryAddSkill(ProfessionType.LUMBERJACK, 7);
                this.villager.modifyHunger(-logsChopped / 2);
                this.villager.debugOut("ChopTree [ " + this.treePos.getX() + ", " + this.treePos.getZ() + "] Chopped: " + logsChopped);
            }
        }

    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public boolean shouldExecute() {
        if (this.villager.isAITick() && this.villager.hasVillage() && this.villager.isWorkTime() && ((IOreFinderVillage)this.villager.getVillage()).hasOreDict("logWood")) {
            List<ItemStack> axeList = this.villager.getInventory().getItems(EntityLumberjack.getBestAxe(this.villager), 1);
            if (!axeList.isEmpty()) {
                this.bestAxe = axeList.get(0);
                return super.shouldExecute();
            }

            this.villager.setThought(EntityVillagerTek.VillagerThought.AXE);
            this.bestAxe = null;
        }

        return false;
    }
}
