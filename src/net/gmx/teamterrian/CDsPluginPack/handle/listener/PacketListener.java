package net.gmx.teamterrian.CDsPluginPack.handle.listener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

import com.comphenix.protocol.events.PacketEvent;

public class PacketListener
{
	private Log clog;
	Map<String, CDPlugin> plugins;
	
	public Map<String, List<Entry<Object, Method>>> packets = new HashMap<String, List<Entry<Object, Method>>>();
	
	public PacketListener(PluginHandler handler)
	{
		clog = handler.clog;
	}
	
	public void onPacket(PacketEvent e)
	{
		String packetName = (e.isServerPacket() ? "s" : "c") + e.getPacketType().name().toLowerCase();
		for(Entry<Object, Method> p : packets.get(packetName))
			try
			{
				if(e.isCancelled() && !p.getValue().getAnnotation(CDPluginPacket.class).ignoreCancelled()) continue;
				p.getValue().invoke(p.getKey(), e);
			}
			catch (Exception x) {
				x.printStackTrace(clog.getStream());
				clog.log("Error while handling a Packet. An Exception should have been logged", this);
			}
	}
}
