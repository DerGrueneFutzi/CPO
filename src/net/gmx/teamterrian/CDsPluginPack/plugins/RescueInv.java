package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.HashMap;
import java.util.Map;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class RescueInv extends CDPlugin
{
	Log clog;
	private Map<Player, ItemStack[][]> invs = new HashMap<Player, ItemStack[][]>();
	public String mbeg = ChatColor.DARK_GRAY + "[RescueInv] " + ChatColor.AQUA;
	
	public RescueInv(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.rinv.rescue", PermissionDefault.OP),
			new Permission("cdpp.rinv.rescue.others", PermissionDefault.OP),
			new Permission("cdpp.rinv.clear.nosave", PermissionDefault.OP),
			new Permission("cdpp.rinv.clear.others", PermissionDefault.OP),
			new Permission("cdpp.rinv.clear", PermissionDefault.OP),
			new Permission("cdpp.rinv.clear.nosave.others", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "clear cdpp.rinv.clear 1", "ci cdpp.rinv.clear 1", "clearinventory cdpp.rinv.clear 1", "rinv cdpp.rinv.rescue 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException, CDNoPermissionException
	{
		process(e.getArgs(), e.getSender(), e.getCommand());
	}
	
	private void process(String[] args, CommandSender sender, Command cmd) throws CDInvalidArgsException, CDNoPermissionException
	{
		Player p;
		if((p = getPlayer(sender, args)) == null) {
			sender.sendMessage(mbeg +  "Player not found");
			return;
		}
		switch(cmd.getName().toLowerCase())
		{
			case "clear":
			case "ci":
				clear(sender, args, p); return;
			case "rinv":
				rinv(sender, args, p); return;
		}
		throw new CDInvalidArgsException(cmd.getName());
	}
	
	private void clear(CommandSender sender, String[] args, Player p) throws CDNoPermissionException
	{
		if(!isAllowed(sender, args, false)) throw new CDNoPermissionException(true);
		clear(p, isNoSave(args));
		sender.sendMessage(mbeg + "Inventory of " + p.getName() + " cleared");
	}
	public void clear(Player p, boolean nosave)
	{
		if(!nosave)
		{
			clog.log("Saving inventory of " + p.getName(), this);
			invs.put(p, new ItemStack[][] { p.getInventory().getContents().clone(), p.getInventory().getArmorContents().clone()});
		}
		else clog.log("Do not saving inventory of " + p.getName(), this);
		clog.log("Clearing inventory of " + p.getName(), this);
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}
	private void rinv(CommandSender sender, String[] args, Player p) throws CDNoPermissionException
	{
		if(!isAllowed(sender, args, true)) throw new CDNoPermissionException(true);
		ItemStack[][] inv;
		if(!invs.containsKey(p))
		{
			clog.log("Inventory of " + p.getName() + " could not be rescued", this);
			sender.sendMessage(mbeg + "No Inventory to Rescue. Maybe it was cleared with NoSave");
			return;
		}
		inv = invs.get(p);
		p.getInventory().setContents(inv[0]);
		p.getInventory().setArmorContents(inv[1]);
		invs.remove(p);
		sender.sendMessage(mbeg + "Inventory of " + sender.getName() + " rescued");
		clog.log("Inventory of " + sender.getName() + " rescued", this);
	}
	
	private Player getPlayer(CommandSender sender, String[] args)
	{
		try
		{
			switch(args.length)
			{
				case 0:
					return Player.getPlayer(sender);
				case 1:
					if(isNoSave(args)) return Player.getPlayer(sender);
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
	private boolean isNoSave(String[] args)
	{
		switch(args.length)
		{
			case 0:
				return false;
			case 1:
				return args[0].equals("nosave");
			case 2:
				return args[1].equals("nosave");
		}
		return false;
	}
	private boolean isAllowed(CommandSender sender, String[] args, boolean rinvCmd)
	{
		if(rinvCmd)
		{
			boolean self = sender.hasPermission("cdpp.rinv.rescue"),
					others = sender.hasPermission("cdpp.rinv.rescue.others");
			return (args.length == 0 ? self : others);
		}
		else
		{
			boolean self = sender.hasPermission("cdpp.rinv.clear.nosave"),
					others = sender.hasPermission("cdpp.rinv.clear.others"),
					nself = sender.hasPermission("cdpp.rinv.clear"),
					nothers = sender.hasPermission("cdpp.rinv.clear.nosave.others");
			switch(args.length)
			{
				case 0:
					return self;
				case 1:
					if(args[0].equals("nosave"))
						return nself;
					return others;
				case 2:
					return nothers;
			}
			return false;
		}
	}
}
