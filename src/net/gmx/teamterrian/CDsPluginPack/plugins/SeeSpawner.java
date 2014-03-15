package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.CDArrayList;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class SeeSpawner extends CDPlugin
{
	Log clog;
	List<Player> see = new CDArrayList<Player>();
	
	public SeeSpawner(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.seespawner", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "seespawner cdpp.seespawner 1" })
	public void onCommand(CommandEvent e)
	{
		Player p = (Player) e.getSender();
		if(see.contains(p)) see.remove(p);
		else see.add(p);
		e.getSender().sendMessage(ChatColor.GOLD + "SpawnerVisibility changed");
	}
	
	@CDPluginPacket(types = { "stile_entity_data" })
	public void onPacket(PacketEvent e)
	{
		if(see.contains(e.getPlayer())) return;
		int x, y, z;
		PacketContainer pc = e.getPacket();
		try
		{
			StructureModifier<Integer> sm = pc.getIntegers();
			x = sm.read(0);
			y = sm.read(1);
			z = sm.read(2);
		}
		catch (Exception ex) { return; }
		if(Bukkit.getServer().getWorld("world").getBlockAt(x, y, z).getType() == Material.MOB_SPAWNER)
			e.setCancelled(true);
	}
}
