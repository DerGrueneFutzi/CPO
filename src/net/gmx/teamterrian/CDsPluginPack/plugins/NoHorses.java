package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class NoHorses extends CDPlugin
{
	public NoHorses(PluginHandler handler)
	{
		super(handler);
	}
	
	@CDPluginEvent
	public void onEntitySpawn(CreatureSpawnEvent e)
	{
		if(e.getEntityType() != EntityType.HORSE) return;
		handler.clog.log("Cancelling Horse-Spawn on " + VarTools.parse(e.getLocation(), false, false, false), this);
		e.setCancelled(true);
	}
}
