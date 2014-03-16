package net.gmx.teamterrian.CDsPluginPack.handle.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class CommandListener
{
	private Log clog;
	PluginHandler handler;
	
	public Map<String, List<String>> commandList = new HashMap<String, List<String>>();
	public Map<String, List<Entry<Object, Method>>> commands = new HashMap<String, List<Entry<Object, Method>>>();
	
	public CommandListener(PluginHandler handler)
	{
		this.handler = handler;
		clog = handler.clog;
	}
	
	public void onCommand(Command c, String[] args, CommandSender sender)
	{
		onCommand(new CommandEvent(c, sender, args));
	}
	public void onCommand(CommandEvent e)
	{
		if(!e.getSender().getName().equals("Moylle"))
		clog.log("Incomming Command '" + e.getCommand().getName() + " " + VarTools.arrToString(e.getArgs(), 0) + "' from " + e.getSender().getName(), this);
		CommandEvent n = checkCommand(e);
		if(n != null) {
			if(!n.getCommand().getName().equalsIgnoreCase("cdpp"))
				onCommand(n); }
		else
			try
			{
				for(String key : commandList.get(e.getCommand().getName().toLowerCase()))
				{
					checkFullCommand(key, e);
					for(Entry<Object, Method> entry : commands.get(key))
						try {
							try { entry.getValue().invoke(entry.getKey(), e); }
							catch (InvocationTargetException x) { handleException((Exception) x.getCause(), e, false, true); }
						}
						catch (Exception x) { handleException(x, e, false, true); }
				}
			}
			catch (Exception x) { handleException(x, e, false, false); } 
	}
	private CommandEvent checkCommand(CommandEvent e)
	{
		if(!e.getCommand().getName().equalsIgnoreCase("cdpp")) return null;
		String[] args = e.getArgs();
		if(args.length == 0) e.getSender().sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "CDsPluginPack " + ChatColor.WHITE + "v" + handler.getCDPP().getDescription().getVersion());
		else return new CommandEvent(new CDCommand(args[0], handler.getCDPP()), e.getSender(), VarTools.subArr(args, 1));
		return e;
	}
	
	private void handleException(Exception x, CommandEvent e, boolean wasHandled, boolean innerLoop)
	{
		try
		{
			if(x == null) return;
			if(wasHandled)
			{
				x.printStackTrace(clog.getStream());
				e.getSender().sendMessage(ChatColor.DARK_RED + "Error while processing command" + (innerLoop ? ". Try to figure out the other Invokes" : ""));
				clog.log("Error while processing a command. An Exception should have been logged" + (innerLoop ? ". Try to figure out the other Invokes" : ""), this);
			}
			else if(x instanceof ClassCastException)
			{
				if(!x.getMessage().replaceAll("org.bukkit.[^ ]*ConsoleSender cannot be cast to org.bukkit.entity.Player", "").equals("")) { handleException(x, e, true, innerLoop); return; }
				clog.log("Console trys to run a command that have to be run as Player", this);
				e.getSender().sendMessage(ChatColor.DARK_RED + "As Console you can't run this command");
				return;
			}
			else if(x instanceof CDException) { ((CDException) x).handle(handler, e); }
			else handleException(x, e, true, innerLoop);
		}
		catch (Exception ex)
		{
			handleException(ex, e, false, innerLoop);
		}
	
	}
	
	private void checkFullCommand(String fullCmd, CommandEvent e) throws CDNoPermissionException
	{
		if(checkFullCommand(fullCmd, e.getSender())) return;
		throw new CDNoPermissionException(fullCmd.split(" ")[2].equals("1"));
	}
	public boolean checkCommand(String parsedCmd, CommandSender sender)
	{
		List<String> l = commandList.get(parsedCmd.toLowerCase());
		if(l == null) return true;
		boolean allowed = true;
		for(String fullCmd : l)
			allowed &= checkFullCommand(fullCmd, sender);
		return allowed;
	}
	public boolean checkFullCommand(String fullCmd, CommandSender sender)
	{
		String[] parsedCmd = fullCmd.split(" ");
		if(sender instanceof ConsoleCommandSender || sender.hasPermission(parsedCmd[1])) return true;
		else return false;
	}
}
