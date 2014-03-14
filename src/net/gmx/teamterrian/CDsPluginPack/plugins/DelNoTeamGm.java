package net.gmx.teamterrian.CDsPluginPack.plugins;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class DelNoTeamGm extends CDPlugin
{
	Log clog;
	
	public DelNoTeamGm(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.isinteam", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = Player.getPlayer(e.getPlayer());
		if(p.hasPermission("cdpp.isinteam") || p.getGameMode() != GameMode.CREATIVE) return;
		clog.log("Deleting Mod-Status of " + p.getName(), this);
		p.setGameMode(GameMode.ADVENTURE);
		((RescueInv) handler.plugins.get(RescueInv.class)).clear(p, false);
		p.setExp(0);
		p.setHealth((double) 0);
	}
}
