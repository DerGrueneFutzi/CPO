package net.gmx.teamterrian.CDsPluginPack.handle;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.CommandListener;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.MyEntry;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class CommandRegister
{
	CDsPluginPack cdpp;
	Log clog;
	CommandListener clistener;
	public SimpleCommandMap commandMap;
	public Collection<Command> cmds = null;
	private List<String> skip;
	
	public CommandRegister(CDsPluginPack cdpp)
	{
		this.cdpp = cdpp;
		this.clog = cdpp.handler.clog;
		this.clistener = cdpp.handler.clistener;
		this.commandMap = getCommandMap();
		this.skip = getSkip();
	}
	
	private List<String> getSkip()
	{
		File f = new File(CDPlugin.getDir() + "/overridden");
		if(!f.exists()) return new ArrayList<String>();
		String file;
		try { file = Data.readFile(CDPlugin.getDir() + "/overridden", Charset.forName("UTF-8"));}
		catch (Exception x) { return new ArrayList<String>(); }
		String [] lines = file.split("(\\r){0,1}(\\n)");
		return VarTools.toList(lines);
	}
	
	public void registerCommands()
	{		
		clog.log("Registering Commands", this);
		List<Entry<Object, Method>> list;
		List<String> aliases;
		String simpleCmd;
		CDPluginCommand pc;
		for(CDPlugin cdp : cdpp.handler.plugins.values())
			for(Method m : cdp.getClass().getMethods())
			{
				if((pc = m.getAnnotation(CDPluginCommand.class)) == null) continue;
				for(String command : pc.commands())
				{
					if(!checkCommand(command)) {
						clog.log("Commands have to have the Syntax 'COMMAND PERMISSION SHOWMSG(0 | 1). '" + command + "' does not have this Syntax and would not be registered.", this);
						continue;
					}
					if((list = clistener.commands.get(command.toLowerCase())) == null) list = new ArrayList<Entry<Object, Method>>();
					list.add(new MyEntry<Object, Method>(cdp, m));
					clistener.commands.put(command.toLowerCase(), list);
					if((aliases = clistener.commandList.get((simpleCmd = parseToCommand(command)))) == null) aliases = new ArrayList<String>();
					aliases.add(command);
					clistener.commandList.put(simpleCmd, aliases);
					clog.log("Registering '" + command + "' Command to " + cdp.getClass().getSimpleName(), this);
				}
			}
		sortCalls();
		registerBukkitCommands();
		clog.log("Done", this);
	}
	
	public void registerBukkitCommands()
	{
		Method m;
		try { m = clistener.getClass().getMethod("onCommand", CommandEvent.class); }
		catch (NoSuchMethodException x) { clog.log("Method CommandListener.onCommand(CommandEvent) not found. Commands are not registered", this); return; }
		registerBukkitCommands(parseToCommands(clistener.commands.keySet()), m);
		registerBukkitCommand("cdpp", m);
	}
	
	private boolean checkCommand(String cmd)
	{
		String[] parts = cmd.split(" ");
		if(parts.length != 3) return false;
		if(!parts[2].equals("0") && !parts[2].equals("1")) return false;
		return true;
	}
	
	private void sortCalls()
	{
		clog.log("Sorting CommandCalls by Priority", this);
		VarTools.sortCalls(clistener.commands, CDPluginCommand.class);
		clog.log("All Packets with Priority between 0 and 10000 sorted", this);
	}
	
	private String parseToCommand(String input)
	{
		return input.split(" ")[0];
	}
	private List<String> parseToCommands(Set<String> set)
	{
		List<String> back = new ArrayList<String>();
		for(String str : set)
			back.add(parseToCommand(str));
		return back;
	}
	
	private void registerBukkitCommands(List<String> commands, Method m)
	{
		for(String command : commands)
			registerBukkitCommand(command, m);
	}
	private void registerBukkitCommand(String command, Method m)
	{
		if(commandMap == null) return;
		if(skip.contains(command)) {
			clog.log("Skipping Command " + command + " because he is marked as overridden", this);
			return;
		}
		clog.log("Registering Command " + command + " to Bukkit", this);
		commandMap.register(command, new CDCommand(command, cdpp));
	}
	
	private SimpleCommandMap getCommandMap()
	{
		clog.log("Getting BukkitCommandMap", this);
		try { return (SimpleCommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer()); }
		catch (Exception x)
		{
			clog.log("Error while getting CommandMap", this);
			return null;
		}
	}
}
