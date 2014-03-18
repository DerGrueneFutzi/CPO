package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntitySpawnEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;

public class NoHorses extends CDPlugin
{
	public NoHorses(PluginHandler handler)
	{
		super(handler);
	}
	
	@CDPluginEvent
	public void onEntitySpawn(EntitySpawnEvent e)
	{
		if(e.getEntityType() != EntityType.HORSE) return;
		e.setCancelled(true);
	}
}
