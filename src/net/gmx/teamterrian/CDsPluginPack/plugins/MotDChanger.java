package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagString;

public class MotDChanger extends CDPlugin
{
	Log clog;
	String motd = "";
	Map<String, String> messages = new CDHashMap<String, String>();
	String mbeg = ChatColor.AQUA + "[MotDChanger] " + ChatColor.GOLD;
	
	public MotDChanger(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.motdc.msg.add", PermissionDefault.OP),
			new Permission("cdpp.motdc.msg.override", PermissionDefault.OP),
			new Permission("cdpp.motdc.msg.del", PermissionDefault.OP),
			new Permission("cdpp.motdc.set", PermissionDefault.OP),
			new Permission("cdpp.motdc.lock.message.certain", PermissionDefault.OP),
			new Permission("cdpp.motdc.lock.message.uncertain", PermissionDefault.OP),
			new Permission("cdpp.motdc.io", PermissionDefault.OP),
			new Permission("cdpp.motdc", PermissionDefault.OP)
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
	
	public String[] getDirectorys() { return new String[] { "MotDChanger" }; }
	
	@CDPluginCommand(commands = { "motdc cdpp.motdc 0" })
	public void onCommand(CommandEvent e)
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		switch(args.length)
		{
			case 0:
				change(sender);
				return;
			case 1:
				switch(args[0])
				{
					case "save":
						save(sender);
						return;
					case "load":
						load(sender);
						return;
					default:
						change(args[0], sender);
				}
				return;
			case 2:
				switch(args[0])
				{
					case "del":
						del(args[1], sender);
						return;
				}
			default:
				switch(args[0])
				{
					case "add":
						add(args, sender);
						return;
				}
				change(args, sender);
		}
	}
	
	@CDPluginEvent
	public void onServerListPing(ServerListPingEvent e)
	{
		if(!motd.equals(""))
			e.setMotd(motd);
	}

	private void add(String[] args, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.sv.msg.add")) {
			sender.sendMessage(ChatColor.RED + "No Permission to add a MotD");
			return;
		}
		String toAdd = VarTools.arrToString(args, 2);
		if(messages.containsKey(args[1]) && !sender.hasPermission("cdpp.motdc.msg.override")) {
			sender.sendMessage(ChatColor.RED + "No Permission to override a set MotD");
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
		if(!sender.hasPermission("cdpp.motdc.msg.del")) {
			sender.sendMessage(ChatColor.RED + "No Permission to delete a set MotD");
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
	
	private void change(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.motdc.set")) {
			sender.sendMessage(ChatColor.RED + "No Permission to change the MotD");
			return;
		}
		if(!motd.equals(""))
		{
			clog.log("Resetting MotD from " + sender.getName(), this);
			motd = "";
			sender.sendMessage(mbeg + "MotD resetted");
			return;
		}
		clog.log("Changing MotD with default from " + sender.getName(), this);
		if(!messages.containsKey("default")) {
			clog.log("Default message not found", this);
			sender.sendMessage(mbeg + "Default message not found");
			return;
		}
		motd = messages.get("default");
		sender.sendMessage(mbeg + "MotD changed with default message");
	}
	private void change(String key, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.motdc.lock.message.certain")) {
			sender.sendMessage(ChatColor.RED + "No Permission to change the MotD to a certain message");
			return;
		}
		clog.log("Changing MotD with Message-Key " + key, this);
		if(!messages.containsKey(key)) {
			clog.log("MessageKey " + key + " not found", this);
			sender.sendMessage(mbeg + "MessageKey not found");
			return;
		}
		motd = messages.get(key);
		sender.sendMessage(mbeg + "MotD changed to " + key + " message");
	}
	private void change(String[] args, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.motdc.lock.message.uncertain")) {
			sender.sendMessage(ChatColor.RED + "No Permission to lock the Server with an specified message");
			return;
		}
		String s = VarTools.arrToString(args, 0);
		clog.log("Changing MotD with Message \"" + s + "\" from " + sender.getName(), this);
		motd = s;
		sender.sendMessage(mbeg + "MotD changed with your message");
	}
	
	private boolean save(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.motdc.io")) {
			sender.sendMessage("No Permission to use this command");
			return true;
		}
		try {
			save();
			sender.sendMessage(ChatColor.GREEN + "[MotDChanger] Messages saved");
		}
		catch (Exception x)
		{
			sender.sendMessage(ChatColor.RED + "[MotDChanger] Error while saving");
		}
		return true;
	}
	private boolean load(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.motdc.io")) {
			sender.sendMessage("No Permission to use this command");
			return true;
		}
		try
		{
			load();
			sender.sendMessage(ChatColor.GREEN + "[MotDChanger] Messages loaded");
		}
		catch (Exception x)
		{
			sender.sendMessage(ChatColor.RED + "[MotDChanger] Error while loading");
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
		if(!new File(CDPlugin.getDir() + getDirectorys()[0] + "/data.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + getDirectorys()[0] + "/data.dat not found. Returning", this);
			return;
		}
		NBTTagCompound base;
		FileInputStream inputStream = new FileInputStream(CDPlugin.getDir() + getDirectorys()[0] + "/data.dat");
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
