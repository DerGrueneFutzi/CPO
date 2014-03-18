package net.gmx.teamterrian.CDsPluginPack.handle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CDPluginEvent
{
	boolean ignoreCancelled() default false;
	int priority() default 100;
}
