package top.ithilelda.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import top.ithilelda.ICampfireXpExtendedBlockEntity;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    // minecraft in minecraft's true form: called once for server and client AND each hand... so confusing.
    @Inject(at = @At("HEAD"), method = "onUseWithItem", cancellable = true)
    private void getXpBottleWhenUse(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world instanceof ServerWorld serverWorld && world.getBlockEntity(pos) instanceof CampfireBlockEntity campfireBlockEntity && hand == Hand.MAIN_HAND && player.getStackInHand(hand).isEmpty()) {
            ICampfireXpExtendedBlockEntity extendedEntity = (ICampfireXpExtendedBlockEntity) campfireBlockEntity;
            if (player.isSneaking()) {
                player.addExperience(extendedEntity.campfire_xp$getXP());
                player.sendMessage(Text.of(String.format("Player gets %d points of xp from this campfire.", extendedEntity.campfire_xp$getXP())), true);
                extendedEntity.campfire_xp$setXP(0);
            } else {
                player.sendMessage(Text.of(String.format("This campfire currently stores %d points of xp", extendedEntity.campfire_xp$getXP())), true);
            }
                cir.setReturnValue(ActionResult.SUCCESS_SERVER);
        }
    }

    // make every damage set the player hit timer, so that mobs drop xp orbs.
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;serverDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"), method = "onEntityCollision")
    private void onCollisionSetHitTimer(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, CallbackInfo ci) {
        ((LivingEntityAccessor) entity).setPlayerHitTimer(100);
    }
}
