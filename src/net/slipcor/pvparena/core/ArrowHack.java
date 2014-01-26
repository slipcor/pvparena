package net.slipcor.pvparena.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ArrowHack {

	public ArrowHack(Player player) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		System.out.print(Bukkit.getServer().getVersion());
		
		Method mGetHandle = player.getClass().getMethod("getHandle");
		
		Object cHandle = mGetHandle.invoke(player);
		
		Method mGetDataWatcher = cHandle.getClass().getMethod("getDataWatcher");
		
		Object cWatcher = mGetDataWatcher.invoke(cHandle);
		
		Method mWatch = cWatcher.getClass().getMethod("watch", int.class, Object.class);
		
		mWatch.invoke(cWatcher, 9, (byte) 0);
	}

}
