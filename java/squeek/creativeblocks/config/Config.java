package squeek.creativeblocks.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import squeek.creativeblocks.ModInfo;

import java.io.File;

public class Config
{
    /** Set to true if 'Creative Blocks' should be one-hit. [Default: false] */
    public static boolean oneHitBreak = false;
    /** Set to true if "pick blocking" in the air should remove the Fabricator in your hand. [Default: true] */
    public static boolean pickBlockAirRemovesFabricator = true;

    private static Configuration configurationFile;

    public static void preInit(FMLPreInitializationEvent event)
    {
        configurationFile = new Configuration(new File(event.getModConfigurationDirectory() + "/" + ModInfo.MODID + "/" + ModInfo.MODID + ".cfg"));
        loadConfigurations();
    }

    public static void loadConfigurations()
    {
        configurationFile.load();
        Property property;
        // General
        {
            property = configurationFile.get(Configuration.CATEGORY_GENERAL, "oneHitBreak", false);
            property.setComment("Set to true if 'Creative Blocks' should be one-hit. [Default: false]");
            oneHitBreak = property.getBoolean();

            property = configurationFile.get(Configuration.CATEGORY_GENERAL, "pickBlockAirRemovesFabricator", true);
            property.setComment("Set to true if \"pick blocking\" in the air should remove the Fabricator in your hand. [Default: true]");
            pickBlockAirRemovesFabricator = property.getBoolean();
        }

        if (configurationFile.hasChanged())
        {
            configurationFile.save();
        }
    }
}