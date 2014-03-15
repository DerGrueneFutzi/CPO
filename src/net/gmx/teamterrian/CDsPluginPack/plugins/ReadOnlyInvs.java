package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class ReadOnlyInvs extends CDPlugin
{
	Log clog;
	
	public ReadOnlyInvs(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.roi.bypass", PermissionDefault.OP)
		};
	}
		
	@CDPluginPacket(types = { "cwindow_click" })
	@SuppressWarnings("deprecation")
	public void onPacket(PacketEvent e)
	{
		PacketContainer pc = e.getPacket();
		Player p = e.getPlayer();
		if(p.hasPermission("cdpp.roi.bypass")) return;
		if(pc.getIntegers().read(3) == 3) return;
		Inventory vi = p.getOpenInventory().getTopInventory();
		if(!vi.getTitle().endsWith("§1")) return;
		if(pc.getIntegers().read(1) >= vi.getSize() && pc.getIntegers().read(3) != 1) return;
		e.setCancelled(true);
		p.sendMessage(ChatColor.RED + "This Inventory is ReadOnly");
		p.sendMessage(ChatColor.RED + "You can´t modify it");
		p.updateInventory();
	}
		
	@CDPluginEvent
	public void onBlockBreak(BlockBreakEvent e)
	{
		Block b = e.getBlock();
		Player p = e.getPlayer();
		if(p.hasPermission("cdpp.roi.bypass")) return;
		if(b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) return;
		Chest c = (Chest) b.getState();
		if(!c.getBlockInventory().getTitle().endsWith("§1")) return;
		e.setCancelled(true);
		p.sendMessage(ChatColor.RED + "The Inventory of this Block is ReadOnly");
		p.sendMessage(ChatColor.RED + "Please don´t remove it");
	}
}
