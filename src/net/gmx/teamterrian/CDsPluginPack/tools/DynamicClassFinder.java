package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;

public class DynamicClassFinder
{
	String pluginDirectory;
	PluginHandler handler;
	
	private List<Class<? extends CDPlugin>> foundClasses;
	
	public DynamicClassFinder(String pluginDirectory, PluginHandler handler)
	{
		this.pluginDirectory = pluginDirectory;
		this.handler = handler;
	}
	
	public List<Class<? extends CDPlugin>> getFoundClasses() throws ZipException, IOException
	{
		if(foundClasses == null) searchClasses();
		return foundClasses;
	}
	public void searchClasses() throws ZipException, IOException
	{
		foundClasses = new ArrayList<Class<? extends CDPlugin>>();
		ZipEntry entry;
		String packageName = "net.gmx.teamterrian.CDsPluginPack.plugins";
		String entryName;
		boolean found = false;
		Class<? extends CDPlugin> c;
		Enumeration<? extends ZipEntry> entries;
		File dir = new File(pluginDirectory);
		for(File f : dir.listFiles(new FilenameFilter() { public boolean accept(File f, String name) { return name.toLowerCase().endsWith(".jar"); } }))
		{
			entries = new ZipFile(f.getAbsoluteFile()).entries();
			while(entries.hasMoreElements())
			{
				if((entry = entries.nextElement()).isDirectory()) continue;
				entryName = entry.getName();
				if(!entryName.startsWith("net/gmx/teamterrian/CDsPluginPack/plugins")) continue;
				try
				{
					entryName = entryName.substring(entryName.lastIndexOf('/') + 1);
					entryName = entryName.substring(0, entryName.lastIndexOf('.'));
					c = Class.forName(packageName + "." + entryName).asSubclass(CDPlugin.class);
				}
				catch (Exception x) { continue; }
				handler.clog.log("Found class " + c.getName(), this);
				foundClasses.add(c);
				found = true;
			}
			if(found) break;
		}
	}
}
