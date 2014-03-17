package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class BetterJump extends CDPlugin
{
	Log clog;
	String mbeg = ChatColor.LIGHT_PURPLE + "[BetterJump] " + ChatColor.WHITE;
	Map<Runnable, Player> protect = new CDHashMap<Runnable, Player>();
	
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
	public boolean onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		return process(e.getSender(), e.getArgs());
	}
	
	@CDPluginEvent
	public boolean onEntityDamage(EntityDamageEvent e)
	{
		if(e.getCause() != DamageCause.FALL) return e.isCancelled();
		if(!VarTools.isPlayer(e.getEntity())) return e.isCancelled();
		doProtect(e);
		return false;
	}
	
	private boolean process(CommandSender sender, String[] args) throws CDInvalidArgsException
	{
		if(args.length > 4) throw new CDInvalidArgsException("bj");
		Player p = getPlayer(args, sender);
		if(p == null) return true;
		Double[] data = getData(args);
		if(getProtect(args)) putRunnable(p);
		doJump(p, data);
		return true;
	}
	
	private void doProtect(EntityDamageEvent e)
	{
		if(!VarTools.isPlayer(e)) return;
		Player p = (Player) e.getEntity();
		if(protect.containsValue(p))
		{
			clog.log("Cancelling FallDamage for " + p.getName(), this);
			removeRunnable(p);
			e.setCancelled(true);
			clog.log("FallDamage for " + p.getName() + " canceled", this);
		}
	}
	
	protected void putRunnable(Player p)
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
		for(Runnable r : new CDHashMap<Runnable, Player>(protect).keySet())
			if(protect.get(r).equals(p)) {
				clog.log("Removing " + p.getName() + " of the Protect list", this);
				protect.remove(r);
			}
	}
	
	protected Double[] getDefaultData()
	{
		return new Double[] { 4.375, 0.2 };
	}
	
	private Player getPlayer(String[] args, CommandSender sender)
	{
		if(args.length == 0 || ifDouble(args[0]) || args[0].equals("true"))
			return (Player) sender;
		Player p;
		if((p = Bukkit.getServer().getPlayer(args[0])) == null) {
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
	
	protected void doJump(HumanEntity e, Double[] d)
	{
		clog.log("Jump " + e.getName() + " with h = " + d[1] + ", l = " + d[0], this);
		e.setVelocity(e.getLocation().getDirection().setY(d[1]).multiply(d[0]));
	}
	
	private boolean ifDouble(String input) { try { Double.valueOf(input); return true; } catch (Exception x) { return false; } }
	
	@CDPluginEvent
	public void onMove(PlayerMoveEvent e)
	{
		Player p = e.getPlayer();
		if(p.hasPermission("cdpp.bj.doublejump")) {
			if(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
				p.setAllowFlight(true);
		}
		else if(p.getGameMode() != GameMode.CREATIVE)
			p.setAllowFlight(false);
	}	
	@CDPluginEvent
	public void onFlyToggle(PlayerToggleFlightEvent e)
	{
		Player p = e.getPlayer();
		if(!p.hasPermission("cdpp.bj.doublejump") || p.getGameMode() == GameMode.CREATIVE) return;
		e.setCancelled(true);
	    p.setAllowFlight(false);
	    p.setFlying(false);
	    putRunnable(p);
	    doJump(p, getDefaultData());
	}
}
