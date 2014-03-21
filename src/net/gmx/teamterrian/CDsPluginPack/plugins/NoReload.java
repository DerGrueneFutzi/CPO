package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginLoadEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class NoReload extends CDPlugin
{
	Log clog;
	
	public NoReload(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}
	
	@SuppressWarnings("unchecked")
	@CDPluginEvent
	public void onEnable(CDPluginLoadEvent e)
	{
		try
		{
			clog.log("Try to unregister reload-Command", this);
			SimpleCommandMap scm = handler.cRegister.commandMap;
			Map<String, Command> knownCommands;
			Field f = scm.getClass().getDeclaredField("knownCommands");
			f.setAccessible(true);
			knownCommands = (Map<String, Command>) f.get(scm);
			knownCommands.remove("bukkit:reload");
			knownCommands.remove("reload");
			clog.log("Reload-Command unregistered", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace();
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginCommand(commands = { "reload bukkit.command.reload 1" }, priority = 120)
	public void onCommand(CommandEvent e)
	{
		e.getSender().sendMessage(VarTools.getExclamation(ChatColor.DARK_RED) + "Reload is disabled on this Server");
		e.setCancelled(true);
	}
}
