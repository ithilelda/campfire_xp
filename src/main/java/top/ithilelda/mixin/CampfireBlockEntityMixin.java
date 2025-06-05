package top.ithilelda.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ithilelda.ICampfireXpExtendedBlockEntity;

import java.util.List;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin implements ICampfireXpExtendedBlockEntity {
    @Unique
    private int experience;

    @Unique
    private int xPTick = 10;

    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void processXp(ServerWorld world, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, ServerRecipeManager.MatchGetter<SingleStackRecipeInput, CampfireCookingRecipe> recipeMatchGetter, CallbackInfo ci) {
        ICampfireXpExtendedBlockEntity extendedEntity = (ICampfireXpExtendedBlockEntity) blockEntity;
        extendedEntity.campfire_xp$setXpTick(extendedEntity.campfire_xp$getXpTick() - 1);
        if(extendedEntity.campfire_xp$getXpTick() <= 0) {
            Box range = new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
            List<ExperienceOrbEntity> orbs = world.getOtherEntities(null, range)
                    .stream()
                    .filter(ExperienceOrbEntity.class::isInstance)
                    .map(ExperienceOrbEntity.class::cast)
                    .toList();
            for (ExperienceOrbEntity orb : orbs) {
                extendedEntity.campfire_xp$setXP(extendedEntity.campfire_xp$getXP() + orb.getValue());
                orb.discard();
            }
            extendedEntity.campfire_xp$setXpTick(10);
        }
    }

    // make the signaling boolean always true to circumvent lithium optimization.
    @ModifyVariable(at = @At("STORE"), method = "litServerTick", ordinal = 0)
    private static boolean modifyBl(boolean original) {
        return true;
    }

    @Inject(at = @At("TAIL"), method = "readNbt")
    private void readXp(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        if (nbt.contains("ExperienceAmount")) {
            ((ICampfireXpExtendedBlockEntity) (Object) this).campfire_xp$setXP(nbt.getInt("ExperienceAmount").orElse(0));
        }
    }

    @Inject(at = @At("TAIL"), method = "writeNbt")
    private void writeXp(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        nbt.putInt("ExperienceAmount", ((ICampfireXpExtendedBlockEntity) (Object) this).campfire_xp$getXP());
    }

    @Override
    @Unique
    public int campfire_xp$getXP() {
        return experience;
    }

    @Override
    @Unique
    public void campfire_xp$setXP(int xp) {
        experience = xp;
    }

    @Override
    public int campfire_xp$getXpTick() {
        return xPTick;
    }

    @Override
    public void campfire_xp$setXpTick(int tick) {
        xPTick = tick;
    }
}