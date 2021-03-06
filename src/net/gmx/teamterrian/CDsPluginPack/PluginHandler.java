package net.gmx.teamterrian.CDsPluginPack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import net.gmx.teamterrian.CDsPluginPack.handle.CommandRegister;
import net.gmx.teamterrian.CDsPluginPack.handle.EventRegister;
import net.gmx.teamterrian.CDsPluginPack.handle.PacketRegister;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginLoadEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.hardDependencys.IEMStop;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.CommandListener;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.EventListener;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.PacketListener;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.DynamicClassLoader;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class PluginHandler
{
	private CDsPluginPack cdpp;
	public Logger log = Logger.getLogger("Minecraft");
	public Log clog = new Log("./plugins/CDsPluginPack/logs", this);
	public EventListener elistener = new EventListener(this);
	public PacketListener plistener = new PacketListener(this);
	public CommandListener clistener = new CommandListener(this);
	public Dependencys dependencys = new Dependencys(this);
	public boolean isEnabled = false;
	
	public Map<Class<? extends CDPlugin>, CDPlugin> plugins = new HashMap<Class<? extends CDPlugin>, CDPlugin>();
	
	public CommandRegister cRegister;
	public EventRegister eRegister;
	public PacketRegister pRegister;
	
	public PluginHandler(CDsPluginPack cdpp)
	{	
		this.cdpp = cdpp;
	}
	
	public CDsPluginPack getCDPP()
	{
		return cdpp;
	}
	
	public void load()
	{
		cRegister = new CommandRegister(cdpp);
		getDependencys();
		try { constructClasses(); }
		catch (Exception x) { error(x); return; }
		doDirectorys();
		(eRegister = new EventRegister(cdpp)).registerEvents();
		(pRegister = new PacketRegister(cdpp)).registerPackets();
		cRegister.searchCommands(false);
		registerPermissions();
		clog.log("Calling LoadEvent", this);
		CDPluginLoadEvent e = new CDPluginLoadEvent();
		elistener.onEvent(e);
		int failed = 0;
		for(boolean success : e.getSuccess())
			if(!success) failed++;
		clog.log((e.getSuccess().size() - failed) + " intern Plugins catched and processed the LoadEvent successfully, " + failed + " failed", this);
		if(failed != 0)
		{
			log.severe(failed + " Plugins failed to load");
			error();
		}
	}
	
	public void enable()
	{
		clog.log("Enabling", this);
		log.info("[CDPP] Enabling Plugins");
		if(!enablePPs()) return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(cdpp, new Runnable()
		{
			public void run()
			{
				cRegister.registerBukkitCommands();
			}
		}, 0);
		clog.log("All plugins enabled", this);
		log.info("[CDPP] All Plugins enabled");
	}
	private void getDependencys()
	{
		clog.log("Getting Dependencys", this);
		if(dependencys.getDependencys()) clog.log("All Dependencys get", this);
		else clog.log("Failed to get all Dependencys", this);
	}
	
	private boolean enablePPs()
	{
		clog.log("Calling EnableEvent", this);
		CDPluginEnableEvent e = new CDPluginEnableEvent();
		Bukkit.getPluginManager().callEvent(e);
		int failed = 0;
		for(boolean success : e.getSuccess())
			if(!success) failed++;
		clog.log((e.getSuccess().size() - failed) + " Plugins catched and processed the EnableEvent successfully, " + failed + " failed", this);
		if(failed != 0)
		{
			log.severe(failed + " Plugins failed to enable");
			error();
			return false;
		}
		return true;
	}
	
	public CDPlugin getPlugin(Class<? extends CDPlugin> clazz)
	{
		return plugins.get(clazz);
	}
	public CDPlugin getPlugin(String name)
	{
		for(Class<? extends CDPlugin> c : plugins.keySet())
			if(c.getSimpleName().equals(name))
				return plugins.get(c);
		return null;
	}
	
	private void constructClasses() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ZipException, IOException
	{
		clog.log("Clearing Plugins", this);
		plugins.clear();
		clog.log("Finding Plugins and calling Constructors of it", this);
		for(CDPlugin cdp : new DynamicClassLoader(this).loadDir(new File("./plugins/CDsPluginPack/")))
			plugins.put(cdp.getClass(), cdp);
		clog.log("Found " + plugins.size() + " CDPlugins", this);
		clog.log("Done", this);
	}
	private void doDirectorys()
	{
		clog.log("Checking and creating Directorys", this);
		checkDir(CDPlugin.getDir());
		String[] paths;
		for(CDPlugin cdp : plugins.values())
			if((paths = cdp.getDirectorys()) != null)
				for(String path : paths)
					checkDir(CDPlugin.getDir() + path);
		clog.log("All Directorys checked", this);
	}
	
	private void registerPermissions()
	{
		clog.log("Register Permissions", this);
		PluginManager manager = Bukkit.getPluginManager();
		for(CDPlugin p : plugins.values())
			for(Permission perm : p.getPermissions())
				manager.addPermission(perm);
		clog.log("All Permissions registered", this);
	}

	public void disable()
	{
		clog.log("Calling DisableEvent", this);
		CDPluginDisableEvent e = new CDPluginDisableEvent();
		Bukkit.getPluginManager().callEvent(e);
		int failed = 0;
		for(boolean success : e.getSuccess())
			if(!success) failed++;
		clog.log((e.getSuccess().size() - failed) + " Plugins catched and processed the DisableEvent successfully, " + failed + " failed", this);
		if(failed != 0)
		{
			log.severe(failed + " Plugins failed to disable");
			log.severe("Maybe data isn't save");
		}
		else
		{
			clog.log("All plugins disabled", this);
			log.info("[CDPP] All Plugins disabled");
		}
		clog.close();
		clog = null;
		System.gc();
		
	}
	
	public void checkDir(String path)
	{
		clog.log("Checking Directory " + path, this);
		File f = new File(path);
		if(!f.exists()) { clog.log("Not existing. Creating it", this); f.mkdir(); }
		else clog.log("Existing", this);
	}

	private void error()
	{
		error(null);
	}
	private void error(Exception x)
	{
		if(x != null) x.printStackTrace(clog.getStream());
		log.severe("Error while enabling/loading Plugins");
		log.severe("The Server would not start, and the Plugin would not work");
		clog.log("The Server would be killed now. Or if its not Linux, immediatly shut down on finishing start", this);
		((IEMStop) getPlugin("EMStop")).doKill(clog, this);
		Bukkit.getServer().shutdown();
	}
}
