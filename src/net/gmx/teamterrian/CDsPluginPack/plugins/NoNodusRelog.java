package net.gmx.teamterrian.CDsPluginPack.plugins;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class NoNodusRelog extends CDPlugin
{
	Log clog;
	
	public NoNodusRelog(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.everjoin", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		Player p = e.getPlayer();
		((BugTracker) (handler.plugins.get(BugTracker.class))).checkBug(e.getPlayer(), ((Trade) (handler.plugins.get(Trade.class))).prefix);
		String s = p.getName();
		for(Player pl : Bukkit.getServer().getOnlinePlayers())
			if(pl.getName().equals(s))
			{
				pl.kickPlayer("You logged in from another location");
				clog.log("Kicked Player " + s + " because someone with the same name trys to connect", this);
				e.disallow(Result.KICK_OTHER, "You were logged in from another location. Please try again");
				clog.log("Disallowed connecting of " + s + " because he was already logged in", this);
				return;
			}
		if(!p.isBanned() && p.hasPermission("cdpp.everjoin")) {
			clog.log("Allowed joining for " + p.getName() + (Bukkit.getServer().getMaxPlayers() <= Bukkit.getServer().getOnlinePlayers().length ? " althought the Server is full" : ""), this);
			e.allow();
		}
	}
}
