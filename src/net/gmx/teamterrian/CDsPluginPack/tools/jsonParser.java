package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.Map.Entry;

import org.bukkit.ChatColor;

import net.minecraft.util.com.google.gson.JsonArray;
import net.minecraft.util.com.google.gson.JsonElement;
import net.minecraft.util.com.google.gson.JsonObject;
import net.minecraft.util.com.google.gson.JsonParser;

public class jsonParser
{
	public static String parse(String json)
	{
		String copy = json;
		try
		{
			if(json == null || json.equals("")) return null;
			if(json.equals("\"\"")) return "\"\"";
			return getFullString(getExtraArray(json));
		}
		catch (Exception x)
		{
			return copy;
		}
	}
	
	private static JsonArray getExtraArray(String json)
	{
		JsonElement jelement = new JsonParser().parse(json);
		return jelement.getAsJsonObject().get("extra").getAsJsonArray();
	}
	private static String getFullString(JsonArray jarray)
	{
		String back = "";
		JsonElement jelement;
		for(int i = 0; i < jarray.size(); i++)
		{
			jelement = jarray.get(i);
			if(jelement.isJsonPrimitive())
				back += jelement.getAsString();
			else back += getChatString(jelement.getAsJsonObject());
		}
		return back;
	}
	
	private static String getChatString(JsonObject jobj)
	{
		String back = "";
		String colors = "";
		String mods = "";
		for(Entry<String, JsonElement> jentry : jobj.entrySet())
		{
			switch(jentry.getKey().toLowerCase())
			{
				case "text":
					back = jentry.getValue().getAsString();
					break;
				case "color":
					colors = ChatColor.valueOf(jentry.getValue().getAsString().toUpperCase()) + colors;
					break;
				case "italic":
				case "bold":
				case "underline":
				case "strikethrough":
					if(jentry.getValue().getAsBoolean()) mods = ChatColor.valueOf(jentry.getKey().toUpperCase()) + mods;
					break;
			}
		}
		return colors + mods + back;
	}
}
