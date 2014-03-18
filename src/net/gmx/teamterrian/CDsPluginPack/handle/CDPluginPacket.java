package net.gmx.teamterrian.CDsPluginPack.handle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CDPluginPacket
{
	int priority() default 100;
	String[] types();
	boolean ignoreCancelled() default false;
}
