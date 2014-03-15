package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;
import java.util.Map;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class MonsterVanish extends CDPlugin
{
	Log clog;
	static Map<Runnable, Player> players = new CDHashMap<Runnable, Player>();
	
	public MonsterVanish(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.mvanish", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "mvanish cdpp.mvanish 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		process(e.getArgs(), e.getSender());
	}
	
	private void process(String[] args, CommandSender sender) throws CDInvalidArgsException
	{
		if(args.length == 0) throw new CDInvalidArgsException("mvanish");
		Player p = Bukkit.getServer().getPlayerExact(args[0]);
		if(p == null) sender.sendMessage(ChatColor.DARK_RED + "Player not found");
		else if(args.length == 1) removeMonsterPerm(p, true);
		else createTask(p, Long.valueOf(args[1]));
	}
	
	private void createTask(Player p, long time)
	{
		if(!addMonsterPerm(p)) clog.log("MonsterVanish for " + p.getName() + " refreshed", this);
		clearPlayer(p, clog, this);
		if(time == -1) return;
		Runnable r = new Runnable(){
			public void run()
			{
				Player p = players.get(this);
				if(p == null || hasPermission(p)) return;
				removeMonsterPerm(p, true);
			}
		};
		players.put(r, p);
		Bukkit.getServer().getScheduler().runTaskLater(handler.getCDPP(), r, time);
	}
	static void clearPlayer(Player p, Log clog, Object c)
	{
		Runnable t;
		boolean check = false;
		while(players.containsValue(p))
		{
			t = null;
			for(Runnable a : players.keySet())
				if(players.get(a).equals(p)) { t = a; break; }
			if(t != null) { players.remove(t); check = true; }
		}
		if(check) clog.log("Removed all entries from " + p.getName(), c);
	}
	
	@CDPluginEvent
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		removeMonsterPerm(e.getPlayer(), false);
	}
	public void onPlayerRespawn(final String p)
	{
		if(p.startsWith("lmrc "))
			((HealingStones)(handler.plugins.get(HealingStones.class))).checkName(p);
		else Bukkit.getScheduler().runTask(handler.getCDPP(), new Runnable() { public void run() { Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), p); }});
	}
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		removeMonsterPerm(e.getPlayer(), false);
	}
	@CDPluginEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{
		if(!VarTools.isPlayer(e.getDamager())) return;
		Player p = (Player) e.getDamager();
		if (removeMonsterPerm(p, true))
		clog.log("Removed MVanish from " + p.getName() + " because he had hit an Entity", this);
	}
	@CDPluginEvent
	public void onPlayerDeath(PlayerDeathEvent e)
	{	
		Player p = e.getEntity();
		if(p == null) return;
		if (removeMonsterPerm(p, true))
		clog.log("Removed MVanish from " + p.getName() + " because he had died", this);
	}
	
	private boolean hasPermission(Player p)
	{
		if(p == null) return false;
		PermissionUser pu = PermissionsEx.getUser(p);
		return pu.has("modifyworld.mobtarget.monster.*", p.getWorld().getName());
	}
	private boolean removeMonsterPerm(Player p, boolean notify)
	{
		if(p == null) return false;
		clearPlayer(p, clog, this);
		if(hasPermission(p)) return false;
		((HealingStones) handler.plugins.get(HealingStones.class)).remInvulnerable(p);
		if(notify) p.sendMessage(ChatColor.GOLD + "§lDu bist wieder sichtbar/verwundbar");
		clog.log("Trying to remove the negated Monster-Permission from " + p.getName(), this);
		PermissionUser pexUser = PermissionsEx.getUser(p);
		clog.log("Add \"modifyworld.mobtarget.monster.*\"", this);
		pexUser.addPermission("modifyworld.mobtarget.monster.*");
		clog.log("Remove \"-modifyworld.mobtarget.monster.*\"", this);
		pexUser.removePermission("-modifyworld.mobtarget.monster.*");
		return true;
	}
	private boolean addMonsterPerm(Player p)
	{
		List<Entity> e = p.getNearbyEntities(50, 50, 50);
		Creature cr;
		for(Entity akt : e)
		{
			if(!(akt instanceof Creature)) continue;
			cr = (Creature) akt;
			if(cr.getTarget() == p)
				cr.setTarget(null);
		}
		((HealingStones) handler.plugins.get("hs")).makeInvulnerable(p);
		p.sendMessage(ChatColor.GOLD + "§lDu bist nun unsichtbar und/oder unantastbar für Monster");
		if(p == null || !p.hasPermission("modifyworld.mobtarget.monster.zombie")) return false;
		clog.log("Trying to add the negated Monster-Permission to " + p.getName(), this);
		PermissionUser pexUser = PermissionsEx.getUser(p);
		clog.log("Add \"-modifyworld.mobtarget.monster.*\"", this);
		pexUser.addPermission("-modifyworld.mobtarget.monster.*");
		clog.log("Remove \"modifyworld.mobtarget.monster.*\"", this);
		pexUser.removePermission("modifyworld.mobtarget.monster.*");
		return true;
	}
}
