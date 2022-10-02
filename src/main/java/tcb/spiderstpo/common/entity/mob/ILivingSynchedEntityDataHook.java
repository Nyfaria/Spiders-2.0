package tcb.spiderstpo.common.entity.mob;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface ILivingSynchedEntityDataHook {
	public void onNotifyDataManagerChange(EntityDataAccessor<?> key);
}
