package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tangotek.tektopia.Village;

import javax.annotation.Nullable;

public class TreeOreScanner extends OreDictScanner {
    public TreeOreScanner(Village v, int scansPerTick) {
        super("logWood", v, scansPerTick);
    }

    public BlockPos testBlock(World w, BlockPos bp) {
        IBlockState blockState = w.getBlockState(bp);
        return isLeaf(blockState) ? this.findTreeFromLeaf(w, bp) : null;
    }

    public void scanNearby(BlockPos bp) {
        for(int dy = 2; dy <= 10; dy += 2) {
            for(int dx = -9; dx <= 9; dx += 3) {
                for(int dz = -9; dz <= 9; dz += 3) {
                    if (dx != 0 || dz != 0) {
                        BlockPos scanPos = bp.add(dx, dy, dz);
                        this.scanBlock(scanPos);
                    }
                }
            }
        }

    }

    @Nullable
    protected BlockPos findTreeFromLeaf(World world, BlockPos leafPos) {
        for(BlockPos bp : BlockPos.getAllInBox(leafPos.getX() - 2, leafPos.getY() - 1, leafPos.getZ() - 2, leafPos.getX() + 2, leafPos.getY() - 1, leafPos.getZ() + 2)) {
            BlockPos treePos = treeTest(world, bp);
            if (treePos != null) {
                return treePos;
            }
        }

        return null;
    }

    public static BlockPos treeTest(World world, BlockPos bp) {
        while(isLog(world.getBlockState(bp))) {
            bp = bp.down();
            if (world.getBlockState(bp).getBlock() == Blocks.DIRT) {
                BlockPos treePos = bp.up();
                bp = bp.up(3);

                for(int i = 0; i < 9; ++i) {
                    IBlockState westBlock = world.getBlockState(bp.west());
                    IBlockState eastBlock = world.getBlockState(bp.east());
                    if ((isLeaf(westBlock) || isLog(westBlock)) && (isLeaf(eastBlock) || isLog(eastBlock))) {
                        return treePos;
                    }

                    bp = bp.up();
                }

                return null;
            }
        }

        return null;
    }

    public static boolean isLog(IBlockState blockState) {
        return blockState.getBlock() == Blocks.LOG || blockState.getBlock() == Blocks.LOG2;
    }

    public static boolean isLeaf(IBlockState blockState) {
        return (blockState.getBlock() == Blocks.LEAVES || blockState.getBlock() == Blocks.LEAVES2) && (Boolean)blockState.getValue(BlockLeaves.DECAYABLE);
    }
}
