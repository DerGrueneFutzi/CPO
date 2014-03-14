package net.gmx.teamterrian.CDsPluginPack.handle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.EventListener;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.MyEntry;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredListener;

public class EventRegister
{
	CDsPluginPack cdpp;
	Log clog;
	EventListener elistener;
	
	public EventRegister(CDsPluginPack cdpp)
	{
		this.cdpp = cdpp;
		clog = cdpp.handler.clog;
		elistener = cdpp.handler.elistener;
	}
	
	public void registerEvents()
	{		
		clog.log("Registering Events", this);
		List<Entry<Object, Method>> list;
		String name;
		for(CDPlugin c : cdpp.handler.plugins.values())
			for(Method m : c.getClass().getMethods())
			{
				if(m.getAnnotation(CDPluginEvent.class) == null) continue;
				name = m.getParameterTypes()[0].getSimpleName();
				if((list = elistener.events.get(name)) == null) list = new ArrayList<Entry<Object, Method>>();
				list.add(new MyEntry<Object, Method>(c, m));
				elistener.events.put(name, list);
				clog.log("Registering Event " + name + " to " + c.getClass().getSimpleName(), this);
			}
		sortCalls();
		try { registerBukkitEvents(elistener.events.keySet(), elistener.getClass().getMethod("onEvent", Event.class)); }
		catch (NoSuchMethodException x) { clog.log("Method EventListener.onEvent(Event) not found. Events are not registered", this); return; }
		clog.log("Done", this);
	}
	
	private void sortCalls()
	{
		clog.log("Sorting EventCalls by Priority", this);
		VarTools.sortCalls(elistener.events, CDPluginEvent.class);
		clog.log("All Events with Priority between 0 and 10000 sorted", this);
	}
	
	private void registerBukkitEvents(Set<String> set, Method method)
	{
		for(String event : set)
			registerBukkitEvent(event, method);
	}
	private void registerBukkitEvent(String event, Method method)
	{
		Entry<Class<? extends Event>, RegisteredListener> entry = getRegisteredListener(method, event);
		if(entry == null) { clog.log("Could not register " + event, this); return; }
		getEventListeners(getRegistrationClass(entry.getKey())).register(entry.getValue());
		clog.log("Registered " + event + " to Bukkit", this);
	}

	private HandlerList getEventListeners(Class<? extends Event> type)
	{
		try
		{
			Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
			method.setAccessible(true);
			return (HandlerList) method.invoke(null);
		}
		catch (Exception e)
		{
			throw new IllegalPluginAccessException(e.toString());
		}
	}
 
	private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz)
	{
		try
		{
			clazz.getDeclaredMethod("getHandlerList");
			return clazz;
		}
		catch (NoSuchMethodException e)
		{
			if (clazz.getSuperclass() != null
					&& !clazz.getSuperclass().equals(Event.class)
					&& Event.class.isAssignableFrom(clazz.getSuperclass()))
			{
				return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
			}
			else
			{
				throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
			}
		}
	}

	private Entry<Class<? extends Event>, RegisteredListener> getRegisteredListener(final Method method, String eventName)
	{
		final Class<? extends Event> eventClass = getEventClass(eventName);
		if(eventClass == null) return null;
		if (method.getParameterTypes().length != 1) return null;
		method.setAccessible(true);
		EventExecutor executor = new EventExecutor()
		{
			public void execute(Listener listener, Event event) throws EventException
			{
				try { method.invoke(listener, event); }
				catch (InvocationTargetException ex) { throw new EventException(ex.getCause()); }
				catch (Throwable t) { throw new EventException(t); }
			}
		};
		return new MyEntry<Class<? extends Event>, RegisteredListener>(eventClass, new CDEventListener(cdpp.handler.elistener, executor, CDsPluginPack.getInstance()));
	}
	
	private Class<? extends Event> getEventClass(String name)
	{
		Class<?> c;
		if((c = getBukkitEventClass(name)) == null)
			try { c = Class.forName("net.gmx.teamterrian.CDsPluginPack.handle.events." + name); }
			catch (ClassNotFoundException ex) { return null; }
		return (Event.class.isAssignableFrom(c) ? c.asSubclass(Event.class) : null); 
	}
	
	private Class<?> getBukkitEventClass(String name)
	{
		Class<?> c;
		for(String p : new String[] {"block", "enchantment", "entity", "hanging", "inventory", "painting", "player", "server", "vehicle", "weather", "world"})
			try { c = Class.forName("org.bukkit.event." + p + "." + name);  return c; }
			catch (ClassNotFoundException x) { continue; }
		return null;
	}
}