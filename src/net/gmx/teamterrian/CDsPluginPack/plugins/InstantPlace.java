package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;

public class InstantPlace extends CDPlugin
{
	List<Player> players = new ArrayList<Player>();
	
	public InstantPlace(PluginHandler handler)
	{
		super(handler);
	}
	
	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.ip", PermissionDefault.OP)
		};
	}
	
	@CDPluginCommand(commands = { "instantplace cdpp.ip 1" })
	public void onCommand(CommandEvent e)
	{
		Player p = (Player) e.getSender();
		if(players.contains(p)) players.remove(p);
		else players.add(p);
		p.sendMessage(ChatColor.GOLD + "InstantPlace now changed for you");
	}
	
	
	@SuppressWarnings("deprecation")
	@CDPluginEvent
	public void onBlockBreak(BlockBreakEvent e)
	{		
		if(!players.contains(e.getPlayer())) return;
		e.setCancelled(true);
		Block b = e.getBlock();
		ItemStack i = e.getPlayer().getItemInHand();
		if(i == null || i.getTypeId() >= 256) return;
		b.setTypeIdAndData(i.getTypeId(), i.getData().getData(), false);
	}
}
