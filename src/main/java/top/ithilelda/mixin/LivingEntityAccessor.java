package top.ithilelda.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    int getPlayerHitTimer();

    @Accessor
    void setPlayerHitTimer(int playerHitTimer);
}
