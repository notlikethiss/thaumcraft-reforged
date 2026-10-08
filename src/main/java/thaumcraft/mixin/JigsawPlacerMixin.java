package thaumcraft.mixin;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.world.gen.VillageTowers;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class JigsawPlacerMixin {
    @Shadow
    @Final
    private List<? super PoolElementStructurePiece> pieces;

    @Redirect(
        method = "tryPlacingChildren",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getShuffledTemplates(Lnet/minecraft/util/RandomSource;)Ljava/util/List;")
    )
    private List<StructurePoolElement> thaumcraft$limitTowers(StructureTemplatePool pool, RandomSource random) {
        List<StructurePoolElement> templates = pool.getShuffledTemplates(random);
        if (VillageTowers.limitReached(this.pieces)) {
            templates.removeIf(VillageTowers::isTower);
        }
        return templates;
    }
}
