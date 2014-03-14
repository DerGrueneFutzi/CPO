package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

public class NoFish extends CDPlugin
{
	Log clog;
	
	public NoFish(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.nf.bypass", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public boolean onPlayerFish(PlayerFishEvent e)
	{
		Player p = Player.getPlayer(e.getPlayer());
		if(p.hasPermission("cdpp.nf.bypass")) return e.isCancelled();
		e.setCancelled(true);
		clog.log("Forbid fishing for " + p.getName(), this);
		p.sendMessage(ChatColor.GOLD + "Fishing is not allowed on this Server");
		return e.isCancelled();
	}
}
