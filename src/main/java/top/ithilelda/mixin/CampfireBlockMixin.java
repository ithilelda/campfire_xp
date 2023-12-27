package top.ithilelda.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ithilelda.CampfireXp;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @Inject(at = @At("TAIL"), method = "onUse")
    private void getXpWhenUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world instanceof ServerWorld serverWorld) {
            if (player.getStackInHand(hand) == ItemStack.EMPTY && CampfireXp.XpMap.get(pos) != null) {
                ExperienceOrbEntity.spawn(serverWorld, pos.toCenterPos(), CampfireXp.XpMap.get(pos));
                CampfireXp.XpMap.put(pos, 0);
            }
        }
    }
}
