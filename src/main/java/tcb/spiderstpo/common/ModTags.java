package tcb.spiderstpo.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;

public class ModTags {
	public static final TagKey<Block> NON_CLIMBABLE = BlockTags.create(new ResourceLocation("spiderstpo:non_climbable"));
}
