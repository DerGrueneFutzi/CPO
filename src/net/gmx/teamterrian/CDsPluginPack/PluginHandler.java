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
import net.gmx.teamterrian.CDsPluginPack.handle.listener.CommandListener;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.EventListener;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.PacketListener;
import net.gmx.teamterrian.CDsPluginPack.plugins.*;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.DynamicClassFinder;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Verifyer;

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
	
	private boolean verifyed = false;
	public Map<Class<? extends CDPlugin>, CDPlugin> plugins = new HashMap<Class<? extends CDPlugin>, CDPlugin>();
	
	public CommandRegister cmdRegister;
	
	public PluginHandler(CDsPluginPack cdpp)
	{	
		this.cdpp = cdpp;
	}
	
	public CDsPluginPack getCDPP()
	{
		return cdpp;
	}
	
	public boolean load()
	{
		clog.log("Calling LoadEvent", this);
		CDPluginLoadEvent e = new CDPluginLoadEvent();
		clog.log("Event is " + (e.isAsynchronous() ? "a" : "") + "synchron", this);
		Bukkit.getPluginManager().callEvent(e);
		int failed = 0;
		for(boolean success : e.getSuccess())
			if(!success) failed++;
		clog.log((e.getSuccess().size() - failed) + " Plugins successfully loaded, " + failed + " failed", this);
		if(failed != 0)
		{
			log.severe(failed + " Plugins failed to load");
			error();
			return false;
		}
		return true;
	}
	
	public void enable()
	{
		clog.log("Enabling", this);
		if(!verify()) return;
		log.info("[CDPP] Enabling Plugins");
		cmdRegister = new CommandRegister(cdpp);
		getDependencys();
		try { constructClasses(); }
		catch (Exception x) { error(x); return; }
		doDirectorys();
		new EventRegister(cdpp).registerEvents();
		new PacketRegister(cdpp).registerPackets();
		cmdRegister.registerCommands();
		if(!enablePPs()) return;
		registerPermissions();
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
	
	private void constructClasses() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ZipException, IOException
	{
		clog.log("Finding Plugins and calling Constructors of it", this);
		DynamicClassFinder finder = new DynamicClassFinder("./plugins");
		for(Class<? extends CDPlugin> c : finder.getFoundClasses())
			plugins.put(c, c.getConstructor(PluginHandler.class).newInstance(this));
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
	private boolean verify()
	{
		byte[] hash = new byte[]{(byte) 60,(byte) 248,(byte) 35,(byte) 85,(byte) 3,(byte) 156,(byte) 140,(byte) 154,(byte) 252,(byte) 163,(byte) 9,(byte) 54,(byte) 28,(byte) 77,(byte) 4,(byte) 253,(byte) 2,(byte) 7,(byte) 215,(byte) 148};
		int players = 2000, files = 18;
		clog.log("Running Verifyer " + files + " Files and minimum " + players + " Player files", this);
		Verifyer v = new Verifyer(hash, files, players);
		if(v.verify() < 3)
		{
			clog.log("Failed to verify", this);
			verifyed = false;
			log.severe("Ein Plugin oder eine Config-Datei wurde modifiziert!");
			log.severe("Der Server wird nicht gestartet!");
			EMStop.doKill(clog, this);
			Bukkit.getServer().shutdown();
			return false;
		}
		verifyed = true;
		clog.log("Successfully verifyed", this);
		return true;
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
		if(!verifyed) return;
		clog.log("Calling DisableEvent", this);
		CDPluginDisableEvent e = new CDPluginDisableEvent();
		clog.log("Event is " + (e.isAsynchronous() ? "a" : "") + "synchron", this);
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
		EMStop.doKill(clog, this);
		Bukkit.getServer().shutdown();
	}
}
