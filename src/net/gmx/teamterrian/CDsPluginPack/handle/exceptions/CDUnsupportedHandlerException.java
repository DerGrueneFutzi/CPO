package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

public class CDUnsupportedHandlerException extends Exception
{
	private static final long serialVersionUID = 6560555252802829625L;
	
	public CDUnsupportedHandlerException(String method, String clazz)
	{
		super("Handler-Method " + method + " in Class " + clazz + " is not able to handle the Exception");
	}
}
