package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.event.entity.EntityDeathEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;

public class MoreXp extends CDPlugin
{	
	public MoreXp(PluginHandler handler)
	{
		super(handler);
	}
	
	@CDPluginEvent
	public void onEntityDeath(EntityDeathEvent e)
	{
		e.setDroppedExp(e.getDroppedExp() / 2);
	}
}
