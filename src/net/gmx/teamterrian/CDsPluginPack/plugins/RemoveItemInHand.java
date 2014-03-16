package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDPlayerNotFoundException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

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
	public void onCommand(CommandEvent e) throws CDException
	{
		removeItem(e.getSender(), e.getArgs());
	}
	
	private void removeItem(CommandSender sender, String[] args) throws CDException
	{
		Player p;
		if(args.length == 1)
			if(!sender.hasPermission("cdpp.riih.others")) throw new CDNoPermissionException(true);
			else
				if((p = Bukkit.getPlayer(args[0])) == null) throw new CDPlayerNotFoundException(args[0]);
				else removeItem(p);
		else if(args.length == 0)
			if(!sender.hasPermission("cdpp.riih")) throw new CDNoPermissionException(true);
			else removeItem((Player) sender);
		else throw new CDInvalidArgsException("riih");
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
