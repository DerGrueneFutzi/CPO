package net.gmx.teamterrian.CDsPluginPack.handle.listener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.ActionEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

import org.bukkit.event.*;

public class EventListener implements Listener
{
	Log clog;
	public Map<String, CDPlugin> plugins;
	public Map<String, List<Entry<Object, Method>>> events = new HashMap<String, List<Entry<Object, Method>>>();
	
	public EventListener(PluginHandler handler)
	{
		clog = handler.clog;
	}
	
	public void onEvent(Event e)
	{
		if(!checkEvent(e)) return;
		String eventName = e.getEventName();
		if(!events.containsKey(eventName)) return;
		for(Entry<Object, Method> p : events.get(eventName))
			try
			{
				if(e instanceof Cancellable && ((Cancellable) e).isCancelled() && !p.getValue().getAnnotation(CDPluginEvent.class).ignoreCancelled()) continue;
				p.getValue().invoke(p.getKey(), e);
			}
			catch (Exception x) {
				x.printStackTrace(clog.getStream());
				clog.log("Error while handling an Event. An Exception should have been logged", this);
			}
	}
	private boolean checkEvent(Event e)
	{
		if(!(e instanceof ActionEvent)) return true;
		ActionEvent a = (ActionEvent) e;
		if(a.wasFired) return false;
		a.wasFired = true;
		return true;
	}
}
