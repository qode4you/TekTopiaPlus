package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.IOreFinderVillage;
import com.sushiy.tektopiaaddons.oredictfinder.OreDictFinder;
import com.sushiy.tektopiaaddons.oredictfinder.SaplingOreScanner;
import com.sushiy.tektopiaaddons.oredictfinder.SugarCaneOreScanner;
import com.sushiy.tektopiaaddons.oredictfinder.TreeOreScanner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tangotek.tektopia.Village;
import net.tangotek.tektopia.blockfinder.BlockFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Village.class)
public abstract class VillageMixin implements IOreFinderVillage {
    @Shadow(remap = false)
    private World world;

    @Unique
    private OreDictFinder oreDictFinder;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(World worldIn, BlockPos origin, CallbackInfo ci) {
        Village self = (Village) (Object)this;

        this.oreDictFinder = new OreDictFinder();

        this.oreDictFinder.registerOreScanner(new TreeOreScanner(self, 30));
        this.oreDictFinder.registerOreScanner(new SugarCaneOreScanner(self, 15));
        this.oreDictFinder.registerOreScanner(new SaplingOreScanner(self, 15));
    }

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/blockfinder/BlockFinder;update()V"
            ),
            remap = false
    )
    private void replaceBlockFinderUpdate(BlockFinder originalBlockFinder) {
        this.oreDictFinder.update();
    }

    @Unique
    public boolean hasOreDict(String oreName) {
        return this.oreDictFinder != null && this.oreDictFinder.hasOreDict(oreName);
    }

    @Unique
    public BlockPos requestOreDict(String oreName) {
        return this.oreDictFinder != null ? this.oreDictFinder.requestOreDict(oreName) : null;
    }

    @Unique
    public void releaseOreDictClaim(String oreName, BlockPos pos) {
        if (this.oreDictFinder != null) {
            this.oreDictFinder.releaseClaim(this.world, oreName, pos);
        }
    }
}
