package tcb.spiderstpo.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Mob;
import tcb.spiderstpo.common.entity.mob.IMobEntityLivingTickHook;
import tcb.spiderstpo.common.entity.mob.IMobEntityRegisterGoalsHook;
import tcb.spiderstpo.common.entity.mob.IMobEntityTickHook;

@Mixin(Mob.class)
public abstract class MobEntityMixin implements IMobEntityLivingTickHook, IMobEntityTickHook, IMobEntityRegisterGoalsHook {
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void onLivingTick(CallbackInfo ci) {
		this.onLivingTick();
	}

	@Override
	public void onLivingTick() { }

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void onTick(CallbackInfo ci) {
		this.onTick();
	}

	@Override
	public void onTick() { }

	@Shadow(prefix = "shadow$")
	private void shadow$registerGoals() { }

	@Redirect(method = "<init>*", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Mob;registerGoals()V"
			))
	private void onRegisterGoals(Mob _this) {
		this.shadow$registerGoals();

		if(_this == (Object) this) {
			this.onRegisterGoals();
		}
	}

	@Override
	public void onRegisterGoals() { }
}
