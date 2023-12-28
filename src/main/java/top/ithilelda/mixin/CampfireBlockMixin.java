package top.ithilelda.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ithilelda.CampfireXp;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    // minecraft in minecraft's true form: called once for server and client AND each hand... so confusing.
    @Inject(at = @At("TAIL"), method = "onUse", cancellable = true)
    private void getXpWhenUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world instanceof ServerWorld sw && hand == Hand.MAIN_HAND && CampfireXp.XpMap.get(pos) != null) {
            if (player.getStackInHand(hand).isOf(Items.GLASS_BOTTLE) && CampfireXp.XpMap.get(pos) != 0) {
                ExperienceOrbEntity.spawn(sw, player.getPos(), CampfireXp.XpMap.get(pos));
                CampfireXp.XpMap.put(pos, 0);
                cir.setReturnValue(ActionResult.SUCCESS);
            }
            else if (player.getStackInHand(hand).isEmpty()) {
                player.sendMessage(Text.of("This campfire currently stores " + CampfireXp.XpMap.getOrDefault(pos, 0) + " points of xp"));
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), method = "onEntityCollision")
    private void onCollisionSetHitTimer(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        ((LivingEntityAccessor) entity).setPlayerHitTimer(100);
    }
}
