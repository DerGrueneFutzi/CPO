package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;

public class DynamicClassLoader
{
	PluginHandler handler;
	public DynamicClassLoader(PluginHandler handler)
	{
		this.handler = handler;
	}
	
	public List<CDPlugin> loadDir(File dir)
	{
		List<CDPlugin> back = new ArrayList<CDPlugin>();
		if(dir == null || !dir.exists() || !dir.isDirectory()) return back;
		for(String f : dir.list(new FilenameFilter() { public boolean accept(File arg0, String arg1) { return arg1.endsWith(".jar"); }}))
			back.addAll(load(new File(f)));
		return back;
	}
	
	public List<CDPlugin> load(File f)
	{
		List<CDPlugin> back = new ArrayList<CDPlugin>();
	    try
	    {
	    	ZipFile z = new ZipFile(f);
	    	Enumeration<? extends ZipEntry> en = z.entries();
	    	URL url = f.toURI().toURL();
			ClassLoader cloader = new URLClassLoader(new URL[] { url }, getClass().getClassLoader());
	        Class<?> c;
	        Class<? extends CDPlugin> cdc;
	        String name = "";
	        while(en.hasMoreElements())
	        {
	        	try
	        	{
		        	name = en.nextElement().getName();
		        	name = name.substring(0, name.length() - 6).replace('/', '.');
		            c = Class.forName(name, true, cloader);
		            cdc = c.asSubclass(CDPlugin.class);
		            handler.clog.log("Found class " + VarTools.cutEnd(f.getName().substring(f.getName().lastIndexOf('/') + 1), 4), this);
		            back.add(cdc.getConstructor(PluginHandler.class).newInstance(handler));
	        	}
	        	catch (Throwable ix) { continue; }
	        	
	        }
	        z.close();
	    }
	    catch (Exception x) { return back; }
	    return back;
	}
}
