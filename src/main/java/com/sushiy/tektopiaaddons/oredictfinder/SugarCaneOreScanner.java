package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tangotek.tektopia.Village;

public class SugarCaneOreScanner extends OreDictScanner {
    public SugarCaneOreScanner(Village v, int scansPerTick) {
        super("sugarcane", v, scansPerTick);
    }

    public static BlockPos getCaneStalk(World w, BlockPos bp) {
        if (isCane(w.getBlockState(bp))) {
            do {
                bp = bp.down();
            } while(isCane(w.getBlockState(bp)));

            Block downBlock = w.getBlockState(bp.down()).getBlock();
            if (downBlock == Blocks.GLOWSTONE) {
                return null;
            }

            if (isCane(w.getBlockState(bp.up(2)))) {
                return bp.up();
            }
        }

        return null;
    }

    public BlockPos testBlock(World w, BlockPos bp) {
        return getCaneStalk(w, bp);
    }

    public void scanNearby(BlockPos bp) {
        for(BlockPos scanPos : BlockPos.getAllInBox(bp.getX() - 2, bp.getY(), bp.getZ() - 2, bp.getX() + 2, bp.getY(), bp.getZ() + 2)) {
            this.scanBlock(scanPos);
        }

    }

    public static boolean isCane(IBlockState blockState) {
        return blockState.getBlock() == Blocks.REEDS;
    }
}
