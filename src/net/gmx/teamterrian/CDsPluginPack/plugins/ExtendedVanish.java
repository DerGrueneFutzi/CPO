package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.earth2me.essentials.Essentials;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class ExtendedVanish extends CDPlugin
{
	Log clog;
	Essentials ess;
	
	public ExtendedVanish(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		ess = handler.dependencys.ess;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.ev.silentjoin", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onServerListPing(ServerListPingEvent e)
	{
		if (!(e instanceof Iterable)) return;
		Iterator<?> players;
		try { players = e.iterator(); }
		catch (UnsupportedOperationException x) { return; }
		while (players.hasNext())
			if (ess.getUser((Player) players.next()).isVanished())
				players.remove();
	}
	
	@CDPluginEvent
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		Player p = e.getPlayer();
		if(!p.hasPermission("cdpp.ev.silentjoin")) return;
		try
		{
			ess.getUser(p).setVanished(true);
			p.getActivePotionEffects().clear();
		}
		catch (NullPointerException x) {}
	}
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if(ess.getUser(p).isVanished())
			p.sendMessage(ChatColor.LIGHT_PURPLE + "§l[Extended Vanish] You were automatically vanished. To unvanish type " + ChatColor.ITALIC + "/vanish");
	}
}
