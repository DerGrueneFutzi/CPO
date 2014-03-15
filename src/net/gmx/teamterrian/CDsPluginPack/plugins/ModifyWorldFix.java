package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;

public class ModifyWorldFix extends CDPlugin
{	
	public ModifyWorldFix(PluginHandler handler)
	{
		super(handler);
	}
		
	@CDPluginEvent
	@SuppressWarnings("deprecation")
	public boolean onPlayerInteract(PlayerInteractEvent e)
	{
		Player p = e.getPlayer();
		Material m = p.getItemInHand().getType();
		Block b = e.getClickedBlock();
		if(b != null) {
			if(!(p.hasPermission("modifyworld.items.use." + m.getId()) && p.hasPermission("modifyworld.items.use." + m.getId() + ".on.block." + e.getClickedBlock().getType().getId())))
				e.setCancelled(true);
		}
		else
			if(!(p.hasPermission("modifyworld.items.use." + m.getId())))
				e.setCancelled(true);
		return e.isCancelled();
	}
}
