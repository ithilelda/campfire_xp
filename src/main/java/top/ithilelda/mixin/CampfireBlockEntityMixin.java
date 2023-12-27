package top.ithilelda.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ithilelda.CampfireXp;

import java.util.List;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    @Unique
    private static final int tickSpacing = 10;
    @Unique
    private static int worldTick = tickSpacing;

    // always server, no need to check.
    @Inject(at = @At("TAIL"), method = "litServerTick")
    private static void gatherXp(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (worldTick-- <= 0) {
            // a 3x3 box around the campfire.
            Box range = new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY(), pos.getZ() + 1);
            List<ExperienceOrbEntity> orbs = world.getOtherEntities(null, range)
                    .stream()
                    .filter(ExperienceOrbEntity.class::isInstance)
                    .map(ExperienceOrbEntity.class::cast)
                    .toList();
            int curAmount = CampfireXp.XpMap.getOrDefault(pos, 0);
            for (ExperienceOrbEntity orb : orbs) {
                curAmount += orb.getExperienceAmount();
                orb.discard();
            }
            CampfireXp.XpMap.put(pos, curAmount);
            worldTick = tickSpacing;
        }
    }

    @Inject(at = @At("TAIL"), method = "readNbt")
    private void readXp(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("ExperienceAmount", NbtElement.INT_TYPE)) {
            int amount = nbt.getInt("ExperienceAmount");
            CampfireXp.XpMap.put(((CampfireBlockEntity) (Object) this).getPos(), amount);
        }
    }

    @Inject(at = @At("TAIL"), method = "writeNbt")
    private void writeXp(NbtCompound nbt, CallbackInfo ci) {
        BlockPos pos = ((CampfireBlockEntity) (Object) this).getPos();
        int amount = CampfireXp.XpMap.getOrDefault(pos, 0);
        nbt.putInt("ExperienceAmount", amount);
    }
}
