package squeek.creativeblocks.commands;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import squeek.creativeblocks.config.JSONConfigHandler;

import java.util.Collections;
import java.util.List;

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload"))
        {
            notifyOperators(sender, this, 1, "Reloading Creative Blocks from config...");
            JSONConfigHandler.reload();
        }
        else
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return Collections.singletonList("reload");
        }
        else
        {
            return null;
        }
    }

    @Override
    public int compareTo(ICommand p_compareTo_1_)
    {
        return super.compareTo(p_compareTo_1_);
    }
}
