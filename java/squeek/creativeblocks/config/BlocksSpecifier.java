package squeek.creativeblocks.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreDictionary;
import squeek.creativeblocks.CreativeBlocks;
import squeek.creativeblocks.network.IPackable;

import java.util.ArrayList;
import java.util.List;

public class BlocksSpecifier implements IPackable
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

    public BlocksSpecifier()
    {
    }

    public BlocksSpecifier(String unresolvedString)
    {
        this.unresolvedString = unresolvedString;
        initializeFromString(unresolvedString);
    }

    protected void initializeFromString(String unresolvedString)
    {
        if (unresolvedString == null)
            return;

        String[] itemStringParts = unresolvedString.split(":");

        if (itemStringParts.length < 1)
            return;

        if (!itemStringParts[0].equals("minecraft") && !Loader.isModLoaded(itemStringParts[0]))
            return;

        boolean anyMetadata = itemStringParts.length < 3 || itemStringParts[2].equals("*");
        int metadata = anyMetadata ? ComparableBlock.WILDCARD_META : Integer.parseInt(itemStringParts[2]);

        if (itemStringParts[1].equals("*"))
        {
            List<Block> allModBlocks = getAllBlocksOfModID(itemStringParts[0]);
            for (Block block : allModBlocks)
            {
                blocks.add(new ComparableBlock(block, metadata));
            }
        }
        else
        {
            ResourceLocation blockToGet = new ResourceLocation(itemStringParts[0], itemStringParts[1]);
            Block block = Block.REGISTRY.getObject(blockToGet);

            if (block != null)
            {
                blocks.add(new ComparableBlock(block, metadata));
            }
            else
                CreativeBlocks.log.warn("Unable to find block: " + unresolvedString);
        }
    }

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

    @SuppressWarnings("unchecked")
    public static List<Block> getAllBlocksOfModID(String modID)
    {
        List<Block> blocks = new ArrayList<Block>();
        for (ResourceLocation rl : Block.REGISTRY.getKeys())
        {
            if (rl.getResourceDomain().equals(modID))
                blocks.add(Block.REGISTRY.getObject(rl));
        }
        return blocks;
    }

    @Override
    public void pack(ByteBuf data)
    {
        ByteBufUtils.writeUTF8String(data, unresolvedString);
    }

    @Override
    public void unpack(ByteBuf data)
    {
        unresolvedString = ByteBufUtils.readUTF8String(data);
        initializeFromString(unresolvedString);
    }
}
