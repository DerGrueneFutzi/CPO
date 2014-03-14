package net.gmx.teamterrian.CDsPluginPack.handle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.handle.listener.PacketListener;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.MyEntry;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class PacketRegister
{
	CDsPluginPack cdpp;
	PacketListener plistener;
	Log clog;
	ProtocolManager pm = ProtocolLibrary.getProtocolManager();
	
	public PacketRegister(CDsPluginPack cdpp)
	{
		this.cdpp = cdpp;
		this.plistener = cdpp.handler.plistener;
		this.clog = cdpp.handler.clog;
	}
	
	public void registerPackets()
	{		
		clog.log("Registering Packets", this);
		List<Entry<Object, Method>> list;
		CDPluginPacket an;
		char c;
		for(CDPlugin cdp : cdpp.handler.plugins.values())
			for(Method m : cdp.getClass().getMethods())
			{
				if((an = m.getAnnotation(CDPluginPacket.class)) == null) continue;
				for(String type : an.types())
				{
					if(type.length() < 2 || ((c = type.charAt(0)) != 's' && c != 'c'))
					{
						clog.log("PacketTypes have to have a 's' or an 'c' on beginning to identify if it's a Server or Client Packet. " + type + " is unidentifyed and would not be registered", this);
						continue;
					}
					if((list = plistener.packets.get(type.toLowerCase())) == null) list = new ArrayList<Entry<Object, Method>>();
					list.add(new MyEntry<Object, Method>(cdp, m));
					plistener.packets.put(type.toLowerCase(), list);
					clog.log("Registering " + (c == 's' ? "Server" : "Client") + type.substring(1) + "Packet to " + cdp.getClass().getSimpleName(), this);
				}
			}
		sortCalls();
		try { registerProtocolLibPackets(plistener.packets.keySet(), plistener.getClass().getMethod("onPacket", PacketEvent.class)); }
		catch (NoSuchMethodException x) { clog.log("Method PacketListener.onPacket(PacketEvent) not found. Packets are not registered", this); return; }
		clog.log("Done", this);
	}
	
	private void registerProtocolLibPackets(Collection<String> packets, Method m)
	{
		for(String packet : packets)
			registerProtocolLibPacket(packet, m);
	}
	private void registerProtocolLibPacket(String packet, Method m)
	{
		PacketType type = getPacketType(packet);
		if(type == null) { clog.log("Could not register " + packet + " Packet", this); return; }
		pm.addPacketListener(getAdapter(type, m, plistener));
		clog.log("Registered " + packet + " Packet to ProtocolLib", this);
	}
	
	private void sortCalls()
	{
		clog.log("Sorting PacketCalls by Priority", this);
		VarTools.sortCalls(plistener.packets, CDPluginPacket.class);
		clog.log("All Packets with Priority between 0 and 10000 sorted", this);
	}
	
	private PacketType getPacketType(String name)
	{
		boolean serverSide = (name.charAt(0) == 's');
		name = name.substring(1);
		for(PacketType type : PacketType.values())
			if(type.isServer() != serverSide) continue;
			else if(type.name().equalsIgnoreCase(name))
				return type;
		return null;
	}
	
	private PacketAdapter getAdapter(PacketType type, final Method m, final Object listener)
	{
		return new PacketAdapter(cdpp, type)
		{
			public void onPacketReceiving(PacketEvent e)
			{
				try { m.invoke(listener, e); }
				catch (Exception x) { x.printStackTrace(clog.getStream()); }
			}
			public void onPacketSending(PacketEvent e)
			{
				try { m.invoke(listener, e); }
				catch (Exception x) { x.printStackTrace(clog.getStream()); }
			}
		};
	}
}
