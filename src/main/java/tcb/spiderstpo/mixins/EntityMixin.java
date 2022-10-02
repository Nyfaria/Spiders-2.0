package tcb.spiderstpo.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tcb.spiderstpo.common.entity.mob.IEntityMovementHook;
import tcb.spiderstpo.common.entity.mob.IEntityReadWriteHook;
import tcb.spiderstpo.common.entity.mob.IEntityRegisterDataHook;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityMovementHook, IEntityReadWriteHook, IEntityRegisterDataHook {
	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void onMovePre(MoverType type, Vec3 pos, CallbackInfo ci) {
		if(this.onMove(type, pos, true)) {
			ci.cancel();
		}
	}

	@Inject(method = "move", at = @At("RETURN"))
	private void onMovePost(MoverType type, Vec3 pos, CallbackInfo ci) {
		this.onMove(type, pos, false);
	}

	@Override
	public boolean onMove(MoverType type, Vec3 pos, boolean pre) {
		return false;
	}

	@Inject(method = "getOnPos", at = @At("RETURN"), cancellable = true)
	private void onGetOnPosition(CallbackInfoReturnable<BlockPos> ci) {
		BlockPos adjusted = this.getAdjustedOnPosition(ci.getReturnValue());
		if(adjusted != null) {
			ci.setReturnValue(adjusted);
		}
	}

	@Override
	public BlockPos getAdjustedOnPosition(BlockPos onPosition) {
		return null;
	}

//todo:figure out
//	@Inject(method = "", at = @At("RETURN"), cancellable = true)
//	private void onCanTriggerWalking(CallbackInfoReturnable<Boolean> ci) {
//		ci.setReturnValue(this.getAdjustedCanTriggerWalking(ci.getReturnValue()));
//	}

	@Override
	public boolean getAdjustedCanTriggerWalking(boolean canTriggerWalking) {
		return canTriggerWalking;
	}

	@Inject(method = "load", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
			shift = At.Shift.AFTER
			))
	private void onRead(CompoundTag nbt, CallbackInfo ci) {
		this.onRead(nbt);
	}

	@Override
	public void onRead(CompoundTag nbt) { }

	@Inject(method = "saveWithoutId", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
			shift = At.Shift.AFTER
			))
	private void onWrite(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> ci) {
		this.onWrite(nbt);
	}

	@Override
	public void onWrite(CompoundTag nbt) { }

	@Shadow(prefix = "shadow$")
	protected void shadow$defineSynchedData() { }

	@Redirect(method = "<init>*", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;defineSynchedData()V"
			))
	private void onRegisterData(Entity _this) {
		this.shadow$defineSynchedData();
		
		if(_this == (Object) this) {
			this.onRegisterData();
		}
	}

	@Override
	public void onRegisterData() { }
}
