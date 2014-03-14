package net.gmx.teamterrian.CDsPluginPack.handle.events;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ActionEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Map<CDPlugin, Boolean> success = new HashMap<CDPlugin, Boolean>();
	public boolean wasFired = false;
	
	public HandlerList getHandlers() {
        return handlers;
	}

    public static HandlerList getHandlerList() {
        return handlers;
    }
	
	public Set<CDPlugin> getPlugins()
	{
		return success.keySet();
	}
	public void setSuccess(CDPlugin plugin, boolean success)
	{
		this.success.put(plugin, success);
	}
	public boolean getSuccess(CDPlugin plugin)
	{
		return success.get(plugin); 
	}
	public Collection<Boolean> getSuccess()
	{
		return success.values();
	}
}
