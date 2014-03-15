package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

public class GiveTP extends CDPlugin
{
	Log clog;
	Map<String, ItemStack> gti = new CDHashMap<String, ItemStack>();
	
	public GiveTP(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.gtp.default", PermissionDefault.NOT_OP),
			new Permission("cdpp.gtp.ignoreclick", PermissionDefault.OP),
			new Permission("cdpp.gtp", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{	
		try {
			clog.log("Loading Teleporters", this);
			load();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	public String[] getDirectorys() { return new String[] { "GiveTP" }; }
	
	@CDPluginCommand(commands = { "givetp cdpp.gtp 1", "gtp cdpp.gtp 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "give":
				if(give(args)) return;
				else break;
			case "set":
				if(set(args, sender)) return;
				else break;
			case "save":
				save(args, sender);
				return;
			case "load":
				load(args, sender);
				return;
			case "del":
				if(del(args, sender)) return;
				else break;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private boolean give(String[] args)
	{
		if(args.length < 3) return false;
		Player p = Bukkit.getPlayer(args[1]);
		if(p == null) return true;
		give(p, args[2], true);	
		return true;
	}
	private boolean set(String[] args, CommandSender sender)
	{
		if(args.length < 1 || !VarTools.isPlayer(sender)) return false;
		Player p = (Player) sender;
		ItemStack i = p.getItemInHand();
		if(i == null || i.getType() == Material.AIR) {
			p.sendMessage("You have to have an item in your hand");
			return true;
		}
		gti.put(args[1], i);
		try {
			save();
			p.sendMessage(ChatColor.GREEN + "[GTP] Item added");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			p.sendMessage(ChatColor.RED + "[GTP] The Item was added, but there is a problem in saving the data´.");
		}
		return true;
	}
	private boolean del(String[] args, CommandSender sender)
	{
		if(args.length < 2) return false;
		clog.log(sender.getName() + " removes Item with id \"" + args[1] + "\"", this);
		gti.remove(args[1]);
		try {
			save();
			clog.log("Item removed", this);
			sender.sendMessage(ChatColor.GREEN + "[GTP] Item removed");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.RED + "[GTP] The Item was removed, but there is a problem in saving the data´.");
		}
		return true;
	}
	private boolean save(String[] args, CommandSender sender)
	{
		try {
			save();
			sender.sendMessage(ChatColor.GREEN + "[GTP] Data saved");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.DARK_RED + "[GTP] Error while saving data");
		}
		return true;
	}
	private boolean load(String[] args, CommandSender sender)
	{
		try {
			load();
			sender.sendMessage(ChatColor.GREEN + "[GTP] Data loaded");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.DARK_RED + "[GTP] Error while loaded data");
		}
		return true;
	}
	
	private void give(Player p, String item, boolean notify)
	{
		Inventory inv = p.getInventory();
		clog.log("Trying to give Teleporter \"" + item + "\" to " + p.getName(), this);
		if(gti.get(item) == null)
		{
			if(notify) p.sendMessage(ChatColor.GOLD + "The Teleporter \"" + item + "\" was not found");
			clog.log("Teleporter not found", this);
			return;
		}
		boolean has = clearItem(inv, gti.get(item));
		if(has) {
			clog.log("The player already has this teleporter", this);
			if(notify) p.sendMessage(ChatColor.GOLD + "Du hast diesen Teleporter schon");
		}
		else try
		{ 
			if(inv.firstEmpty() == -1) {
				clog.log("The players inventory is full", this);
				if(notify) p.sendMessage(ChatColor.GOLD + "Dein Inventar ist voll");
			}
			else inv.addItem(gti.get(item).clone());
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
		}
	}
	
	private void load() throws IOException
	{
		clog.log("Begin loading items", this);
		NBTTagCompound base = Data.load(CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat", this);
		NBTTagCompound item;
		NBTTagList list = (NBTTagList) base.get("Teleporters");
		for (int i = 0; i < list.size(); i++)
		{
			item = (NBTTagCompound)list.get(i);
			gti.put(item.getString("itemId"), CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack(item)));
		}
		clog.log("Finished loading items", this);
	}
	private void save() throws IOException
	{
		clog.log("Begin saving Items", this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(String s : gti.keySet())
		{
			NBTTagCompound item = new NBTTagCompound();
			item = CraftItemStack.asNMSCopy(gti.get(s)).save(item);
			item.set("itemId", new NBTTagString(s));
			list.add(item);
		}
		base.set("Teleporters", list);
		Data.secureSave(base, CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat", this);
	    clog.log("Finished items saved", this);
	}
	
	private boolean clearItem(Inventory inv, ItemStack i)
	{
		int counter = -1;
		boolean has = false;
		boolean check;
		for(ItemStack item : inv.getContents())
		{
			counter++;
			if(item == null) continue;
			ItemStack ci = item.clone();
			ci.setAmount(1);
			check = true;
			for(ItemStack ii : gti.values())
				if(ii.equals(ci)) check = false;
			if(check) continue;
			if(has) { inv.setItem(counter, null); continue; }
			if(item.getAmount() > 1)
			{
				has = true;
				item.setAmount(1);
				inv.setItem(counter, item);
			}
			else has = true;
		}
		return has;
	}
	
	@CDPluginEvent
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		if(e.getPlayer().hasPermission("cdpp.gtp.default"))
			give(e.getPlayer(), "default", false);
	}
	@CDPluginEvent
	public boolean onInventoryClick(InventoryClickEvent e)
	{
		if(e.getSlot() == -1) return false;
		if(!e.isCancelled())
			if(processClick((Player) e.getWhoClicked(), e.getCurrentItem())) {
				e.setCancelled(true);
				return true;
			}
		return e.isCancelled();
	}
	@SuppressWarnings("unchecked")
	private boolean processClick(Player p, Object i)
	{
		boolean cancelled = false;
		if(p.getOpenInventory().getTopInventory().getType() != InventoryType.CHEST) return cancelled;
		if(p.hasPermission("cdpp.gtp.ignoreclick")) return cancelled;
		if(i == null) return cancelled;
		ItemStack toClear;
		if((i instanceof ItemStack ? ((toClear = checkContains((ItemStack) i)) != null) : ((toClear = checkContains(((Map<Integer, ItemStack>) i).values())) != null)))
		{
			clearItem(p.getInventory(), toClear);
			cancelled = true;
			clog.log("Blocked clicking on a teleporter in a open chest from " + p.getName(), this);
			p.sendMessage(ChatColor.RED + "Du kannst Teleporter nicht weglegen");
		}
		return cancelled;
	}
	
	private ItemStack checkContains(ItemStack i)
	{
		i = i.clone();
		i.setAmount(1);
		for(ItemStack akt : gti.values())
			if(i.equals(akt)) return akt;
		return null;
	}
	private ItemStack checkContains(Collection<ItemStack> itemSet)
	{
		List<ItemStack> items = new ArrayList<ItemStack>(itemSet);
		ItemStack toBack;
		for(ItemStack i : items)
			if((toBack = checkContains(i)) != null) return toBack;
		return null;
	}
}
