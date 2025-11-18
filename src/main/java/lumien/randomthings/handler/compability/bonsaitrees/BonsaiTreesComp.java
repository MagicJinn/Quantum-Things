package lumien.randomthings.handler.compability.bonsaitrees;

import java.lang.reflect.Method;
import java.util.Set;
import org.apache.logging.log4j.Level;

import lumien.randomthings.RandomThings;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class BonsaiTreesComp {
    static final String BONSAITREES_MODID = "bonsaitrees";

    public static void postInit(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded(BONSAITREES_MODID)) {
            RandomThings.logger.log(Level.INFO,
                    "Registering Fertilized Dirt as a Bonsai Trees soil.");
            try {
                // Get the BonsaiTrees instance
                Class<?> bonsaiTreesClass = Class.forName("org.dave.bonsaitrees.BonsaiTrees");
                Object bonsaiTreesInstance = bonsaiTreesClass.getField("instance").get(null);

                Object soilRegistry =
                        bonsaiTreesClass.getField("soilRegistry").get(bonsaiTreesInstance);

                if (soilRegistry == null) {
                    RandomThings.logger.log(Level.WARN,
                            "Bonsai Trees soil registry is not initialized yet. Fertilized Dirt registration will be skipped.");
                    return;
                }

                // Get the IBonsaiSoil interface
                Class<?> soilInterface = Class.forName("org.dave.bonsaitrees.api.IBonsaiSoil");

                // Get the BonsaiSoil class
                Class<?> bonsaiSoilClass = Class.forName("org.dave.bonsaitrees.soils.BonsaiSoil");

                Object fertilizedDirtSoil =
                        bonsaiSoilClass.getConstructor(String.class, ItemStack.class).newInstance(
                                "randomthings:fertilizeddirt",
                                new ItemStack(ModBlocks.fertilizedDirt));

                // Set the modifier speed (3x faster = 0.333f)
                Method setModifierSpeed =
                        bonsaiSoilClass.getMethod("setModifierSpeed", float.class);
                setModifierSpeed.invoke(fertilizedDirtSoil, 0.333f);

                Method setModifierDropChance =
                        bonsaiSoilClass.getMethod("setModifierDropChance", float.class);
                setModifierDropChance.invoke(fertilizedDirtSoil, 1.0f);

                // Set ignore meta to false
                Method setIgnoreMeta = bonsaiSoilClass.getMethod("setIgnoreMeta", boolean.class);
                setIgnoreMeta.invoke(fertilizedDirtSoil, false);

                // Add soil tags
                Method addProvidedTag = bonsaiSoilClass.getMethod("addProvidedTag", String.class);
                addProvidedTag.invoke(fertilizedDirtSoil, "dirt");
                addProvidedTag.invoke(fertilizedDirtSoil, "grass");

                // Verify tags were added (for debugging)
                Method getProvidedTags = soilInterface.getMethod("getProvidedTags");
                Set<String> tags = (Set<String>) getProvidedTags.invoke(fertilizedDirtSoil);
                RandomThings.logger.log(Level.DEBUG,
                        "Fertilized Dirt soil tags: " + tags.toString());

                // Get the IBonsaiIntegration interface
                Class<?> integrationInterface =
                        Class.forName("org.dave.bonsaitrees.api.IBonsaiIntegration");

                // Create a proxy implementation of IBonsaiIntegration
                Object integration = java.lang.reflect.Proxy.newProxyInstance(
                        integrationInterface.getClassLoader(), new Class[] {integrationInterface},
                        (java.lang.reflect.InvocationHandler) (proxy, method, args) -> {
                            // Return default values for interface methods
                            String methodName = method.getName();
                            if (methodName.equals("generateTree")) {
                                return null;
                            } else if (methodName.equals("registerTrees")
                                    || methodName.equals("registerSoils")) {
                                return null;
                            } else if (methodName.equals("modifyTreeShape")) {
                                return null;
                            }
                            if (method.getReturnType().isPrimitive()) {
                                if (method.getReturnType() == boolean.class) {
                                    return false;
                                } else if (method.getReturnType() == int.class) {
                                    return 0;
                                }
                            }
                            return null;
                        });

                // Register the soil
                Method registerMethod = soilRegistry.getClass().getMethod(
                        "registerBonsaiSoilIntegration", integrationInterface, soilInterface);
                registerMethod.invoke(soilRegistry, integration, fertilizedDirtSoil);

                // Get the tree type registry and soil compatibility
                Object typeRegistry =
                        bonsaiTreesClass.getField("typeRegistry").get(bonsaiTreesInstance);
                Object soilCompatibility =
                        bonsaiTreesClass.getField("soilCompatibility").get(bonsaiTreesInstance);

                // Update compatibility to include our newly registered soil
                if (soilCompatibility != null && typeRegistry != null) {
                    Class<?> soilRegistryClass =
                            Class.forName("org.dave.bonsaitrees.soils.BonsaiSoilRegistry");
                    Class<?> typeRegistryClass =
                            Class.forName("org.dave.bonsaitrees.trees.TreeTypeRegistry");

                    Method updateCompatibility = soilCompatibility.getClass()
                            .getMethod("updateCompatibility", soilRegistryClass, typeRegistryClass);
                    updateCompatibility.invoke(soilCompatibility, soilRegistry, typeRegistry);
                }

                RandomThings.logger.log(Level.INFO,
                        "Successfully registered Fertilized Dirt as a Bonsai Trees soil.");
            } catch (Exception e) {
                RandomThings.logger.log(Level.ERROR,
                        "Couldn't register Fertilized Dirt with Bonsai Trees. Error: "
                                + e.getMessage());
                RandomThings.logger.log(Level.DEBUG, e);
            }
        }
    }
}

