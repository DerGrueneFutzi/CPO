package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;

public abstract class CDException extends Exception
{
	private static final long serialVersionUID = 5852946734258157096L;

	public void handle(PluginHandler handler, CommandEvent e) throws CDInvalidArgsException, CDUnsupportedHandlerException
	{
		throw new CDUnsupportedHandlerException("handle(PluginHandler, CommandEvent)", this.getClass().getName());
	}
	public void handle(PluginHandler handler) throws CDUnsupportedHandlerException
	{
		throw new CDUnsupportedHandlerException("handle(PluginHandler)", this.getClass().getName());
	}
}
