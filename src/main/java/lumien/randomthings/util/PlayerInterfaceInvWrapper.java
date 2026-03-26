package lumien.randomthings.util;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

public class PlayerInterfaceInvWrapper extends InvWrapper {
    public PlayerInterfaceInvWrapper(IInventory inventory) {
        super(inventory);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = super.getStackInSlot(slot);
        // Check if the stack has the Curse Of Binding enchantment
        if (!stack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack) > 0) {
            return ItemStack.EMPTY;
        }

        // No Curse Of Binding detected, continue as normal
        return super.extractItem(slot, amount, simulate);
    }
}