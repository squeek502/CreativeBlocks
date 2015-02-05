package squeek.creativeblocks.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import squeek.creativeblocks.network.MessageInvalidateCreativeBlocksRegistry;
import squeek.creativeblocks.network.MessageSyncCreativeBlocks;
import squeek.creativeblocks.network.NetworkHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

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

	public static void init()
	{
		FMLCommonHandler.instance().bus().register(new CreativeBlocksRegistry());
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
		// server needs to send creative blocks to the client
		CreativeBlocksRegistry.sync((EntityPlayerMP) event.player);
	}

	public static void invalidate()
	{
		blacklists.clear();
		whitelists.clear();
	}

	public static void sync(EntityPlayerMP player)
	{
		NetworkHandler.channel.sendTo(new MessageInvalidateCreativeBlocksRegistry(), player);

		for (BlocksSpecifier blocksSpecifier : whitelists)
		{
			if (blocksSpecifier.unresolvedString == null || blocksSpecifier.blocks.isEmpty())
				continue;

			NetworkHandler.channel.sendTo(new MessageSyncCreativeBlocks(blocksSpecifier, true), player);
		}
		for (BlocksSpecifier blocksSpecifier : blacklists)
		{
			if (blocksSpecifier.unresolvedString == null || blocksSpecifier.blocks.isEmpty())
				continue;

			NetworkHandler.channel.sendTo(new MessageSyncCreativeBlocks(blocksSpecifier, false), player);
		}
	}

	@SuppressWarnings("unchecked")
	public static void sync()
	{
		for (EntityPlayerMP player : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList)
		{
			sync(player);
		}
	}
}
