package lumien.randomthings.client.gui;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.RandomThings;
import lumien.randomthings.lib.Reference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiModConfig extends GuiConfig {
    public GuiModConfig(GuiScreen parent) {
        super(parent, getConfigElements(), Reference.MOD_ID, false, false,
                Reference.MOD_NAME + " Configuration");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();

        Configuration config = RandomThings.instance.configuration.getConfiguration();

        if (config == null) {
            return list;
        }

        // Ensure all config properties are registered before creating GUI
        // This ensures the Configuration object has all properties registered
        RandomThings.instance.configuration.ensurePropertiesRegistered();

        // Add all categories (alphabetically sorted for consistency)
        String[] categories = {"Divining Rods", "Features", "Internals", "Lotus", "Nature Core",
                "Numbers", "Spectre Coils", "Visual",
                "Voxel Projector", "Worldgen Features", "Worldgen Loot", "Worldgen Plants"};

        for (String category : categories) {
            if (config.hasCategory(category)) {
                net.minecraftforge.common.config.ConfigCategory configCategory =
                        config.getCategory(category);
                // Only add categories that have properties (non-empty categories)
                if (configCategory != null && !configCategory.getValues().isEmpty()) {
                    list.add(new ConfigElement(configCategory));
                }
            }
        }

        return list;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        // Save config before processing the button action
        // This ensures changes are saved whether "Done" or "Cancel" is clicked
        // (though Cancel shouldn't save, but we'll handle that in onGuiClosed)
        if (this.entryList != null && button.id == 2000) { // 2000 is typically "Done"
            // Save all config entries to Property objects
            this.entryList.saveConfigElements();

            // Save the configuration to disk
            Configuration config = RandomThings.instance.configuration.getConfiguration();
            if (config != null) {
                config.save();

                // Sync static fields from the updated Configuration
                RandomThings.instance.configuration.syncStaticFields();
            }
        }

        super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        // Ensure config is saved even if closed another way
        if (this.entryList != null) {
            this.entryList.saveConfigElements();
        }

        Configuration config = RandomThings.instance.configuration.getConfiguration();
        if (config != null) {
            config.save();
            RandomThings.instance.configuration.syncStaticFields();
        }

        super.onGuiClosed();
    }
}

