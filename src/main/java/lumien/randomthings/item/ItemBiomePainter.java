package lumien.randomthings.item;

import net.minecraft.item.ItemStack;

public class ItemBiomePainter extends ItemBase {

    public ItemBiomePainter() {
        super("biomePainter");
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        // TODO: Implement and remove WIP
        return super.getItemStackDisplayName(stack) + " (WIP)";
    }
}
