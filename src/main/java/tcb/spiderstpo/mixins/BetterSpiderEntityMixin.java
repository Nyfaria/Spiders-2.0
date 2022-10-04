package tcb.spiderstpo.mixins;

import java.util.UUID;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import tcb.spiderstpo.common.Config;
import tcb.spiderstpo.common.ModTags;
import tcb.spiderstpo.common.entity.goal.BetterLeapAtTargetGoal;
import tcb.spiderstpo.common.entity.mob.IClimberEntity;
import tcb.spiderstpo.common.entity.mob.IMobEntityRegisterGoalsHook;

@Mixin(value = Spider.class, priority = 1001)
public abstract class BetterSpiderEntityMixin extends Monster implements IClimberEntity, IMobEntityRegisterGoalsHook {

	private static final UUID FOLLOW_RANGE_INCREASE_ID = UUID.fromString("9e815957-3a8e-4b65-afbc-eba39d2a06b4");
	private static final AttributeModifier FOLLOW_RANGE_INCREASE = new AttributeModifier(FOLLOW_RANGE_INCREASE_ID, "Spiders 2.0 follow range increase", 8.0D, AttributeModifier.Operation.ADDITION);

	private boolean pathFinderDebugPreview;

	private BetterSpiderEntityMixin(EntityType<? extends Monster> type, Level worldIn) {
		super(type, worldIn);
	}

	@Inject(method = "<init>*", at = @At("RETURN"))
	private void onConstructed(CallbackInfo ci) {
		this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(FOLLOW_RANGE_INCREASE);
	}

	@Inject(method = "defineSynchedData", at = @At("HEAD"))
	private void onRegisterData(CallbackInfo ci) {
		this.pathFinderDebugPreview = Config.PATH_FINDER_DEBUG_PREVIEW.get();
	}

	@Redirect(method = "registerGoals()V", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V"
			))
	private void onAddGoal(GoalSelector selector, int priority, Goal task) {
		if(task instanceof LeapAtTargetGoal) {
			selector.addGoal(3, new BetterLeapAtTargetGoal<>(this, 0.4f));
		} else if(task instanceof TargetGoal) {
			selector.addGoal(2, ((TargetGoal) task).setUnseenMemoryTicks(200));
		} else {
			selector.addGoal(priority, task);
		}
	}

	@Override
	public boolean shouldTrackPathingTargets() {
		return this.pathFinderDebugPreview;
	}	

	@Override	
	public boolean canClimbOnBlock(BlockState state, BlockPos pos) {
		return !state.getBlock().is(ModTags.NON_CLIMBABLE);
	}

	@Override
	public float getBlockSlipperiness(BlockPos pos) {
		BlockState offsetState = this.level.getBlockState(pos);

		float slipperiness = offsetState.getBlock().getSlipperiness(offsetState, this.level, pos, this) * 0.91f;

		if(offsetState.getBlock().is(ModTags.NON_CLIMBABLE)) {
			slipperiness = 1 - (1 - slipperiness) * 0.25f;
		}

		return slipperiness;
	}

	@Override
	public float getPathingMalus(BlockGetter cache, Mob entity, BlockPathTypes nodeType, BlockPos pos, Vec3i direction, Predicate<Direction> sides) {
		if(direction.getY() != 0) {
			boolean hasClimbableNeigbor = false;

			BlockPos.MutableBlockPos offsetPos = new BlockPos.MutableBlockPos();

			for(Direction offset : Direction.values()) {
				if(sides.test(offset)) {
					offsetPos.set(pos.getX() + offset.getStepX(), pos.getY() + offset.getStepY(), pos.getZ() + offset.getStepZ());

					BlockState state = cache.getBlockState(offsetPos);

					if(this.canClimbOnBlock(state, offsetPos)) {
						hasClimbableNeigbor = true;
					}
				}
			}

			if(!hasClimbableNeigbor) {
				return -1.0f;
			}
		}

		return entity.getPathfindingMalus(nodeType);
	}
}
