package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tangotek.tektopia.Village;

public class SaplingOreScanner extends OreDictScanner {
    public SaplingOreScanner(Village v, int scansPerTick) {
        super("treeSapling", v, scansPerTick);
    }

    public BlockPos testBlock(World w, BlockPos bp) {
        IBlockState blockState = w.getBlockState(bp);
        return isSapling(blockState) ? bp : null;
    }

    public void scanNearby(BlockPos bp) {
        for(BlockPos scanPos : BlockPos.getAllInBox(bp.getX() - 7, bp.getY() - 2, bp.getZ() - 7, bp.getX() + 7, bp.getY() + 2, bp.getZ() + 7)) {
            this.scanBlock(scanPos);
        }

    }

    public static boolean isSapling(IBlockState blockState) {
        return blockState.getBlock() == Blocks.SAPLING;
    }
}
