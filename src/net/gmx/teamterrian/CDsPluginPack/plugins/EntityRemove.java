package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDWorldNotFoundException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDArrayList;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class EntityRemove extends CDPlugin
{
	Log clog;
	
	public EntityRemove(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.eremove", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "eremove cdpp.eremove 1" })
	public void onCommand(CommandEvent e) throws CDException
	{
		try {process(e.getArgs(), e.getSender()); }
		catch (NumberFormatException x) { throw new CDInvalidArgsException("eremove"); }
	}
	private void process(String[] args, CommandSender sender) throws CDException
	{
		if(args.length == 0) throw new CDInvalidArgsException("eremove");
		Map<EntityType, Integer> counter;
		World w = Bukkit.getWorld(args[args.length - 1]);
		if(w == null) throw new CDWorldNotFoundException(args[args.length - 1]);
		if(args.length == 1)
			counter = doRemove(getTypeList("n"), null, w);
		else if(args.length == 2)
			counter = doRemove(getTypeList(args[0]), null, w);
		else if(args.length == 3)
			counter = doRemove(getTypeList(args[0]), VarTools.getBox(((Player) sender).getLocation(), Integer.valueOf(args[1])), w);
		else if(args.length == 6)
			counter = doRemove(getTypeList(args[0]), VarTools.getBox(VarTools.getPoint(args, 1, w), Integer.valueOf(args[4])), w);
		else if(args.length == 8)
			counter = doRemove(getTypeList(args[0]), VarTools.getBox(args, 1, w), w);
		else throw new CDInvalidArgsException("eremove");
		sender.sendMessage(ChatColor.GREEN + "[ERemover] Entitys removed");
		showCounts(counter);
	}
	
	private void showCounts(Map<EntityType, Integer> counts)
	{
		StringBuilder sb = new StringBuilder();
		for(EntityType type : counts.keySet())
			sb.append(" " + type.name() + " -> " + counts.get(type) + ";");
		String s = sb.toString();
		if(counts.size() != 0) s = s.substring(1, s.length() - 2);
		clog.log(s + " Entitys removed", this);
	}
	
	public void checkEntitys(final String s)
	{
		if(s.startsWith("bacfm "))
			((GroupMail) handler.plugins.get(GroupMail.class)).pDel(null, null);
		else ((MonsterVanish) handler.plugins.get(MonsterVanish.class)).onPlayerRespawn(s);
	}
	public static List<Entity> getEntitys(Location[] boxes, List<EntityType> types, World w)
	{
		List<Entity> back = new CDArrayList<Entity>();
		for(Entity e : w.getEntities())
			if(types.contains(e.getType()) && checkPosition(e.getLocation(), getCoordsArray(boxes[0], boxes[1])))
				back.add(e);
		return back;
	}
	
	public static List<EntityType> getTypeList(String s) throws CDInvalidArgsException
	{
		List<EntityType> back = new CDArrayList<EntityType>();
		switch(s)
		{
			case "a":
				for(EntityType type : EntityType.values())
					back.add(type);
				break;
			case "l":
				back.add(EntityType.BAT);
				back.add(EntityType.BLAZE);
				back.add(EntityType.CAVE_SPIDER);
				back.add(EntityType.CHICKEN);
				back.add(EntityType.COW);
				back.add(EntityType.CREEPER);
				back.add(EntityType.ENDERMAN);
				back.add(EntityType.GHAST);
				back.add(EntityType.GIANT);
				back.add(EntityType.HORSE);
				back.add(EntityType.IRON_GOLEM);
				back.add(EntityType.MAGMA_CUBE);
				back.add(EntityType.OCELOT);
				back.add(EntityType.PIG);
				back.add(EntityType.PIG_ZOMBIE);
				back.add(EntityType.PLAYER);
				back.add(EntityType.SHEEP);
				back.add(EntityType.SILVERFISH);
				back.add(EntityType.SKELETON);
				back.add(EntityType.SLIME);
				back.add(EntityType.SNOWMAN);
				back.add(EntityType.SPIDER);
				back.add(EntityType.SQUID);
				back.add(EntityType.VILLAGER);
				back.add(EntityType.WITCH);
				back.add(EntityType.WITHER);
				back.add(EntityType.WOLF);
				back.add(EntityType.ZOMBIE);
				break;
			case "i":
				back.add(EntityType.DROPPED_ITEM);
				break;
			case "n":
				back.add(EntityType.ARROW);
				back.add(EntityType.EXPERIENCE_ORB);
				break;
			default:
				throw new CDInvalidArgsException("eremove");
		}
		return back;
	}
	
	private Map<EntityType, Integer> doRemove(List<EntityType> toRemove, Location[] boxes, World w)
	{
		Map<EntityType, Integer> counter = new CDHashMap<EntityType, Integer>();
		Location box1, box2;
		if(boxes == null) { box1 = null; box2 = null; }
		else { box1 = boxes[0]; box2 = boxes[1]; }
		boolean isNull = (box1 == null || box2 == null);
		double[] coords = getCoordsArray(box1, box2);
		EntityType type;
		for(Entity e : w.getEntities())
		{
			type = e.getType();
			if(e.getType() == EntityType.PLAYER || (toRemove != null && !toRemove.contains(type))) continue;
			if(!isNull && !checkPosition(e.getLocation(), coords)) continue;
			if(!counter.containsKey(type)) counter.put(type, 1);
			else counter.put(type, counter.get(type) + 1);
			e.remove();
		}
		return counter;
	}
	
	private static boolean checkPosition(Location l, double[] coords)
	{
		coords[0] = l.getX();
		coords[1] = l.getY();
		coords[2] = l.getZ();
		return
				(( coords[3] < coords[0] && coords[6] > coords[0] ) || ( coords[3] > coords[0] && coords[6] < coords[0] )) &&
				(( coords[4] < coords[1] && coords[7] > coords[1] ) || ( coords[4] > coords[1] && coords[7] < coords[1] )) &&
				(( coords[5] < coords[2] && coords[8] > coords[2] ) || ( coords[5] > coords[2] && coords[8] < coords[2] ));
	}
	private static double[] getCoordsArray(Location b1, Location b2)
	{
		if(b1 == null || b2 == null) return null;
		return new double[]
		{
			-1,
			-1,
			-1,
			b1.getX(),
			b1.getY(),
			b1.getZ(),
			b2.getX(),
			b2.getY(),
			b2.getZ()
		};
	}
}
