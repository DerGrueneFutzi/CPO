package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class RealCommand extends CDPlugin
{
	SimpleCommandMap realCommands = new SimpleCommandMap(null);
	
	public RealCommand(PluginHandler handler)
	{
		super(handler);
		realCommands.setFallbackCommands();
	}
	
	@CDPluginCommand(commands = { "realcmd cdpp.realcmd 1" })
	public void onCommand(CommandEvent e)
	{
		String command = e.getArgs()[0];
		for(Command cmd : realCommands.getCommands())
		{
			if(!cmd.getName().equals(command)) continue;
			cmd.execute(e.getSender(), "", VarTools.subArr(e.getArgs(), 1));
			break;
		}
	}
}
