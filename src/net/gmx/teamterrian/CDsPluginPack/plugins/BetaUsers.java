package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDArrayList;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.util.org.apache.commons.io.IOUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class BetaUsers extends CDPlugin
{
	Log clog;
	private List<String> users = new CDArrayList<String>();
	private List<ItemStack> items;
	
	public BetaUsers(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.bu", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Loading Betausers", this);
			load();
			clog.log("Success", this);
			clog.log("Loading Items", this);
			loadItems();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginEvent
	public void onDisable(CDPluginDisableEvent e)
	{
		try
		{
			clog.log("Saving File", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	public String[] getDirectorys() { return new String[]{ "BetaUsers" }; }
	
	@CDPluginCommand(commands = { "bu cdpp.bu 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		CommandSender sender = e.getSender();
		String[] args = e.getArgs();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0].toLowerCase())
		{
			case "flush":
			case "save":
				save(sender);
				return;
			case "load":
				if(args.length < 2) throw new CDInvalidArgsException(e.getCommand().getName());
				switch(args[1].toLowerCase())
				{
					case "users":
						load(sender); return;
					case "items":
						loadItems(sender); return;
				}
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private boolean load(CommandSender sender)
	{
		try
		{
			clog.log("Trying to load Betausers", this);
			load();
			clog.log("Betausers loaded", this);
			sender.sendMessage(ChatColor.GREEN + "[BetaUsers] Betausers loaded");
			
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while loading Betausers", this);
			sender.sendMessage(ChatColor.RED + "[BetaUsers] Error while loading Betausers");
		}
		return true;
	}
	private boolean save(CommandSender sender)
	{
		try
		{
			clog.log("Trying to save Betausers", this);
			save();
			clog.log("Betausers saved", this);
			sender.sendMessage(ChatColor.GREEN + "[BetaUsers] Betausers saved");
			
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving Betausers", this);
			sender.sendMessage(ChatColor.RED + "[BetaUsers] Error while saving Betausers");
		}
		return true;
	}
	
	private void save() throws IOException
	{
		clog.log("Begin saving Betausers", this);
		File f = new File(getDir() + getDirectorys()[0] + "/data");
		if(!f.exists()) f.createNewFile();
		OutputStream s;
		s = new FileOutputStream(f);
		for(String user : users) s.write((user + "\n").getBytes());
		s.close();
		clog.log("Finished saving Betausers", this);
	}
	private void load() throws IOException
	{
		clog.log("Begin loading Betausers", this);
		File f = new File(getDir() + getDirectorys()[0] + "/data");
		if(!f.exists()) {
			clog.log("File " + getDir() + getDirectorys()[0] + "/data not found. Returning", this);
			return;
		}
		String u;
		InputStream s = new FileInputStream(f);
		u = IOUtils.toString(s, "UTF-8");
		users.clear();
		for(String user : u.split("\n")) users.add(user.toLowerCase());
		clog.log("Finished loading Betausers", this);
	}
	
	private boolean loadItems(CommandSender sender)
	{
		try
		{
			clog.log("Trying to load Items", this);
			loadItems();
			clog.log("Items loaded", this);
			sender.sendMessage(ChatColor.GREEN + "[BetaUsers] Items loaded");
			
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while loading Items", this);
			sender.sendMessage(ChatColor.RED + "[BetaUsers] Error while loading Items");
		}
		return true;
	}
	
	private void loadItems() throws IOException
	{
		clog.log("Begin loading Items", this);
		NBTTagCompound base = Data.load(getDir() + getDirectorys()[0] + "/data.dat", this);
		if(base == null) return;
		List<ItemStack> back = new CDArrayList<ItemStack>();
		NBTTagList inventoryTag = (NBTTagList) base.get("Items");
		for (int i = 0; i < inventoryTag.size(); i++)
			back.add(CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack((NBTTagCompound)inventoryTag.get(i))));
		items = back;
		clog.log("Finished loading Items", this);
	}
	
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if(users.indexOf(p.getName().toLowerCase()) == -1) return;
		for(ItemStack i : items) p.getInventory().addItem(i.clone());
		users.remove(p.getName().toLowerCase());
		try { save(); }
		catch (Exception x) { x.printStackTrace(clog.getStream()); }
	}
}