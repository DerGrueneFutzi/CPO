package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class ServerLock extends CDPlugin
{
	Log clog;
	String lock = "";
	boolean noBypass = false;
	Map<String, String> messages = new CDHashMap<String, String>();
	String mbeg = ChatColor.AQUA + "[ServerLock] " + ChatColor.GOLD;

	public ServerLock(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.sl.bypass", PermissionDefault.OP),
			new Permission("cdpp.sl.option.all", PermissionDefault.OP),
			new Permission("cdpp.sl.msg.add", PermissionDefault.OP),
			new Permission("cdpp.sl.msg.override", PermissionDefault.OP),
			new Permission("cdpp.sl.msg.del", PermissionDefault.OP),
			new Permission("cdpp.sl.lock", PermissionDefault.OP),
			new Permission("cdpp.sl.lock.message.certain", PermissionDefault.OP),
			new Permission("cdpp.sl.lock.message.uncertain", PermissionDefault.OP),
			new Permission("cdpp.sl.io", PermissionDefault.OP)
		};
	}

	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Loading messages", this);
			load();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	@CDPluginEvent
	public void onDisable(CDPluginDisableEvent e)
	{
		try {
			clog.log("Saving messages", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}

	public String[] getDirectorys() { return new String[] { "ServerLock" }; }

	@CDPluginCommand(commands = { "lock cdpp.sl.lock 0" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		switch(args.length)
		{
			case 0:
				noBypass = false;
				lock(sender);
				return;
			case 1:
				switch(args[0])
				{
					case "all":
						optionAll(sender);
						return;
					case "save":
						save(sender);
						return;
					case "load":
						load(sender);
						return;
					default:
						lock(args[0], sender);
				}
				return;
			case 2:
				switch(args[0])
				{
					case "del":
						del(args[1], sender);
						return;
					case "option":
						switch(args[1])
						{
							case "all":
								optionAll(sender);
								return;
						}
						throw new CDInvalidArgsException(e.getCommand().getName());
				}
				throw new CDInvalidArgsException(e.getCommand().getName());
			default:
				switch(args[0])
				{
					case "add":
						add(args, sender);
						return;
				}
				lock(args, sender);
		}
	}

	@CDPluginEvent
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		if((lock.equals("") || e.getPlayer().hasPermission("cdpp.sl.bypass")) && !noBypass) return;
		e.disallow(Result.KICK_WHITELIST, lock);
	}

	private void optionAll(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.option.all")) {
			sender.sendMessage(ChatColor.RED + "No Permission to change this option");
			return;
		}
		clog.log("Option \"all\" changing to " + !noBypass + " from " + sender.getName(), this);
		noBypass = !noBypass;
		sender.sendMessage(mbeg + "Option all changed. Now it's " + noBypass);
	}
	private void add(String[] args, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.msg.add")) {
			sender.sendMessage(ChatColor.RED + "No Permission to add a message");
			return;
		}
		String toAdd = VarTools.arrToString(args, 2);
		if(messages.containsKey(args[1]) && !sender.hasPermission("cdpp.sl.msg.override")) {
			sender.sendMessage(ChatColor.RED + "No Permission to override a message");
			return;
		}
		clog.log("Adding message \"" + toAdd + "\" with key " + args[1] + " from " + sender.getName(), this);
		messages.put(args[1], toAdd);
		sender.sendMessage(mbeg + "Message added");
		try { save(); }
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving Data. An Exception should have been logged", this);
			sender.sendMessage(mbeg + "The message were added, but there's a problem in saving the data. Please contact an Admin");
		}
	}
	private void del(String key, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.msg.del")) {
			sender.sendMessage(ChatColor.RED + "No Permission to delete a message");
			return;
		}
		clog.log("Removing message \"" + messages.get(key) + "\" with key " + key + " from " + sender.getName(), this);
		messages.remove(key);
		sender.sendMessage(mbeg + "Message removed");
		try { save(); }
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving Data. An Exception should have been logged", this);
			sender.sendMessage(mbeg + "The message were deleted, but there's a problem in saving the data. Please contact an Admin");
		}
	}

	private void lock(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.lock")) {
			sender.sendMessage(ChatColor.RED + "No Permission to lock or unlock the Server");
			return;
		}
		if(!lock.equals(""))
		{
			clog.log("Unlocking Server from " + sender.getName(), this);
			lock = "";
			sender.sendMessage(mbeg + "Server unlocked");
			return;
		}
		clog.log("Locking Server with default from " + sender.getName(), this);
		if(!messages.containsKey("default")) {
			clog.log("Default message not found", this);
			sender.sendMessage(mbeg + "Default message not found");
			return;
		}
		lock = messages.get("default");
		sender.sendMessage(mbeg + "Server locked with default message");
	}
	private void lock(String key, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.lock.message.certain")) {
			sender.sendMessage(ChatColor.RED + "No Permission to lock the Server with a certain message");
			return;
		}
		clog.log("Locking Server with Message-Key " + key, this);
		if(!messages.containsKey(key)) {
			clog.log("MessageKey " + key + " not found", this);
			sender.sendMessage("MessageKey not found");
			return;
		}
		lock = messages.get(key);
		sender.sendMessage(mbeg + "Server locked with " + key + " message");
	}
	private void lock(String[] args, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.lock.message.uncertain")) {
			sender.sendMessage(ChatColor.RED + "No Permission to lock the Server with an specified message");
			return;
		}
		String s = VarTools.arrToString(args, 1);
		clog.log("Locking Server with Message \"" + s + "\" from " + sender.getName(), this);
		lock = s;
		sender.sendMessage(mbeg + "Server locked with your message");
	}

	private boolean save(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.io")) {
			sender.sendMessage("No Permission to use this command");
			return true;
		}
		try {
			save();
			sender.sendMessage(ChatColor.GREEN + "[ServerLock] Messages saved");
		}
		catch (Exception x)
		{
			sender.sendMessage(ChatColor.RED + "[ServerLock] Error while saving");
		}
		return true;
	}
	private boolean load(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sl.io")) {
			sender.sendMessage("No Permission to use this command");
			return true;
		}
		try
		{
			load();
			sender.sendMessage(ChatColor.GREEN + "[ServerLock] Messages loaded");
		}
		catch (Exception x)
		{
			sender.sendMessage(ChatColor.RED + "[ServerLock] Error while loading");
		}
		return true;
	}

	private void save() throws IOException
	{
		clog.log("Start saving Messages", this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagCompound mesCompound = new NBTTagCompound();
		for(String key : messages.keySet())
			mesCompound.set(key, new NBTTagString(messages.get(key)));
		base.set("Messages", mesCompound);
		Data.secureSave(base, CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat", this);
	    clog.log("Messages saved", this);
	}
	private void load() throws IOException
	{
		clog.log("Start loading messages", this);
		clog.log("Clearing intern message list", this);
		messages.clear();
		if(!new File(CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat not found. Returning", this);
			return;
		}
		NBTTagCompound base;
		FileInputStream inputStream = new FileInputStream(CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat");
		base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		NBTTagCompound list = base.getCompound("Messages");
		clog.log("Reading messages to intern message list", this);
		String s;
		for (Object o : list.c())
		{
			s = (String) o;
			messages.put(s, list.getString(s));	
		}
		clog.log("Messages loaded", this);
	}
}