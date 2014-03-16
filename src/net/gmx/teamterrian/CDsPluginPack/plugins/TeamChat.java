﻿package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.Map;
import java.util.logging.Logger;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketEvent;

public class TeamChat extends CDPlugin
{
	Log clog;
	Logger log;
	Map<Player, Boolean> toggle = new CDHashMap<Player, Boolean>();
	
	public TeamChat(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		log = handler.log;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.tc.toggle", PermissionDefault.OP),
			new Permission("cdpp.tc.receive", PermissionDefault.OP),
			new Permission("cdpp.tc.send", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "ac cdpp.tc.send 1", "actoggle cdpp.tc.toggle 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		process(e.getSender(), e.getArgs(), e.getCommand());
	}
	
	@CDPluginPacket(types = { "cchat" })
	public void onPacket(PacketEvent e)
	{
		Player p = e.getPlayer();
		if(!p.hasPermission("cdpp.tc.toggle") || e.getPacket().getStrings().read(0).startsWith("/")) return;
		if(toggle.containsKey(p) && toggle.get(p))
		{
			e.setCancelled(true);
			clog.log("Text from " + p.getName() + " was catched because he had toggled this action on", this);
			handler.clistener.onCommand(new CommandEvent(new CDCommand("ac", handler.getCDPP()), p, VarTools.stringToArr(e.getPacket().getStrings().read(0), 0)));
		}
	}
	
	private void process(CommandSender sender, String[] args, Command cmd) throws CDInvalidArgsException
	{
		if(cmd.getName().equalsIgnoreCase("ac")) ac(sender, args);
		else actoggle(sender, args);
	}
	
	private void ac(CommandSender sender, String[] args) throws CDInvalidArgsException
	{
		if(args.length < 1) throw new CDInvalidArgsException("ac");
		String name;
		if(Bukkit.getPlayerExact(sender.getName()) == null) name = "Server";
		else name = sender.getName();
		StringBuilder sb = new StringBuilder();
		for(String akt : args) sb.append(" " + akt);
		clog.log("[" + name + "] > "+ sb.toString(), this);
		for(Player p : Bukkit.getServer().getOnlinePlayers())
			if(p.hasPermission("cdpp.tc.receive")) p.sendMessage(ChatColor.RED + name + ChatColor.RESET + ChatColor.GRAY + " >" + ChatColor.RESET + ChatColor.WHITE + sb.toString());
		if(name.equals("Server")) log.info("[CDPP][TeamChat][Console]" + sb.toString());
		else log.info("[CDPP][TeamChat][" + name + "]" + sb.toString());
	}
	private void actoggle(CommandSender sender, String[] args) throws CDInvalidArgsException
	{
		if(args.length >= 1) throw new CDInvalidArgsException("actoggle");
		Player p = (Player) sender;
		if(!toggle.containsKey(p)) toggle.put(p, false);
		if(!toggle.get(p))
		{
			toggle.put(p, true);
			clog.log("Toggled action for " + p.getName() + " on", this);
			p.sendMessage(ChatColor.GREEN + "[TeamChat] Du schreibst ab sofort dauerhaft im TeamChat");
		}
		else
		{
			toggle.put(p, false);
			clog.log("Toggled action for " + p.getName() + " off", this);
			p.sendMessage(ChatColor.GREEN + "[TeamChat] Du schreibst ab sofort wieder im normalen Chat");
		}
	}
	
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if(toggle.containsKey(p)
				&& toggle.get(p))
			toggle.put(p, false);
	}
}
