package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class NoBlockCheating extends CDPlugin
{
	Log clog;
	Map<Player, Location> locs = new HashMap<Player, Location>();
	World w = Bukkit.getServer().getWorld("world");
	
	public NoBlockCheating(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.nbc.bypass", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Scheduling Location saving Task", this);
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(handler.getCDPP(), getTask(), 0, 200);
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if(p.hasPermission("cdpp.nbc.bypass")) return;
		if(isInBlock(getBlockLocation(p)))
			setBack(p);
		setToMainBlock(p);
	}
	
	private void setBack(Player p)
	{
		Location l = locs.get(p);
		if(l == null || isInBlock(l = l.add(0.5, 0, 0.5))) {
			p.setHealth((double) 0);
			return;
		}
		p.teleport(l.add(0.5, 0, 0.5), TeleportCause.PLUGIN);
	}
	private void setToMainBlock(Player p)
	{
		p.teleport(getBlockLocation(p).add(0.5, 0, 0.5), TeleportCause.PLUGIN);
	}
	
	private Runnable getTask()
	{
		return new Runnable()
		{
			public void run()
			{
				Location l;
				for(Player p : Bukkit.getServer().getOnlinePlayers())
					if((l = getLocation(p)) != null)
						locs.put(p, l);
			}
		};
	}
	
	private Location getLocation(Player p)
	{
		Location l = getBlockLocation(p.getLocation());
		return isInBlock(l) ? null : l;
	}
	private Location getBlockLocation(Location l)
	{
		return new Location(w, l.getBlockX(), l.getBlockY(), l.getBlockZ());
	}
	private Location getBlockLocation(Player p)
	{
		return getBlockLocation(p.getLocation());
	}
	private boolean isInBlock(Location l)
	{
		boolean back = w.getBlockAt(l).getType().isSolid();
		if (back && l.getBlockY() !=  256)
			back &= w.getBlockAt(l.add(0, 1, 0)).getType().isSolid();
		return back;
	}
}
