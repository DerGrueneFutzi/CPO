package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;
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
		if(!(VarTools.isPlayer(e.getDamager()) || VarTools.isPlayer(e.getEntity()))) return;
		Player p = (Player) e.getDamager();
		if(!p.hasPermission("cdpp.waterhit") && Bukkit.getServer().getWorld("world").getBlockAt(p.getLocation().add(0, 1.62, 0)).getType() == Material.STATIONARY_WATER)
		{
			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You can´t hit Entitys under water");
		}
	}
}
