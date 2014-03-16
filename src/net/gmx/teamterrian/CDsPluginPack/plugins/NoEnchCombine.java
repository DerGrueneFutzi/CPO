package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

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
			new Permission("cdpp.nec", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onInventoryClick(InventoryClickEvent e)
	{	
		Inventory vi = e.getInventory();
		if(vi == null) return;
		if(vi.getType() != InventoryType.ANVIL || e.getSlot() != 2) return;
		Player p = (Player) e.getWhoClicked();
		if(p.hasPermission("cdpp.nec")) return;
		ItemStack i1, i2;
		i1 = vi.getItem(0);
		i2 = vi.getItem(1);
		if((hasEnchantment(i1, true) && hasEnchantment(i2, true)) || (hasEnchantment(i1, false) && hasEnchantment(i2, false)))
		{
			e.setCancelled(true);
			clog.log("Forbid " + p.getName() + " to combine two or more enchantments", this);
			p.setExp(p.getExp());
			p.sendMessage(VarTools.getExclamation(ChatColor.GOLD) + ChatColor.RED + "Es ist nicht möglich, Verzauberungen aus Waffen oder Rüstung zu kombinieren.");
		}
	}
	@CDPluginEvent
	public void onInvClose(InventoryCloseEvent e)
	{
		Player p = (Player) e.getPlayer();
		if(e.getInventory().getType() == InventoryType.ANVIL)
			p.setExp(p.getExp());
	}
	
	private boolean hasEnchantment(ItemStack i, boolean isBook)
	{
		if(i == null) return false;
			for(Enchantment e : Enchantment.values())
				if(i.containsEnchantment(e)) return true;
		return false;
	}
}
