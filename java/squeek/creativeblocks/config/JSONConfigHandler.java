package squeek.creativeblocks.config;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import squeek.creativeblocks.CreativeBlocks;
import squeek.creativeblocks.ModInfo;
import squeek.creativeblocks.helpers.FileHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONConfigHandler
{
	private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
	private static List<File> configFiles = new ArrayList<File>();
	public static final String defaultConfigFileName = "default.json";
	public static final String defaultConfigRelativePath = "config/" + defaultConfigFileName;

	public static void setup(File configDirectory)
	{
		File modConfigDirectory = new File(configDirectory, ModInfo.MODID);
		if (!modConfigDirectory.exists())
		{
			modConfigDirectory.mkdirs();
		}
		for (File potentialConfigFile : modConfigDirectory.listFiles())
		{
			if (!FilenameUtils.getExtension(potentialConfigFile.getName()).equalsIgnoreCase("json"))
				continue;

			configFiles.add(potentialConfigFile);
		}
		if (shouldWriteDefaultConfig())
			writeDefaultConfig(modConfigDirectory);
	}

	public static void writeDefaultConfig(File configDirectory)
	{
		File defaultConfigDest = new File(configDirectory, defaultConfigFileName);
		try
		{
			if (CreativeBlocks.instance.sourceFile.isDirectory())
			{
				File sourceFile = new File(CreativeBlocks.instance.sourceFile, defaultConfigRelativePath);
				FileHelper.copyFile(sourceFile, defaultConfigDest, true);
			}
			else
			{
				InputStream defaultConfigInputStream = JSONConfigHandler.class.getClassLoader().getResourceAsStream(defaultConfigRelativePath);
				FileHelper.copyFile(defaultConfigInputStream, defaultConfigDest, true);
				defaultConfigInputStream.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean shouldWriteDefaultConfig()
	{
		// create default if there are no other .json files
		if (configFiles.isEmpty())
			return true;

		// update default if it already exists
		for (File configFile : configFiles)
		{
			if (configFile.getName().equalsIgnoreCase(defaultConfigFileName))
				return true;
		}

		return false;
	}

	public static void load()
	{
		for (File configFile : configFiles)
		{
			try
			{
				FileReader reader = new FileReader(configFile);
				JSONConfigFormat parsedConfig = gson.fromJson(reader, JSONConfigFormat.class);
				if (parsedConfig != null)
				{
					for (String blockToWhitelist : parsedConfig.blockWhitelist)
					{
						CreativeBlocksRegistry.whitelists.add(BlocksSpecifier.fromString(blockToWhitelist));
					}
					for (String blockToBlacklist : parsedConfig.blockBlacklist)
					{
						CreativeBlocksRegistry.blacklists.add(BlocksSpecifier.fromString(blockToBlacklist));
					}
				}
				reader.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

}