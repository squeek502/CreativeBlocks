package squeek.creativeblocks.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;
import squeek.creativeblocks.CreativeBlocks;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlocksSpecifier
{
	public static class ComparableBlock
	{
		public final Block block;
		public final int meta;
		public static final int WILDCARD_META = OreDictionary.WILDCARD_VALUE;

		public ComparableBlock(Block block, int meta)
		{
			this.block = block;
			this.meta = meta;
		}

		public boolean matches(ComparableBlock other)
		{
			return other != null && this.block == other.block && (meta == WILDCARD_META || other.meta == WILDCARD_META || this.meta == other.meta);
		}
	}

	public String unresolvedString = null;
	public List<ComparableBlock> blocks = new ArrayList<ComparableBlock>();

	public boolean contains(Block block, int meta)
	{
		return contains(new ComparableBlock(block, meta));
	}

	public boolean contains(ComparableBlock block)
	{
		return find(block) != null;
	}

	public ComparableBlock find(ComparableBlock block)
	{
		for (ComparableBlock curBlock : blocks)
		{
			if (block.matches(curBlock))
				return curBlock;
		}
		return null;
	}

	public void addFrom(BlocksSpecifier blockSpecifier)
	{
		for (ComparableBlock block : blockSpecifier.blocks)
		{
			if (!contains(block))
				blocks.add(block);
		}
	}

	public void removeFrom(BlocksSpecifier blockSpecifier)
	{
		for (ComparableBlock block : blockSpecifier.blocks)
		{
			for (ComparableBlock match = find(block); match != null; match = find(block))
				blocks.remove(match);
		}
	}

	public static BlocksSpecifier fromString(String unresolvedString)
	{
		BlocksSpecifier blockSpecifier = new BlocksSpecifier();

		if (unresolvedString == null)
			return blockSpecifier;

		blockSpecifier.unresolvedString = unresolvedString;

		String[] itemStringParts = unresolvedString.split(":");

		if (itemStringParts.length < 1)
			return blockSpecifier;

		if (!itemStringParts[0].equals("minecraft") && !Loader.isModLoaded(itemStringParts[0]))
			return blockSpecifier;

		boolean anyMetadata = itemStringParts.length < 3 || itemStringParts[2].equals("*");
		int metadata = anyMetadata ? ComparableBlock.WILDCARD_META : Integer.parseInt(itemStringParts[2]);

		if (itemStringParts[1].equals("*"))
		{
			List<Block> allModBlocks = getAllBlocksOfModID(itemStringParts[0]);
			for (Block block : allModBlocks)
			{
				blockSpecifier.blocks.add(new ComparableBlock(block, metadata));
			}
		}
		else
		{
			Block block = GameRegistry.findBlock(itemStringParts[0], itemStringParts[1]);
			if (block != null)
			{
				blockSpecifier.blocks.add(new ComparableBlock(block, metadata));
			}
			else
				CreativeBlocks.Log.warn("Unable to find block: " + unresolvedString);
		}

		return blockSpecifier;
	}

	@SuppressWarnings("unchecked")
	public static List<Block> getAllBlocksOfModID(String modID)
	{
		List<Block> blocks = new ArrayList<Block>();
		for (String blockName : (Set<String>) GameData.getBlockRegistry().getKeys())
		{
			if (!blockName.startsWith(modID + ":"))
				continue;

			blocks.add(GameData.getBlockRegistry().getObject(blockName));
		}
		return blocks;
	}
}
