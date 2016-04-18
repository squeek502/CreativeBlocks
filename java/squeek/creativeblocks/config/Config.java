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

    private static Configuration configurationFile;

    public static void preInit(FMLPreInitializationEvent event)
    {
        configurationFile = new Configuration(new File(event.getModConfigurationDirectory() + "/" + ModInfo.MODID + "/" + ModInfo.MODID + ".cfg"));
        loadConfigurations();
    }

    public static void loadConfigurations()
    {
        configurationFile.load();

        // General
        {
            Property property = configurationFile.get(Configuration.CATEGORY_GENERAL, "oneHitBreak", false);
            property.setComment("Set to true if 'Creative Blocks' should be one-hit. [Default: false]");
            oneHitBreak = property.getBoolean();
        }

        if (configurationFile.hasChanged())
        {
            configurationFile.save();
        }
    }
}