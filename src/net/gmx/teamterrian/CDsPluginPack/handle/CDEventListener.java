package net.gmx.teamterrian.CDsPluginPack.handle;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class CDEventListener extends RegisteredListener
{
	public CDEventListener(Listener listener, EventExecutor executor, Plugin plugin)
	{
		super(listener, executor, EventPriority.NORMAL, plugin, true);
	}
}
