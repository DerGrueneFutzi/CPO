package net.gmx.teamterrian.CDsPluginPack.handle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CDPluginCommand
{
	String[] commands();
	int priority() default 100;
}
