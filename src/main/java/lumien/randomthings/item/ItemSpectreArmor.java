package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import org.lwjgl.opengl.GL11;

public class ItemSpectreArmor extends ItemArmor {
    public static ItemArmor.ArmorMaterial spectreArmorMaterial =
            EnumHelper.addArmorMaterial("spectre", "randomthings:spectre", 35,
                    new int[] {3, 9, 7, 3}, 22, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);

    public ItemSpectreArmor(EntityEquipmentSlot armorType) {
        super(spectreArmorMaterial, 0, armorType);

        String name;
        switch (armorType) {
            case HEAD:
                name = "spectreHelmet";
                break;
            case CHEST:
                name = "spectreChestplate";
                break;
            case LEGS:
                name = "spectreLeggings";
                break;
            case FEET:
                name = "spectreBoots";
                break;
            default:
                name = "spectreArmor";
                break;
        }

        ItemBase.registerItem(name, this);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot,
            String type) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        int layer = (slot == EntityEquipmentSlot.LEGS) ? 2 : 1;
        String typeSuffix = (type == null) ? "" : "_" + type;
        return String.format("randomthings:textures/models/armor/spectre_layer_%d%s.png", layer,
                typeSuffix);
    }

    @Override
    public int getColor(ItemStack stack) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        return 16777215; // White color
    }

    @Override
    public int getMaxDamage() {
        EntityEquipmentSlot slot = this.armorType;
        switch (slot) {
            case HEAD:
                return 385;
            case CHEST:
                return 560;
            case LEGS:
                return 525;
            case FEET:
                return 455;
            default:
                return 0;
        }
    }
}

