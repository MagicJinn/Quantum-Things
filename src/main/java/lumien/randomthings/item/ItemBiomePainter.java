package lumien.randomthings.item;

import lumien.randomthings.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ItemBiomePainter extends ItemBase {

    public ItemBiomePainter() {
        super("biomePainter");
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return super.getItemStackDisplayName(stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {


        ItemStack capsule = findCapsule(player);

        boolean foundCapsule = !capsule.isEmpty();

        // Only perform world modifications on the server
        // But display success/fail to the client
        if (worldIn.isRemote) {
            return foundCapsule ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }

        Biome biome;
        if (capsule.isEmpty() || (biome = ItemBiomeCapsule.getBiome(capsule)) == null)
            return EnumActionResult.FAIL;

        // We already checked if the capsule is empty
        int heldCharges = ItemBiomeCapsule.getHeldCharges(capsule);

        // Try to set the biome, return FAIL if it fails (already the same biome)
        if (!WorldUtil.setBiome(worldIn, pos, biome))
            return EnumActionResult.FAIL;

        // dont reduce in creative mode
        if (!player.isCreative())
            ItemBiomeCapsule.setHeldCharges(capsule, heldCharges - 1);


        return EnumActionResult.SUCCESS;
    }

    private ItemStack findCapsule(EntityPlayer player) {
        ItemStack offHand = player.getHeldItem(EnumHand.OFF_HAND);
        if (this.isCapsule(offHand) && ItemBiomeCapsule.getHeldCharges(offHand) > 0)
            return offHand;

        ItemStack mainHand = player.getHeldItem(EnumHand.MAIN_HAND);
        if (this.isCapsule(mainHand) && ItemBiomeCapsule.getHeldCharges(mainHand) > 0)
            return mainHand;

        for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = player.inventory.getStackInSlot(i);

            if (this.isCapsule(itemstack) && ItemBiomeCapsule.getHeldCharges(itemstack) > 0)
                return itemstack;
        }

        return ItemStack.EMPTY;
    }

    private boolean isCapsule(ItemStack stack) {
        return stack.getItem() instanceof ItemBiomeCapsule;
    }
}
