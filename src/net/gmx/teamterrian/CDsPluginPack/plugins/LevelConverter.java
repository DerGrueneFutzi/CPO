package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.ArrayList;
import java.util.List;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class LevelConverter extends CDPlugin
{
	Log clog;
	ItemStack berry;
	
	public LevelConverter(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.clevel", PermissionDefault.OP),
			new Permission("cdpp.clevel.others", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{		
		try
		{
			clog.log("Setting start items", this);
			setStartItems();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginCommand(commands = { "clevel cdpp.clevel 1" })
	public void onCommand(CommandEvent e)
	{
		process(e.getSender(), e.getArgs());
	}
	
	public void setStartItems()
	{
		berry = new ItemStack(Material.NETHER_STALK);
		ItemMeta meta = berry.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_PURPLE + "Waldbeere");
		List<String> lores = new ArrayList<String>();
		lores.add(ChatColor.COLOR_CHAR + "iWährung von Terrian");
		meta.setLore(lores);
		berry.setItemMeta(meta);
	}
	
	private void process(CommandSender sender, String[] args)
	{
		if(!isAllowed(sender, args)) {
			sender.sendMessage(ChatColor.RED + "No Permission to use this command");
			return;
		}
		Player p = getPlayer(sender, args);
		if(p == null) {
			sender.sendMessage(ChatColor.RED + "Player not found!");
			return;
		}
		boolean all = isAll(args);
		int level;
		if(all) level = p.getLevel();
		else if(p.getLevel() >= 1) level = 1;
		else level = 0;
		int l = p.getLevel();
		clog.log("Removing " + (all ? "all" : "a") + " level from " + sender.getName(), this);
		for(int i = level; i > 0; i--)
		{
			l--;
			p.setLevel(l);
			giveItem(p, berry);
		}
	}
	private void giveItem(Player p, ItemStack i)
	{
		Inventory vi = p.getInventory();
		if(vi.firstEmpty() == -1) p.getWorld().dropItemNaturally(p.getLocation(), i);
		else vi.addItem(i);
	}
	
	private Player getPlayer(CommandSender sender, String[] args)
	{
		boolean isPlayer = Player.isPlayer(sender);
		try
		{
			switch(args.length)
			{
				case 0:
					return (isPlayer ? Player.getPlayer(sender) : null);
				case 1:
					if(isAll(args)) return (isPlayer ? Player.getPlayer(sender) : null);
				case 2:
					return Player.getPlayer(Bukkit.getServer().getPlayer(args[0]));
			}
			return null;
		}
		catch (Exception x)
		{
			return null;
		}
	}
	private boolean isAll(String[] args)
	{
		switch(args.length)
		{
			case 0:
				return false;
			case 1:
				return args[0].equals("all");
			case 2:
				return args[1].equals("all");
		}
		return false;
	}
	private boolean isAllowed(CommandSender sender, String[] args)
	{
		boolean self = sender.hasPermission("cdpp.clevel"),
				others = sender.hasPermission("cdpp.clevel.others");
		switch(args.length)
		{
			case 0:
				return self;
			case 1:
				if(args[0].equals("all"))
					return self;
				return others;
			case 2:
				return others;
		}
		return false;
	}
}
