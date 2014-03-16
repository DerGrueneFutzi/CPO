package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class NoWaterHit extends CDPlugin
{
	public NoWaterHit(PluginHandler handler)
	{
		super(handler);
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.waterhit", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{
		Player p1 = null, p2 = null;
		if(VarTools.isPlayer(e.getDamager()))
			p1 = (Player) e.getDamager();
		if(VarTools.isPlayer(e.getEntity()))
			p2 = (Player) e.getEntity();
		if(p1 == null && p2 == null) return;
		boolean b1 = true, b2 = true;
		if(p1 != null) b1 = p1.hasPermission("cdpp.waterhit");
		if(p2 != null) b2 = p2.hasPermission("cdpp.waterhit");
		if((!b1 || !b2) && (p1.getWorld().getBlockAt(p1.getLocation().add(0, 1.62, 0)).getType() == Material.STATIONARY_WATER || p2.getWorld().getBlockAt(p1.getLocation().add(0, 1.62, 0)).getType() == Material.STATIONARY_WATER))
		{
			e.setCancelled(true);
			if(p1 != null)
				if(!b1) p1.sendMessage(ChatColor.RED + "You can´t hit Entitys under water");
				else if(!b2) p1.sendMessage(ChatColor.RED + "This Player can´t be hit under water");
		}
	}
}
