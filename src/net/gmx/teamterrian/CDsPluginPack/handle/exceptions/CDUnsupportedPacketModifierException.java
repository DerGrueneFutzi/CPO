package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;

public class CDUnsupportedPacketModifierException extends CDException
{
	private static final long serialVersionUID = -741004066139815715L;
	private Object o;
	
	public CDUnsupportedPacketModifierException(Object o)
	{
		this.o = o;
	}
	
	@Override
	public void handle(PluginHandler handler)
	{
		handler.clog.log("Tryed to save an usupported Object from the Modifier of a Packet (" + o.getClass().getName() + ")", this);
	}
	
	public Object getObject()
	{
		return o;
	}
}
