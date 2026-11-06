package lumien.randomthings.entitys;

import java.util.Collections;

import lumien.randomthings.recipes.goldenchicken.GoldenChickenRecipeHandler;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.item.ItemStack;

public class EntityAIGoldenChickenTempt extends EntityAITempt
{
	public EntityAIGoldenChickenTempt(EntityCreature temptedEntity, double speed, boolean scaredByPlayerMovement)
	{
		super(temptedEntity, speed, scaredByPlayerMovement, Collections.emptySet());
	}

	@Override
	protected boolean isTempting(ItemStack stack)
	{
		return GoldenChickenRecipeHandler.findRecipe(stack) != null;
	}
}
