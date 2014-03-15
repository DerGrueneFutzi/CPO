package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;

public class AdventureEngine extends CDPlugin
{
	Log clog;
	private Map<String, ItemStack> items = new HashMap<String, ItemStack>();
	private Map<String, ItemStack> entitys = new HashMap<String, ItemStack>();
	Dependencys d;
	String mbeg = ChatColor.GOLD + "[AdventureEngine] ";
	
	public AdventureEngine(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		d = handler.dependencys;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.ae", PermissionDefault.OP)
		};
	}
		
	enum ShowOption
	{
		SHOW_ALL,
		SHOW_SENTENCE,
		DONT_SHOW
	}
	
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Loading AdventureData", this);
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
			clog.log("Saving AdventureData", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	public String[] getDirectorys() { return new String[] { "AdventureEngine" }; }
	
	@CDPluginCommand(commands = { "ae cdpp.ae 0", "qe cdpp.ae 0", "qi cdpp.ae 0", "aec cdpp.ae 0", "qec cdpp.ae 0", "qic cdpp.ae 0" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		String com = e.getCommand().getName().toLowerCase();
		if(com.equals("qic") || com.equals("qec") || com.equals("aec"))
		{
			if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
			switch(args[0].toLowerCase())
			{
				case "load":
					load(sender);
				case "save":
					save(sender);
				default:
					throw new CDInvalidArgsException(e.getCommand().getName());
			}
		}
		else if(!process(sender, args)) throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private boolean process(CommandSender sender, String[] args) throws CDInvalidArgsException
	{
		if(args.length < 2) return false;
		List<String[]> cmds = new ArrayList<String[]>();
		List<String> connections = new ArrayList<String>();
		List<String> arguments = toList(args);
		List<String> temp = new ArrayList<String>();
		while(arguments.size() != 0)
		{
			try
			{
				String c;
				temp.clear();
				while(arguments.size() != 0)
				{
					if(!(c = arguments.get(0)).equals(">") && !c.equals("->") && !c.equals("?>")) temp.add(arguments.get(0));
					else { if(temp.size() != 0) cmds.add(toArray(temp)); temp.clear(); if(connections.size() < cmds.size()) connections.add(c); }
					arguments.remove(0);
				}
				if(temp.size() != 0) cmds.add(toArray(temp));
				if(connections.size() > cmds.size()) connections.remove(connections.size() - 1);
			}
			catch(IndexOutOfBoundsException x) { arguments.clear(); }
		}
		List<String> messages;
		Player p;
		if(needPlayer(cmds)) p = getPlayer(cmds, sender);
		else p = null;
		ShowOption so = getShowOption(cmds);
		Inventory vi = p != null ? (((HumanEntity) p).getInventory()) : null;
		CommandSender s = (p == null ? sender : p);
		if((messages = make(cmds, connections, s, vi, true)) != null && messages.size() > 0) showMessages(messages, s, so);
		else make(cmds, connections, s, vi, false);
		return true;
	}
	private List<String> make(List<String[]> ccmds, List<String> cconnections, CommandSender p, Inventory cinv, boolean test) throws CDInvalidArgsException
	{
		boolean fullCheck = true;
		List<String[]> cmds = new ArrayList<String[]>(ccmds);
		List<String> connections = new ArrayList<String>(cconnections);
		Inventory inv = null;
		List<String> messages = new ArrayList<String>();
		String message[] = new String[] { null };
		if(cinv != null)
			if(!test) inv = cinv;
			else {
				inv = Bukkit.createInventory(null, cinv.getContents().length);
				inv.setContents(cinv.getContents().clone());
			}
		boolean check;
		while(cmds.size() != 0)
		{
			check = procCmd(cmds.get(0), p, test, inv, message);
			cmds.remove(0);
			if(connections.size() == 0) return (fullCheck ? null : messages);
			if(connections.size() != 0 && connections.get(0).equals("->") && message[0] != null) messages.add(message[0]);
			if(connections.get(0).equals("?>")){
				if(connections.size() != 0 && connections.get(0).equals("->") && message[0] != null) messages.add(message[0]);
				if(check) procCmd(cmds.get(0), p, test, inv, message);
				cmds.remove(0);
				connections.remove(0);
			}
			if(connections.size() == 0) return (fullCheck ? null : messages);
			if(!check && connections.get(0).equals("->"))
				{ fullCheck = false; if(!messages.contains(null)) messages.add(null); }
			connections.remove(0);
		}
		return (fullCheck ? null : messages);
	}
	private boolean procCmd(String[] cmd, CommandSender p, boolean test, Inventory inv, String[] message) throws CDInvalidArgsException
	{
		if(cmd.length < 2 && !cmd[0].equals("!")) return false;
		message[0] = null;
		boolean negate = true,
				take = true,
				simu = false;
		Boolean back = null;
		if(cmd[0].charAt(0) == '!' && cmd[0].length() > 1) {
			negate = false;
			cmd[0] = cmd[0].substring(1);
		}
		cmd[0] = cmd[0].toLowerCase();
		switch(cmd[0])
		{
			case "!":
				if(!test) runCmd(p, cmd);
				back = true; break;
			case "has":
				message[0] = " - " + (negate ? "" : "NOT ") + "The permission " + cmd[1];
				back = p.hasPermission(cmd[1]); break;
			case "set":
				try { back = setItem((Player) p, cmd, test); }
				catch (IOException x) { p.sendMessage(mbeg + ChatColor.RED + "The Item was added, but there was a problem by saving the Data."); }
				break;
			case "rem":
				try { back = remItem((Player) p, cmd, test); }
				catch (IOException x) { p.sendMessage(mbeg + ChatColor.RED + "The Item was added, but there was a problem by saving the Data."); }
				break;
			case "setbook":
				try { back = setBook((Player) p, cmd, test); }
				catch (IOException x) { p.sendMessage(mbeg + ChatColor.RED + "The Item was added, but there was a problem by saving the Data."); }
				break;
			case "rembook":
				try { back = remBook((Player) p, cmd, test); }
				catch (IOException x) { p.sendMessage(mbeg + ChatColor.RED + "The Item was added, but there was a problem by saving the Data."); }
				break;
			case "give":
				back = giveItem((Player) p, cmd, inv, test, false); break;
			case "givebook":
				back = giveItem((Player) p, cmd, inv, test, true); break;
			case "tave":
			case "simu":
				simu = true;
			case "have":
				take = false;
			case "take":
				back = take((Player) p, cmd, message, inv, test, take, negate, simu); break;
			case "exist":
				back = exist(p, cmd, test); break;
			case "spawn":
				back = spawn(p, cmd, test); break;
			case "loaded":
				back = loaded(p, cmd); break;
		}
		return back == null ? false : negate(back, negate);
	}
	private boolean negate(boolean b, boolean negate)
	{
		return !(negate ^ b);
	}
	
	private void runCmd(CommandSender cs, String[] cmd)
	{
		if(cmd.length < 3) return;
		boolean op = false;
		switch(cmd[1].toLowerCase())
		{
			case "@o":
				if(!cs.isOp()) {
					clog.log("Making " + cs.getName() + " to an operator", this);
					cs.setOp(true);
					op = true;
				}
			case "@p":
				clog.log("Running \"" + VarTools.SB(cmd, 2) + "\" as " + cs.getName(), this);
				Bukkit.dispatchCommand(cs, VarTools.SB(cmd, 2));
				if(op) {
					clog.log("Deleting the operator status from " + cs.getName(), this);
					cs.setOp(false);
				}
				return;
			default:
				String strCmd;
				if(cmd[1].equals("@c")) strCmd = VarTools.SB(cmd, 2);
				else strCmd = VarTools.SB(cmd, 1);
				clog.log("Running \"" + strCmd + "\" as Console", this);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), strCmd);
				return;
		}
	}
	private boolean setItem(Player p, String[] cmd, boolean test) throws IOException
	{
		if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) { if(!test) p.sendMessage(ChatColor.RED + "[AdventureEngine] You must holding an Item in your hand!"); return false; }
		if(!test) {
			setItem(p.getItemInHand(), cmd[1]);
			p.sendMessage(ChatColor.GREEN + "[AdventureEngine] Item saved");
		}
		return true;
	}
	private void setItem(ItemStack i, String id) throws IOException
	{
		clog.log("Putting an Item to description \"" + id + "\"", this);
		items.put(id, i);
		save();
	}
	private boolean setBook(Player p, String[] cmd, boolean test) throws IOException
	{
		if(!d.doDepend(Dependency.NBTEDITOR, p)) return false;
		if(p.getItemInHand() == null || !BookOfSouls.isValidBook(p.getItemInHand())) { if(!test) p.sendMessage(ChatColor.RED + "[AdventureEngine] You must holding a BoS your hand!"); return false; }
		if(!test) {
			clog.log("Putting a BoS to description \"" + cmd[1] + "\"", this);
			entitys.put(cmd[1], p.getItemInHand());
			setItem(p.getItemInHand(), "_BOOK_" + cmd[1]);
			save();
			p.sendMessage(ChatColor.GREEN + "[AdventureEngine] BoS saved");
		}
		return true;
	}
	private boolean remItem(Player p, String[] cmd, boolean test) throws IOException
	{
		if(!items.containsKey(cmd[1])) { if(!test) p.sendMessage(ChatColor.RED + "[AdventureEngine] Item not found"); return false; }
		if(!test) {
			clog.log("Removing an Item with description \"" + cmd[1] + "\"", this);
			items.remove(cmd[1]);
			save();
			p.sendMessage(ChatColor.GREEN + "[AdventureEngine] Item deleted");
		}
		return true;
	}
	private boolean remBook(Player p, String[] cmd, boolean test) throws IOException
	{
		if(!entitys.containsKey(cmd[1])) { if(!test) p.sendMessage(ChatColor.RED + "[AdventureEngine] BoS not found"); return false; }
		if(!test) {
			clog.log("Removing a BoS with description \"" + cmd[1] + "\"", this);
			entitys.remove(cmd[1]);
			save();
			p.sendMessage(ChatColor.GREEN + "[AdventureEngine] BoS deleted");
		}
		return true;
	}
	private boolean giveItem(Player p, String id, Inventory inv, boolean test, boolean book)
	{
		Map<String, ItemStack> data = (book ? entitys : items);
		if(!data.containsKey(id)) { if(!test) p.sendMessage(ChatColor.RED + "[AdventureEngine] Item not found"); return false; }
		if(!test) clog.log("Giving " + p.getName() + " Item with description \"" + id + "\"", this);
		if(inv.firstEmpty() == -1) {
			if(!test) {
				clog.log("Inventory from " + p.getName() + " is full. Item with description \"" + id + "\" is dropped", this);
				p.sendMessage(ChatColor.GOLD + "[AdventureEngine] Your Inventory is full. The Item was dropped");
				p.getWorld().dropItemNaturally(p.getLocation(), data.get(id).clone());
			}
			return false;
		}
		else if(!test) inv.addItem(data.get(id).clone());
		return true;
	}
	private boolean giveItem(Player p, String[] cmd, Inventory inv, boolean test, boolean book)
	{
		return giveItem(p, cmd[1], inv, test, book);
	}
	private boolean take(Player p, String[] cmd, String[] message, Inventory inv, boolean test, boolean take, boolean negate, boolean simu)
	{
		if(!test) clog.log("Trying to " + (take ? "take" : "found") + " Item with description \"" + cmd[1] + "\"" +  (take ? "from" : "in") + " inventory of " + p.getName(), this);
		ItemStack i = items.get(cmd[1]);
		message[0] = " - " + (negate ? "" : "NOT ") + "An unknown item";
		if(i == null) return false;
		String name = i.getItemMeta().getDisplayName();
		message[0] = " - " + (negate ? "" : "NOT ") + i.getAmount() + " " + i.getType().name() + (name == null ? "" : " with the name \"" + name + ChatColor.RED + "\"" + (take ? "" : ChatColor.BOLD + " (only have)"));
		i = i.clone();
		int counter = -1;
		ItemStack[] tinv = inv.getContents();
		ItemStack isc;
		for(ItemStack is : tinv)
		{
			counter++;
			if(is == null) continue;
			if(is.getType() == i.getType() && is.getAmount() >= i.getAmount()) {
				if(take || (test && simu))
				{
					isc = is.clone();
					isc.setAmount(i.getAmount());
					if(!i.equals(isc)) continue;
					if(is.getAmount() == i.getAmount()) {
						is = null; }
					else
						is.setAmount(is.getAmount() - i.getAmount());
					tinv[counter] = is;
					inv.setContents(tinv);
				}
				if(!test)
					if(simu)
						clog.log("Found and simulated remove Item with description \"" + cmd[1] + "\" " +  (take ? "from" : "in") + " inventory of " + p.getName(), this);
					else if(take)
						clog.log("Found and removed Item with description \"" + cmd[1] + "\" " +  (take ? "from" : "in") + " inventory of " + p.getName(), this);
					else
						clog.log("Found Item with description \"" + cmd[1] + "\" " +  (take ? "from" : "in") + " inventory of " + p.getName(), this);
				return true;
			}
		}
		return false;
	}
	private boolean exist(CommandSender p, String[] cmd, boolean test) throws CDInvalidArgsException
	{
		if(!d.doDepend(Dependency.NBTEDITOR, p)) return false;
		if(cmd.length != 10) throw new CDInvalidArgsException("ae");
		List<Entity> entityList;
		World w = Bukkit.getWorld(cmd[cmd.length - 1]);
		try { entityList = EntityRemove.getEntitys(new Location[] { VarTools.getPoint(cmd, 2, w), VarTools.getPoint(cmd, 5, w) }, EntityRemove.getTypeList("l"), Bukkit.getWorld(cmd[9])); }
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			return false;
		}
		BookOfSouls bos = BookOfSouls.getFromBook(entitys.get(cmd[1]));
		if(bos == null) {
			if(!test)
				p.sendMessage(ChatColor.RED + "[AdventureEngine] BoS not found");
			return false;
		}
		EntityNBT enbt = bos.getEntityNBT();
		LivingEntity living;
		if(enbt == null) return false;
		for(Entity e : entityList)
		{
			living = (LivingEntity) e;
			if(sameName(enbt, living) && sameType(enbt, living))
				return true;
		}
		return false;
	}
	private boolean sameName(EntityNBT enbt, LivingEntity e)
	{
		if(!d.doDepend(Dependency.NBTEDITOR, null)) return false;
		String name1 = enbt.getVariable("Name").getValue(),
				name2 = e.getCustomName();
		if(name1 == null) {
			if(name2 == null || name2.equals(""));
				return true;
		}
		else if(name1.equals(name2))
			return true;
		return false;
	}
	private boolean sameType(EntityNBT enbt, LivingEntity e)
	{
		if(!d.doDepend(Dependency.NBTEDITOR, null)) return false;
		return enbt.getEntityType() == e.getType();
	}
	private boolean spawn(CommandSender p, String[] cmd, boolean test)
	{
		if(!d.doDepend(Dependency.NBTEDITOR, p)) return false;
		if(cmd.length < 6) return false;
		BookOfSouls bos = BookOfSouls.getFromBook(entitys.get(cmd[1]));
		if(bos == null) {
			if(!test)
				p.sendMessage(ChatColor.RED + "[AdventureEngine] Entity not found");
			return false;
		}
		EntityNBT enbt = bos.getEntityNBT();
		Location l;
		try { l = VarTools.getPoint(cmd, 2, Bukkit.getWorld(cmd[cmd.length - 1])); }
		catch (Exception x) { return false; }
		if(!test) {
			clog.log("Spawing BoS with description " + cmd[1] + " on " + l.getX() + ", " + l.getY() + ", " + l.getZ(), this);
			enbt.spawn(l);
		}
		return true;
	}
	private boolean loaded(CommandSender p, String[] args)
	{
		Location l = VarTools.getPoint(args, 1, Bukkit.getWorld(args[args.length - 1]));
		int x = l.getBlockX(), z = l.getBlockZ(), cx, cz;
		for(Chunk c : l.getWorld().getLoadedChunks())
				if  (
						((cx = c.getX()) <= x) &&
						(cx + 15 >= x) &&
						((cz = c.getZ()) <= z) &&
						(cz + 15 >= z)
					)
					return true;
		return false;
	}
	
	private void load(CommandSender sender)
	{
		try {
			clog.log("Begin loading AdventureEngine Data", this);
			load();
			clog.log("Finished loading AdventureEngine Data", this);
			sender.sendMessage(ChatColor.GREEN + "[AdventureEngine] Successfully loaded");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			clog.log("Error while loading AdventureEngine Data", this);
			sender.sendMessage(ChatColor.DARK_RED + "[AdventureEngine] Error while loading");
		}
	}
	private void save(CommandSender sender)
	{
		try {
			clog.log("Begin saving AdventureEngine Data", this);
			save();
			clog.log("Finished saving AdventureEngine Data", this);
			sender.sendMessage(ChatColor.GREEN + "[AdventureEngine] Successfully saved");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.DARK_RED + "[AdventureEngine] Error while saving.");
		}
	}
	
	private void load() throws IOException
	{
		items.clear();
		if(!new File(CDPlugin.getDir() + this.getDirectorys()[0] + "/items.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + this.getDirectorys()[0] + "/items.dat not found. Returning", this);
			return;
		}
		NBTTagCompound base;
		FileInputStream inputStream = new FileInputStream(CDPlugin.getDir() + this.getDirectorys()[0] + "/items.dat");
		base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		NBTTagCompound item;
		NBTTagList list = (NBTTagList) base.get("Names");
		String name;
		for (int i = 0; i < list.size(); i++)
		{
			name = list.f(i);
			item = base.getCompound(name);
			items.put(name, CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack(item)));
		}
		
		entitys.clear();
		if(!new File(CDPlugin.getDir() + this.getDirectorys()[0] + "/books.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + this.getDirectorys()[0] + "/books.dat not found. Returning", this);
			return;
		}
		inputStream = new FileInputStream(CDPlugin.getDir() + this.getDirectorys()[0] + "/books.dat");
		base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		list = (NBTTagList) base.get("Names");
		for (int i = 0; i < list.size(); i++)
		{
			name = list.f(i);
			item = base.getCompound(name);
			entitys.put(name, CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack(item)));
		}
	}
	private void save() throws IOException
	{
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(String name : items.keySet())
		{
			list.add(new NBTTagString(name));
			NBTTagCompound item = new NBTTagCompound();
			item = CraftItemStack.asNMSCopy(items.get(name)).save(item);
			base.set(name, item);
		}
		base.set("Names", list);
	    Data.secureSave(base, CDPlugin.getDir() + "AdventureEngine/items.dat", this);
	    
	    base = new NBTTagCompound();
		list = new NBTTagList();
		for(String name : entitys.keySet())
		{
			list.add(new NBTTagString(name));
			NBTTagCompound item = new NBTTagCompound();
			item = CraftItemStack.asNMSCopy(entitys.get(name)).save(item);
			base.set(name, item);
		}
		base.set("Names", list);
	    Data.secureSave(base, CDPlugin.getDir() + "AdventureEngine/books.dat", this);
	}
	
	private List<String> toList(String[] arr)
	{
		List<String> back = new ArrayList<String>();
		for(String s : arr) back.add(s);
		return back;
	}
	private String[] toArray(List<String> input)
	{
		String[] back = new String[input.size()];
		for(int i = 0; i < back.length; i++) back[i] = input.get(i);
		return back;
	}

	private ShowOption getShowOption(List<String[]> cmd)
	{
		ShowOption so;
		try {
			so = ShowOption.valueOf(cmd.get(0)[0].toUpperCase());
			cmd.set(0, VarTools.subArr(cmd.get(0), 1));
		}
		catch (IllegalArgumentException x) { so = ShowOption.SHOW_ALL;}
		return so;
	}
	private boolean needPlayer(List<String[]> cmd)
	{
		int i;
		for(String[] s : cmd)
		{
			i = 0;
			while(i < s.length && !isCommand(s[i])) i++;
			if(i >= s.length) continue;
			switch(s[i].toLowerCase())
			{
				case "set":
				case "setbook":
				case "take":
				case "give":
				case "givebook":
				case "has":
				case "tave":
				case "simu":
				case "have":
					return true;
			}
		}
		return false;
	}
	private Player getPlayer(List<String[]> args, CommandSender sender)
	{
		if(!isCommand(args.get(0)[0])) {
			Player p = Bukkit.getPlayer(args.get(0)[0]);
			if(p != null) {
				args.set(0, VarTools.subArr(args.get(0), 1));
				return p;
			}
		}
		Player p2;
		try { p2 = (Player) sender; }
		catch (ClassCastException x) { sender.sendMessage(mbeg + "Player " + args.get(0)[0] + " not found"); throw x; }
		return p2;
	}
	private boolean isCommand(String str)
	{
		switch(str.toLowerCase())
		{
			case "set":
			case "setbook":
			case "take":
			case "give":
			case "givebook":
			case "has":
			case "tave":
			case "simu":
			case "have":
			case "rem":
			case "rembook":
			case "spawn":
			case "exist":
			case "loaded":
				return true;
			default:
				return false;
		}
	}
	
	private void showMessages(List<String> messages, CommandSender sender, ShowOption so)
	{
		if(so == ShowOption.DONT_SHOW) return;
		sender.sendMessage(mbeg + "You don´t comply with the given expressions!");
		if(so == ShowOption.SHOW_SENTENCE) return;
		messages.remove(null);
		if(messages.size() == 0) return;
		sender.sendMessage(mbeg + ChatColor.RED + "To use this command, you have to have the following things:");
		for(String msg : messages) sender.sendMessage(mbeg + ChatColor.RED + msg);
	}
}