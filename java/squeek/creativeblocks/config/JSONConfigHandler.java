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
		File defaultConfigDest = new File(modConfigDirectory, defaultConfigFileName);
		if (shouldWriteDefaultConfig(defaultConfigDest))
		{
			boolean wasOverwritten = defaultConfigDest.exists();

			writeDefaultConfig(defaultConfigDest);

			if (!defaultConfigDest.exists())
				CreativeBlocks.Log.warn("Default config failed to be written");
			else if (!wasOverwritten)
				configFiles.add(defaultConfigDest);
		}
	}

	public static void writeDefaultConfig(File defaultConfigDest)
	{
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

	public static boolean shouldWriteDefaultConfig(File defaultConfigDest)
	{
		// update default if it already exists
		if (defaultConfigDest.exists())
			return true;
		// create default if there are no other .json files
		else if (configFiles.isEmpty())
			return true;
		// don't reinstate default if it doesn't exist and other .json files do
		else
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
						CreativeBlocksRegistry.whitelists.add(new BlocksSpecifier(blockToWhitelist));
					}
					for (String blockToBlacklist : parsedConfig.blockBlacklist)
					{
						CreativeBlocksRegistry.blacklists.add(new BlocksSpecifier(blockToBlacklist));
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