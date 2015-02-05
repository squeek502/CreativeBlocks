package squeek.creativeblocks.commands;

import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import squeek.creativeblocks.config.JSONConfigHandler;

public class CommandCreativeBlocks extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "creativeblocks";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "/creativeblocks reload";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] args)
	{
		if (args.length > 0 && args[0].equalsIgnoreCase("reload"))
		{
			func_152374_a(commandSender, this, 1, "Reloading Creative Blocks from config...");
			JSONConfigHandler.reload();
		}
		else
		{
			throw new WrongUsageException(getCommandUsage(commandSender));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender commandSender, String[] curArgs)
	{
		if (curArgs.length == 1)
			return Arrays.asList(new String[]{"reload"});
		else
			return null;
	}

	@Override
	public int compareTo(Object obj)
	{
		if (obj instanceof ICommand)
			return super.compareTo((ICommand) obj);
		else
			return 0;
	}
}
