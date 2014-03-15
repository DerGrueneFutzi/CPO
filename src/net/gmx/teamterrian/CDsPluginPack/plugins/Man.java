package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;

public class Man extends CDPlugin
{
	Log clog;
	public Map<String, List<String>> help = new CDHashMap<String, List<String>>();
	String mbeg = ChatColor.DARK_GRAY + "[MAN] ";
	
	public Man(PluginHandler handler)
	{
		super(handler);
		this.clog = handler.clog;
	}
	
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try
		{
			clog.log("Loading CommandUsages", this);
			load();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while loading", this);
			e.setSuccess(this, false);
		}
	}
	
	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.man", PermissionDefault.OP),
			new Permission("cdpp.man.load", PermissionDefault.OP)
		};
	}
	
	@CDPluginCommand(commands = { "man cdpp.man 0", "manload cdpp.man.load 0" })
	public void onCommand(CommandEvent e)
	{
		if(e.getCommand().getName().equalsIgnoreCase("Man")) printUsage(e.getSender(), e.getCommand());
		else load(e.getSender());
	}
	
	public void printUsage(CommandSender sender, Command c)
	{
		List<String> usages = help.get(c.getName().toLowerCase());
		if(usages == null)
			sender.sendMessage(mbeg + "No Help for this command available");
		else
			for(String line : usages)
				sender.sendMessage(mbeg + line);
	}
	
	public String[] getDirectorys() { return new String[] { "man" }; }
	
	private void load(CommandSender sender)
	{
		try
		{
			load();
			sender.sendMessage(ChatColor.GREEN + "[MAN] Successfully loaded");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.RED + "[MAN] Error while loading");
		}
	}
	private void load() throws IOException
	{
		help.clear();
		NBTTagCompound baseTag = Data.load(getDir() + getDirectorys()[0] + "/data.dat", this);
		if(baseTag == null) return;
		
		NBTTagList listTag = (NBTTagList) baseTag.get("Data");
		NBTTagCompound helpTag;
		
		for(int i = 0; i < listTag.size(); i++)
		{
			helpTag = listTag.get(i);
			help.put(helpTag.getString("cmd").toLowerCase(), Data.convertNBTList((NBTTagList) helpTag.get("usages")));
		}
	}
}
