package squeek.creativeblocks.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;

public class CreativeBlocksRegistry
{
	public static List<BlocksSpecifier> blacklists = new ArrayList<BlocksSpecifier>();
	public static List<BlocksSpecifier> whitelists = new ArrayList<BlocksSpecifier>();

	public static boolean isCreativeBlock(Block block, int meta)
	{
		BlocksSpecifier.ComparableBlock comparableBlock = new BlocksSpecifier.ComparableBlock(block, meta);

		for (BlocksSpecifier blacklist : blacklists)
		{
			if (blacklist.contains(comparableBlock))
				return false;
		}

		for (BlocksSpecifier whitelist : whitelists)
		{
			if (whitelist.contains(comparableBlock))
				return true;
		}

		return false;
	}
}
