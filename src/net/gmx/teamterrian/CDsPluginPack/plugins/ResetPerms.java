package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;

public class ResetPerms extends CDPlugin
{
	Map<String, String> groups = new HashMap<String, String>();
	Log clog;
	
	public ResetPerms(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}
	
	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.resetperms", PermissionDefault.TRUE)
		};
	}
	
	public String[] getDirectorys() { return new String[] { "ResetPerms" }; }
	
	@CDPluginCommand(commands = { "resetperms cdpp.resetperms 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		switch(args.length)
		{
			case 0:
				clog.log("Reseting PexGroups for Team-Members", this);
				for(String user : groups.keySet())
					PermissionsEx.getUser(user).setGroups(new String[]{groups.get(user)});
				e.getSender().sendMessage(ChatColor.GREEN + "[ResetPerms] All MiniMod, Mod and Admin group Members reseted");
				clog.log("All PexGroups for Team-Members reseted", this);
				break;
			case 1:
				if(args[0].equals("load")) {
					load(e.getSender());
					break;
				}
			default:
				throw new CDInvalidArgsException(e.getCommand().getName());
		}
	}
	
	private void load(CommandSender sender)
	{
		try
		{
			if(!sender.hasPermission("cdpp.resetperms.load")) throw new CDNoPermissionException(false);
			load();
			sender.sendMessage(ChatColor.GREEN + "[ResetPerms] File reloaded");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.RED + "[ResetPerms] Error while reloading");
		}
	}
	private void load() throws IOException
	{
		clog.log("Start loading Groups", this);
		clog.log("Clearing intern map", this);
		groups.clear();
		if(!new File(CDPlugin.getDir() + getDirectorys()[0] + "/data.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + getDirectorys()[0] + "/data.dat not found. Returning", this);
			return;
		}
		NBTTagCompound base;
		FileInputStream inputStream = new FileInputStream(CDPlugin.getDir() + getDirectorys()[0] + "/data.dat");
		base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		for(Object name : base.c())
			groups.put((String) name, base.getString((String) name));
		clog.log("Groups loaded", this);
	}
}