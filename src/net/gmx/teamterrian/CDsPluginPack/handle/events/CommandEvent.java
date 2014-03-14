package net.gmx.teamterrian.CDsPluginPack.handle.events;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Command command;
    private CommandSender sender;
    private String[] args;
    public CommandEvent(Command command, CommandSender sender, String[] args) {
    	
        this.cancelled = false;
        this.command = command;
        this.sender = sender;
        this.args = args;
    }
    
    public Command getCommand()
    {
    	return command;
    }
    public CommandSender getSender()
    {
    	return sender;
    }
    public String[] getArgs()
    {
    	return args;
    }
    public void setArgs(String[] args)
    {
    	this.args = args;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
