package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import com.sk89q.worldedit.bukkit.selections.Selection;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.gmx.teamterrian.CDsPluginPack.tools.Timestamp;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.Blocks;
import net.minecraft.server.v1_7_R1.IInventory;
import net.minecraft.server.v1_7_R1.InventoryLargeChest;
import net.minecraft.server.v1_7_R1.TileEntityChest;

public class GlobalChests extends CDPlugin
{
	Map<String, GCChestData> data = new CDHashMap<String, GCChestData>();
	String mbeg = ChatColor.DARK_GREEN + "[GlobalChests] " + ChatColor.WHITE;
	Dependencys d;
	
	public GlobalChests(PluginHandler handler)
	{
		super(handler);
		d = handler.dependencys;
	}
		
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "set":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				set((Player) sender, args);
				return;
			case "del":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				del((Player) sender);
				return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private void set(Player p, String args[])
	{
		Selection s = d.we.getSelection(p);
		Location l;
		if(s == null) {	
			p.sendMessage(mbeg + "Please mark a block with WorldEdit");
			return;
		}
		else if(!(l = s.getMaximumPoint()).equals(s.getMinimumPoint())) {
			p.sendMessage(mbeg + "Please mark at least one block");
			return;
		}
		
		Block b = p.getWorld().getBlockAt(l);
		if(!ifChest(b)) {
			p.sendMessage(mbeg + "Please mark a chest");
			return;
		}
		
		GCChestData chestData;
		
		String title = ((Chest) b.getState()).getBlockInventory().getName();
		String key = getKey(title);
		handler.log.info("Checking Key");
		if(key == null || !data.containsKey(key)) {
			chestData = new GCChestData();
			key = getNewKey();
			handler.log.info("Key-Checking false");
		}
		else chestData = data.get(key);
		handler.log.info("Set FillInv");
		
		if(chestData.containsPlayer(p.getName()))
			chestData.setFillInventory(chestData.getInventory(p.getName()));
		else
			chestData.setFillInventory(getChestInventory(b));
		handler.log.info("Putting Data");
		data.put(key, chestData);
		
		setNamedChest(l, encryptKey(key) + title);
		
		p.sendMessage(mbeg + "Chest set");
	}
	private void setNamedChest(Location l, String name)
	{
		IInventory inv = Blocks.CHEST.m(((CraftWorld) l.getWorld()).getHandle(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
		if(inv instanceof TileEntityChest)
			((TileEntityChest) inv).a(name);
		else
		{
			((TileEntityChest) ((InventoryLargeChest) inv).left).a(name);
			((TileEntityChest) ((InventoryLargeChest) inv).right).a(name);
		}
	}
	
	private void del(Player p)
	{
		Selection s = d.we.getSelection(p);
		if(s == null) {	
			p.sendMessage(mbeg + "Please mark a block with WorldEdit");
			return;
		}
		
		World w = p.getWorld();
		Block b;
		int c = 0;
		for(Location l : VarTools.getLocations(s))
		{
			if(!ifChest((b = w.getBlockAt(l)))) continue;
			c++;
			del((Chest) b);
		}
		p.sendMessage(mbeg + c + "GlobalChests deleted");
	}
	private void del(Chest c)
	{
		String key = getKey(c.getBlockInventory().getName());
		if(key == null || !data.containsKey(key)) return;
		c.getBlockInventory().setContents(data.get(key).getFillInventory().getContents());
		data.remove(key);
	}

	private Inventory getChestInventory(Block b)
	{
		handler.log.info("getChestInventory()");
		Block nb;
		
		nb = b.getRelative(BlockFace.EAST);
		if(ifChest(nb)) return getRightChestInv((Chest) b.getState(), (Chest) nb.getState(), BlockFace.EAST);
		nb = b.getRelative(BlockFace.NORTH);
		if(ifChest(nb)) return getRightChestInv((Chest) b.getState(), (Chest) nb.getState(), BlockFace.NORTH);
		nb = b.getRelative(BlockFace.SOUTH);
		if(ifChest(nb)) return getRightChestInv((Chest) b.getState(), (Chest) nb.getState(), BlockFace.SOUTH);
		nb = b.getRelative(BlockFace.WEST);
		if(ifChest(nb)) return getRightChestInv((Chest) b.getState(), (Chest) nb.getState(), BlockFace.WEST);
		return ((Chest) b.getState()).getBlockInventory();
	}
	private Inventory getRightChestInv(Chest c1, Chest c2, BlockFace c1Face)
	{	
		handler.log.info("getRightChestInv()");
		switch(c1Face)
		{
			case EAST:
				if(((org.bukkit.material.Chest) c1).getFacing() == BlockFace.NORTH)
					return combineInvs(c2.getBlockInventory(), c1.getBlockInventory());
				else
					return combineInvs(c1.getBlockInventory(), c2.getBlockInventory());
			case WEST:
				if(((org.bukkit.material.Chest) c1).getFacing() == BlockFace.SOUTH)
					return combineInvs(c2.getBlockInventory(), c1.getBlockInventory());
				else
					return combineInvs(c1.getBlockInventory(), c2.getBlockInventory());
			case NORTH:
				if(((org.bukkit.material.Chest) c1).getFacing() == BlockFace.EAST)
					return combineInvs(c2.getBlockInventory(), c1.getBlockInventory());
				else
					return combineInvs(c1.getBlockInventory(), c2.getBlockInventory());
			case SOUTH:
				if(((org.bukkit.material.Chest) c1).getFacing() == BlockFace.WEST)
					return combineInvs(c2.getBlockInventory(), c1.getBlockInventory());
				else
					return combineInvs(c1.getBlockInventory(), c2.getBlockInventory());
			default: return null;
		}
	}
	private Inventory combineInvs(Inventory i1, Inventory i2)
	{
		handler.log.info("getRightChestInv()");
		Inventory back = Bukkit.createInventory(i1.getHolder(), i1.getSize() + i2.getSize(), i1.getName());
		back.setContents(VarTools.combineArray(i1.getContents(), i2.getContents()));
		return back;
	}
	private boolean ifChest(Block b)
	{
		Material m = b.getType();
		return (m == Material.CHEST || m == Material.TRAPPED_CHEST);
	}
	
	@CDPluginEvent
	public boolean onInventoryOpen(InventoryOpenEvent e)
	{
		String s = e.getInventory().getTitle();
		if(!s.startsWith("§g§c")) return e.isCancelled();
		s = getKey(s);
		if(!VarTools.isPlayer(e.getPlayer())) return e.isCancelled();
		if(s == null || !data.containsKey(s)) return e.isCancelled();
		e.setCancelled(true);
		doOpen((Player) e.getPlayer(), data.get(s));
		return e.isCancelled();
	}
	
	private void doOpen(Player p, GCChestData chestData)
	{
		if(chestData.getGlobal()) doOpenGlobal(p, chestData);
		else doOpenNormal(p, chestData);
	}
	private void doOpenNormal(Player p, GCChestData chestData)
	{
		handler.log.info("Updating Inv");
		updateInv(p.getName(), chestData);
		handler.log.info("Opening Inv");
		open(p, chestData);
	}
	private void doOpenGlobal(Player p, GCChestData chestData)
	{
		updateInv(null, chestData);
		open(p, chestData);
	}
	private void updateInv(String p, GCChestData chestData)
	{
		handler.log.info("updateInv()");
		if(p != null) {
			checkExist(chestData, p);
			if(chestData.getTimestamps().checkCooldown(p))
				fill(chestData, p);
		}
		else
		{
			checkExistGlobal(chestData);
			if(chestData.getTimestamps().checkCooldown(""))
				fill(chestData, null);
		}
		
	}
	private void open(Player p, GCChestData chestData)
	{
		handler.log.info("open()");
		Inventory i;
		if(chestData.getGlobal())
		{
			System.out.println("Getting GlobalInv");
			i = chestData.getGlobalInventory();
		}
		else
		{
			System.out.println("Getting PlayerInv");
			i = chestData.getInventory(p.getName());
		}
		//Inventory i = (chestData.getGlobal() ? chestData.getGlobalInventory() : chestData.getInventory((OfflinePlayer) p));
		if(i == null)
			System.out.println("Inv is null");
		else
		{
			System.out.println(i.getContents());
			System.out.println(i.toString());
			//System.out.println(i.getName());
			i = Bukkit.createInventory(p, 56);
			System.out.println(i.getSize());
			System.out.println(i.getType().name());
			System.out.println(i.getHolder().toString());
			p.openInventory(i);
		}
	}
	
	private void checkExist(GCChestData chestData, String p)
	{
		handler.log.info("checkExist()");
		if(chestData.getGlobal()) checkExistGlobal(chestData);
		else
			if(!chestData.containsPlayer(p))
				fill(chestData, p);
	}
	private void checkExistGlobal(GCChestData chestData)
	{
		handler.log.info("checkExistGlobal()");
		if(chestData.getGlobalInventory() == null)
			fill(chestData, null);
	}
	private void fill(GCChestData chestData, String p)
	{
		handler.log.info("fill()");
		if(p == null)
		{
			chestData.setInventory("", chestData.getFillInventory());
			chestData.getTimestamps().updateGlobalCooldown();
		}
		else
		{
			chestData.setInventory(p, chestData.getFillInventory());
			chestData.getTimestamps().updateCooldown(p);
		}
	}
	
	private String getKey(String s)
	{
		try {
			s = s.substring(4);
			return s.substring(0, s.indexOf("§r")).replace("§", "");
		}
		catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}
	private String getNewKey()
	{
		String key = "0";
		while(data.containsKey(key))
			key = String.valueOf(Integer.valueOf(key) + 1);
		return key;
	}
	private String encryptKey(String key)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append("§" + key.charAt(i));
		return "§g§c" + sb.toString() + "§r";
	}
}

class GCChestData
{
	private Map<String, Inventory> inventorys;
	private GCTimestamp timestamp;
	private boolean globalChest;
	
