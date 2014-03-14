﻿package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

public class BetterJump extends CDPlugin
{
	Log clog;
	String mbeg = ChatColor.LIGHT_PURPLE + "[BetterJump] " + ChatColor.WHITE;
	Map<Runnable, Player> protect = new HashMap<Runnable, Player>();
	
	public BetterJump(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.bj", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "bj cdpp.bj 1" })
	public boolean onCommand(CommandEvent e)
	{
		return process(e.getSender(), e.getArgs());
	}
	
	@CDPluginEvent
	public boolean onEntityDamage(EntityDamageEvent e)
	{
		if(e.getCause() != DamageCause.FALL) return e.isCancelled();
		if(!Player.isPlayer(e.getEntity())) return e.isCancelled();
		doProtect(e);
		return false;
	}
	
	private boolean process(CommandSender sender, String[] args)
	{
		if(args.length > 4) {
			sender.sendMessage(mbeg + "To few arguments");
			return true;
		}
		Player p = getPlayer(args, sender);
		if(p == null) return true;
		Double[] data = getData(args);
		if(getProtect(args)) putRunnable(p);
		doJump(p, data);
		return true;
	}
	
	private void doProtect(EntityDamageEvent e)
	{
		if(!Player.isPlayer(e)) return;
		Player p = Player.getPlayer(e.getEntity());
		if(protect.containsValue(p))
		{
			clog.log("Cancelling FallDamage for " + p.getName(), this);
			removeRunnable(p);
			e.setCancelled(true);
			clog.log("FallDamage for " + p.getName() + " canceled", this);
		}
	}
	
	private void putRunnable(Player p)
	{
		clog.log("Putting Runnable for " + p.getName(), this);
		Runnable r = getRunnable(p);
		protect.put(r, p);
		clog.log("Starting Runnable for " + p.getName(), this);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(handler.getCDPP(), r, 200L);
		clog.log("Runnable for " + p.getName() + " started", this);
	}
	private Runnable getRunnable(Player p)
	{
		removeRunnable(p);
		return getRunnable();
	}
	private Runnable getRunnable()
	{
		return new Runnable()
		{
			public void run()
			{
				if(!protect.containsKey(this)) return;
				clog.log("Fallprotection for " + protect.get(this).getName() + " timed out", this);
				protect.remove(this);
			}
		};
	}
	private void removeRunnable(Player p)
	{
		for(Runnable r : protect.keySet())
			if(protect.get(r).equals(p)) {
				clog.log("Removing " + p.getName() + " of the Protect list", this);
				protect.remove(r);
			}
	}
	
	private Double[] getDefaultData()
	{
		return new Double[] { 4.375, 0.2 };
	}
	
	private Player getPlayer(String[] args, CommandSender sender)
	{
		if(args.length == 0 || ifDouble(args[0]) || args[0].equals("true"))
			return Player.getPlayer(sender);
		Player p;
		if((p = Player.getPlayer(Bukkit.getServer().getPlayer(args[0]))) == null) {
			sender.sendMessage(mbeg + "Player not found");
			return null;
		}
		return p;
	}
	private boolean getProtect(String[] args)
	{
		String s = "";
		for(int i = 0; i < args.length; i++)
			if(!ifDouble(args[i]))
				if(s.equals(""))
					s = args[i];
				else return args[i].equals("true");
		return s.equals("true");
	}
	private Double[] getData(String[] args)
	{
		for(int i = 0; i < args.length - 1; i++)
			if(ifDouble(args[i]) && ifDouble(args[i + 1]))
				return getData(args, i);
		return getDefaultData();
	}
	private Double[] getData(String[] args, int start)
	{
		Double[] d;
		if((d = getDoubles(args, start)) == null)
			return getDefaultData();
		else return d;
	}
	private Double[] getDoubles(String[] args, int start)
	{
		if(!ifDouble(args[start]) || !ifDouble(args[start + 1])) return null;
		return new Double[] { Double.valueOf(args[start]), Double.valueOf(args[start + 1]) };
	}
	
	private void doJump(HumanEntity e, Double[] d)
	{
		clog.log("Jump " + e.getName() + " with h = " + d[1] + ", l = " + d[0], this);
		e.setVelocity(e.getLocation().getDirection().setY(d[1]).multiply(d[0]));
	}
	
	private boolean ifDouble(String input) { try { Double.valueOf(input); return true; } catch (Exception x) { return false; } }
}