package com.sushiy.tektopiaaddons;

import net.minecraft.util.math.BlockPos;

public interface IOreFinderVillage {
    boolean hasOreDict(String oreDict);
    BlockPos requestOreDict(String oreDict);
    void releaseOreDictClaim(String oreDict, BlockPos pos);
}