	public GCChestData()
	{
		inventorys = new CDHashMap<String, Inventory>();
		timestamp = new GCTimestamp();
		globalChest = true;
	}
	public GCChestData(Inventory fillInventory, int cooldown)
	{
		inventorys = new CDHashMap<String, Inventory>();
		inventorys.put("_", fillInventory);
		timestamp = new GCTimestamp(cooldown);
		globalChest = true;
	}
	public GCChestData(Inventory fillInventory, int cooldown, boolean global)
	{
		inventorys = new CDHashMap<String, Inventory>();
		inventorys.put("_", fillInventory);
		timestamp = new GCTimestamp(cooldown);
		globalChest = global;
	}
	
	public Inventory getInventory(String p)
	{
		return inventorys.get(p);
	}
	public Inventory getGlobalInventory()
	{
		Inventory i = inventorys.get("");
		System.out.println("Returning GlobalInv");
		//i.firstEmpty();
		/*for(ItemStack item : i.getContents())
			System.out.println(item.getType().name());
		System.out.println(i.getName());
		System.out.println(i.getSize());
		System.out.println(i.getType().name());
		System.out.println(i.getHolder().toString());*/
		return i;
	}
	public void setInventory(String p, Inventory i)
	{
		inventorys.put(p, i);
	}
	public Inventory getFillInventory()
	{
		return VarTools.cloneInventory(inventorys.get("_"));
	}
	public void setFillInventory(Inventory i)
	{
		inventorys.put("_", i);
	}
	
	public boolean containsPlayer(String p)
	{
		return inventorys.containsKey(p);
	}
	
	public GCTimestamp getTimestamps()
	{
		return timestamp;
	}
	
	public boolean getGlobal()
	{
		return globalChest;
	}
	public void setGlobal(boolean global)
	{
		globalChest = global;
	}
}


class GCTimestamp extends Timestamp
{
	public GCTimestamp()
	{
		super();
		super.setTimestamp("", 0);
	}
	public GCTimestamp(int cooldown)
	{
		super(cooldown, (long) 0);
	}
	
	public boolean checkCooldown(String p)
	{
		if(super.getGlobalCooldown() == -1) return false;
		else return super.checkCooldown(p);
	}
	
	public void updateCooldown(String p)
	{
		long act = Data.getTimestamp();
		super.setTimestamp(p, act - (act % super.getPlayerCooldown()));
	}
	public void updateGlobalCooldown()
	{
		long act = Data.getTimestamp();
		super.setTimestamp("", act - (act % super.getGlobalCooldown()));
	}
}
