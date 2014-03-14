package net.gmx.teamterrian.CDsPluginPack.plugins;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class ReplaceEnch extends CDPlugin
{
	Log clog;
	
	public ReplaceEnch(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.re.bypass.looting", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public boolean onEnchantItem(EnchantItemEvent e)
	{
		System.out.println("GET");
		if(e.getEnchantsToAdd().containsKey(Enchantment.LOOT_BONUS_MOBS))
		{
			if(e.getEnchanter().hasPermission("cdpp.re.bypass.looting")) return e.isCancelled();
			clog.log("Replaced an Looting-Enchantment with Smite for " + e.getEnchanter().getName(), this);
			e.getEnchantsToAdd().put(Enchantment.DAMAGE_UNDEAD, e.getEnchantsToAdd().get(Enchantment.LOOT_BONUS_MOBS));
			e.getEnchantsToAdd().remove(Enchantment.LOOT_BONUS_MOBS);
		}
		return e.isCancelled();
	}
}
