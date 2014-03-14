package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

public class AddLiveAndHunger extends CDPlugin
{
	Log clog;
	
	public AddLiveAndHunger(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.addf", PermissionDefault.OP),
			new Permission("cdpp.addh", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "addf cdpp.addf 1", "addh cdpp.addh 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		if(e.getCommand().getName().equalsIgnoreCase("addh")) addLive(e.getArgs(), e.getSender());
		else addFood(e.getArgs(), e.getSender());
	}
	
	private void addLive(String[] args, CommandSender sender) throws CDInvalidArgsException
	{
		Player p;
		if(args.length != 2) throw new CDInvalidArgsException("addh");
		p = Player.getPlayer(Bukkit.getPlayer(args[0]));
		if(p == null) return;
		Damageable d = Bukkit.getPlayer(args[0]);
		double h;
		try { h = d.getHealth() + Double.valueOf(args[1]); }
		catch (Exception x) { sender.sendMessage(ChatColor.RED + "Invalid number"); return; }
		if(h < 0) d.setHealth((double) 0);
		else if(h > p.getHealthScale()) p.setHealth(p.getHealthScale());
		else p.setHealth(h);
		clog.log("Adding " + Double.valueOf(args[1]) + " to health from " + p.getName() + ". Now " + d.getHealth(), this);
	}
	private void addFood(String[] args, CommandSender sender) throws CDInvalidArgsException
	{
		Player p;
		if(args.length <= 1) throw new CDInvalidArgsException("addf");
		p = Player.getPlayer(Bukkit.getPlayer(args[0]));
		if(p == null) return;
		int f = p.getFoodLevel();
		try { f += Integer.valueOf(args[1]); }
		catch (Exception x) { sender.sendMessage(ChatColor.RED + "Invalid number"); return; }
		if(f < 0) f = 0;
		p.setFoodLevel(f);
		clog.log("Adding " + Integer.valueOf(args[1]) + " to food from " + p.getName() + ". Now " + p.getFoodLevel(), this);
	}
}
