package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class ItemInfo extends CDPlugin
{
	Log clog;
	
	public ItemInfo(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.iteminfo", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "iteminfo cdpp.iteminfo 1" })
	public void onCommand(CommandEvent e)
	{
		ItemStack i;
		ItemMeta m;
		Player p = (Player) e.getSender();
		if((i = p.getItemInHand()) == null || (m = i.getItemMeta()) == null) return;
		p.sendMessage("Name -> " + m.getDisplayName().replace('&', '$').replace('§', '$').replace('\r', '%'));
		if(m.getLore() != null)
		for(String s : m.getLore()) p.sendMessage("Lore -> " + s.replace('&', '$').replace('§', '$').replace('\r', '%'));
		for(Enchantment ench : m.getEnchants().keySet()) p.sendMessage("Enchantment -> " + ench.getName());
	}
}
