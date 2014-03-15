package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class NoEnchCombine extends CDPlugin
{
	Log clog;
	
	public NoEnchCombine(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.anvil", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public boolean onInventoryClick(InventoryClickEvent e)
	{	
		Inventory vi = e.getInventory();
		if(vi == null) return false;
		if(vi.getType() != InventoryType.ANVIL || e.getSlot() != 2) return false;
		Player p = (Player) e.getWhoClicked();
		if(p.hasPermission("cdpp.anvil")) return false;
		ItemStack i1, i2;
		i1 = vi.getItem(0);
		i2 = vi.getItem(1);
		if((hasEnchantment(i1, true) && hasEnchantment(i2, true)) || (hasEnchantment(i1, false) && hasEnchantment(i2, false)))
		{
			e.setCancelled(true);
			clog.log("Forbid " + p.getName() + " to combine two or more enchantments", this);
			p.setExp(p.getExp());
			p.sendMessage(ChatColor.RED + "Es ist nicht möglich, Verzauberungen aus Waffen oder Rüstung zu kombinieren.");
		}
		return false;
	}
	
	private boolean hasEnchantment(ItemStack i, boolean isBook)
	{
		if(i == null) return false;
			for(Enchantment e : Enchantment.values())
				if(i.containsEnchantment(e)) return true;
		return false;
	}
}
