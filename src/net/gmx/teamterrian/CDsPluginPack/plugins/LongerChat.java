package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class LongerChat extends CDPlugin
{
	Log clog;
	Map<Player, List<String>> messages = new HashMap<Player, List<String>>();
	
	public LongerChat(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.longerchat", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "lc cdpp.longerchat 1", "lcadd cdpp.longerchat 1", "lcclear cdpp.longerchat 1", "lcrun cdpp.longerchat 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		Player p = Player.getPlayer(e.getSender());
		String[] args = e.getArgs();
		switch(e.getCommand().getName())
		{
			case "lc":
				if(args.length != 0) addMessage(args, p);
				return;
			case "lcadd":
				if(args.length != 0) throw new CDInvalidArgsException(e.getCommand().getName());
				addNew(p);
				return;
			case "lcrun":
				if(args.length != 0) throw new CDInvalidArgsException(e.getCommand().getName());
				runMessages(p);
				return;
			case "lcclear":
				if(args.length != 0) throw new CDInvalidArgsException(e.getCommand().getName());
				delMessages(p);
				return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private void addNew(Player p)
	{
		clog.log("Adding new Line to LC from " + p.getName(), this);
		if(!messages.containsKey(p)) messages.put(p, new ArrayList<String>());
		List<String> add = messages.get(p);
		add.add("");
		messages.put(p, add);
		p.sendMessage(ChatColor.GREEN + "[CDPP][LongerChat] New Message Line added");
	}
	private void addMessage(String[] args, Player p)
	{
		String message = "";
		for(String word : args) message += " " + word;
		if(message.length() == 0) p.sendMessage(ChatColor.RED + "[CDPP][LongerChat] No Input");
		message = message.substring(1);
		if(!p.hasPermission("cdpp.longerchat")) return;
		if(!messages.containsKey(p)) { messages.put(p, new ArrayList<String>()); addNew(p); }
		if(message.charAt(0) == '$') message = " " + message.substring(1);
		if(message.charAt(message.length() - 1) == '$') message = message.substring(0, message.length() - 1) + " ";
		List<String> temp = messages.get(p);
		temp.set(temp.size() - 1, temp.get(temp.size() - 1) + message);
		clog.log("Adding message to LC from " + p.getName() + ": \"" + message, this);
		messages.put(p, temp);
		p.sendMessage(ChatColor.GREEN + "[CDPP][LongerChat] Line successfully added");
		p.sendMessage(ChatColor.GREEN + "[CDPP][LongerChat] Type " + ChatColor.ITALIC + "/lcrun" + ChatColor.RESET + ChatColor.GREEN + " to send the Text");
	}
	private void runMessages(Player p)
	{
		clog.log("Running LC from " + p.getName(), this);
		if(!messages.containsKey(p)) {
			clog.log("Could not run LC from " + p.getName() + " because it is not any text there", this);
			p.sendMessage(ChatColor.RED + "[LongerChat] There is not any Text to send");
			return;
		}
		for(String toRun : messages.get(p))
		{
			if(toRun.charAt(0) == '/' && toRun.length() >= 2)
			{
				toRun = toRun.substring(1);
				clog.log("Running command for " + p.getName() + ": \"" + toRun, this);
				Bukkit.dispatchCommand(p, toRun);
			}
			else {
				clog.log("Sending chat message for " + p.getName() + ": \"" + toRun, this);
				p.chat(toRun);
			}
		}
		delMessages(p);
	}
	private void delMessages(Player p)
	{
		clog.log("Removing LC of " + p.getName(), this);
		messages.remove(p);
		p.sendMessage(ChatColor.GREEN + "[LongerChat] Messages cleared");
	}
}
