package net.gmx.teamterrian.CDsPluginPack.handle;

import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.CommandListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CDCommand extends Command
{
	private CommandListener clistener;
	
	public CDCommand(String name, CDsPluginPack cdpp)
	{
		super(name);
		this.clistener = cdpp.handler.clistener;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args)
	{
		clistener.onCommand(this, args, sender);
		return true;
	}

}
