package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

public class RemoveItemInHand extends CDPlugin
{
	Log clog;
	
	public RemoveItemInHand(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.riih.others", PermissionDefault.OP),
			new Permission("cdpp.riih", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "riih cdpp.riih 1" })
	public void onCommand(CommandEvent e)
	{
		removeItem(e.getSender(), e.getArgs());
	}
	
	private void removeItem(CommandSender sender, String[] args)
	{
		Player p;
		if(args.length >= 1)
			if(!sender.hasPermission("cdpp.riih.others"))
				sender.sendMessage(ChatColor.RED + "You are not permitted to remove the Item of other player hands");
			else
				if((p = Player.getPlayer(Bukkit.getPlayer(args[0]))) == null)
					sender.sendMessage(ChatColor.RED + "Player not found");
				else removeItem(p);
		else
			if(!sender.hasPermission("cdpp.riih"))
				sender.sendMessage(ChatColor.RED + "You are not permitted to remove the Item in your hand");
			else removeItem(Player.getPlayer(sender));
	}
	private void removeItem(Player p)
	{
		if(!p.hasPermission("cdpp.riih")) return;
		ItemStack i = p.getItemInHand();
		if(i == null || i.getType() == Material.AIR) return;
		clog.log("Removing Item named \"" + i.getItemMeta().getDisplayName() + "\" from the inventory from " + p.getName(), this);
		if(i.getAmount() == 0) p.getInventory().setItemInHand(null);
		else { i.setAmount(i.getAmount() - 1); p.getInventory().setItemInHand(i); }
	}
}
